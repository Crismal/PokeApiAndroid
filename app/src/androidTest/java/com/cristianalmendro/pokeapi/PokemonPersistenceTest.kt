package com.cristianalmendro.pokeapi

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.cristianalmendro.pokeapi.data.PokeApiRepository
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonApi
import com.cristianalmendro.pokeapi.data.PokemonPage
import com.cristianalmendro.pokeapi.data.PokemonReferences
import com.cristianalmendro.pokeapi.data.local.PokemonDatabase
import com.cristianalmendro.pokeapi.data.local.PokemonPageEntity
import com.cristianalmendro.pokeapi.data.local.PokemonPageEntry
import com.cristianalmendro.pokeapi.data.local.PokemonLocalDataSource
import com.cristianalmendro.pokeapi.data.local.RoomPokemonLocalDataSource
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PokemonPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "pokemon-test-${UUID.randomUUID()}.db"
    private lateinit var database: PokemonDatabase

    @Before fun setUp() { database = openDatabase() }
    @After fun tearDown() {
        database.close()
        context.deleteDatabase(name)
    }

    @Test fun pagesAndOrderSurviveDatabaseReopenWithoutNetwork() = runBlocking {
        val first = PokemonPage(listOf(pokemon(25), pokemon(1)), 20)
        val last = PokemonPage(listOf(pokemon(99)), null)
        val store = RoomPokemonLocalDataSource(database) { 1234L }
        store.savePage(0, 20, first)
        store.savePage(20, 20, last)
        assertEquals(1234L, database.pokemonDao().getPage(0, 20)?.fetchedAtMillis)
        database.close()
        database = openDatabase()
        val offline = object : PokemonApi {
            override suspend fun getTypeDefenses(name: String): com.cristianalmendro.pokeapi.data.TypeDefenses = error("Unexpected type request")
            override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences =
                error("Unexpected network page request")
            override suspend fun getPokemon(id: Int): Pokemon = error("Unexpected detail request")
        }
        val repository = PokeApiRepository(offline, RoomPokemonLocalDataSource(database))
        assertEquals(first, repository.getPage(0, 20))
        assertEquals(last, repository.getPage(20, 20))
    }

    @Test fun pageWriteRollsBackIfEntryCannotBeStored() = runBlocking {
        try {
            database.withTransaction {
                database.pokemonDao().upsertPage(PokemonPageEntity(0, 20, 20, 1, 1L))
                database.pokemonDao().insertEntries(listOf(PokemonPageEntry(0, 20, 0, 999)))
            }
            fail("Expected foreign-key failure")
        } catch (_: SQLiteConstraintException) { }
        assertNull(database.pokemonDao().getPage(0, 20))
    }

    @Test fun successfulDetailsSurviveFailedPageAndAreReusedAfterReopen() = runBlocking {
        var shouldFail = true
        val requests = mutableListOf<Int>()
        val firstDetailSaved = CompletableDeferred<Unit>()
        val api = object : PokemonApi {
            override suspend fun getTypeDefenses(name: String): com.cristianalmendro.pokeapi.data.TypeDefenses = error("Unexpected type request")
            override suspend fun getReferences(offset: Int, limit: Int) =
                PokemonReferences(listOf(1, 2), null)
            override suspend fun getPokemon(id: Int): Pokemon {
                requests += id
                if (id == 2) firstDetailSaved.await()
                if (id == 2 && shouldFail) throw IOException("offline")
                return pokemon(id)
            }
        }
        var store = RoomPokemonLocalDataSource(database)
        val recordingStore = object : PokemonLocalDataSource by store {
            override suspend fun savePokemon(pokemon: Pokemon) {
                store.savePokemon(pokemon)
                if (pokemon.id == 1) firstDetailSaved.complete(Unit)
            }
        }
        try {
            PokeApiRepository(api, recordingStore).getPage(0, 20)
            fail("Expected failed page")
        } catch (_: IOException) { }
        assertNull(store.getPage(0, 20))
        assertEquals(pokemon(1), store.getPokemon(1))
        database.close()
        database = openDatabase()
        store = RoomPokemonLocalDataSource(database)
        shouldFail = false
        assertEquals(listOf(1, 2), PokeApiRepository(api, store).getPage(0, 20).items.map { it.id })
        assertEquals(1, requests.count { it == 1 })
        assertEquals(2, requests.count { it == 2 })
    }

    @Test fun replacingPageDoesNotLeaveOldPositionsAndKeysIncludePageSize() = runBlocking {
        val store = RoomPokemonLocalDataSource(database)
        store.savePage(0, 20, PokemonPage(listOf(pokemon(1), pokemon(2)), 20))
        store.savePage(0, 10, PokemonPage(listOf(pokemon(3)), 10))
        store.savePage(0, 20, PokemonPage(listOf(pokemon(2)), null))
        assertEquals(PokemonPage(listOf(pokemon(2)), null), store.getPage(0, 20))
        assertEquals(PokemonPage(listOf(pokemon(3)), 10), store.getPage(0, 10))
    }

    @Test fun emptyPageIsPersistedAndIncompletePageIsNotReturned() = runBlocking {
        val store = RoomPokemonLocalDataSource(database)
        store.savePage(20, 20, PokemonPage(emptyList(), null))
        assertEquals(PokemonPage(emptyList(), null), store.getPage(20, 20))
        store.savePage(0, 20, PokemonPage(listOf(pokemon(1), pokemon(2)), 20))
        database.openHelper.writableDatabase.execSQL("DELETE FROM pokemon WHERE id = 1")
        assertNull(store.getPage(0, 20))
    }

    private fun openDatabase() = Room.databaseBuilder(context, PokemonDatabase::class.java, name).build()
    private fun pokemon(id: Int) = Pokemon(id, "pokemon-$id", listOf("grass", "poison"), null)
}
