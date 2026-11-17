package com.cristianalmendro.pokeapi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.cristianalmendro.pokeapi.data.*
import com.cristianalmendro.pokeapi.data.local.*
import com.cristianalmendro.pokeapi.data.search.*
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.json.JSONObject
import org.junit.Test

class PokemonSearchPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "search-test-${UUID.randomUUID()}.db"

    @After fun cleanUp() { context.deleteDatabase(name) }

    @Test fun migrationPreservesVersionOnePokemonPagesAndCursors() = runBlocking {
        val asset = "com.cristianalmendro.pokeapi.data.local.PokemonDatabase/1.json"
        val schema = InstrumentationRegistry.getInstrumentation().context.assets.open(asset)
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).apply {
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.optJSONArray("indices") ?: continue
                for (position in 0 until indices.length()) {
                    execSQL(indices.getJSONObject(position).getString("createSql")
                        .replace("\${TABLE_NAME}", entity.getString("tableName")))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) execSQL(setup.getString(index))
            execSQL("INSERT INTO pokemon VALUES (25, 'pikachu', '[\"electric\"]', NULL)")
            execSQL("INSERT INTO pokemon_pages VALUES (0, 20, 20, 1, 1234)")
            execSQL("INSERT INTO pokemon_page_entries VALUES (0, 20, 0, 25)")
            version = 1
            close()
        }
        val database = openDatabase()
        try {
            val saved = RoomPokemonLocalDataSource(database).getPage(0, 20)
            assertEquals(25, saved?.items?.single()?.id)
            assertEquals(listOf("electric"), saved?.items?.single()?.types)
            assertEquals(20, saved?.nextOffset)
            assertFalse(RoomPokemonSearchLocalDataSource(database).hasCompleteIndex())
            assertEquals(2, database.openHelper.readableDatabase.version)
        } finally { database.close() }
    }

    @Test fun searchesCompleteIndexOfflineAfterReopenWithoutDownloadingDetails() = runBlocking {
        var database = openDatabase()
        RoomPokemonSearchLocalDataSource(database).replaceIndex(entries)
        assertNull(RoomPokemonLocalDataSource(database).getPokemon(25))
        database.close()
        database = openDatabase()
        try {
            val api = object : PokemonApi {
                override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences = error("No network")
                override suspend fun getPokemon(id: Int): Pokemon = error("No network")
            }
            val details = object : PokemonDetailsRepository {
                override suspend fun getPokemon(id: Int): Pokemon = error("Search must not hydrate all matches")
            }
            val repository = LocalPokemonSearchRepository(api, RoomPokemonSearchLocalDataSource(database), details)
            repository.ensureIndex()
            assertEquals(listOf(25), repository.search("  PIKA  ", 0, 20).entries.map { it.id })
            assertEquals(listOf(25), repository.search("#025", 0, 20).entries.map { it.id })
            assertEquals(listOf(122), repository.search("mr mime", 0, 20).entries.map { it.id })
        } finally { database.close() }
    }

    @Test fun wildcardCharactersAreLiteralAndResultsHaveStablePagination() = runBlocking {
        val database = openDatabase()
        try {
            val local = RoomPokemonSearchLocalDataSource(database)
            local.replaceIndex(entries + PokemonSearchEntry(999, "test_name"))
            assertEquals(listOf(999), local.search("_", 0, 20).map { it.id })
            assertTrue(local.search("%", 0, 20).isEmpty())
            assertTrue(local.search("' OR 1=1", 0, 20).isEmpty())
            assertEquals(listOf(1), local.search("saur", 0, 1).map { it.id })
            assertEquals(listOf(2), local.search("saur", 1, 1).map { it.id })
        } finally { database.close() }
    }

    @Test fun failedReplacementDoesNotDestroyPreviouslyCommittedIndex() = runBlocking {
        val database = openDatabase()
        try {
            val local = RoomPokemonSearchLocalDataSource(database)
            local.replaceIndex(entries)
            try {
                local.replaceIndex(listOf(PokemonSearchEntry(1, "one"), PokemonSearchEntry(1, "duplicate")))
                fail("Expected duplicate-ID constraint failure")
            } catch (_: IOException) { }
            assertTrue(local.hasCompleteIndex())
            assertEquals(listOf(25), local.search("pika", 0, 20).map { it.id })
        } finally { database.close() }
    }

    private fun openDatabase() = Room.databaseBuilder(context, PokemonDatabase::class.java, name)
        .addMigrations(PokemonDatabase.MIGRATION_1_2).build()
    private val entries = listOf(PokemonSearchEntry(1, "bulbasaur"), PokemonSearchEntry(2, "ivysaur"),
        PokemonSearchEntry(25, "pikachu"), PokemonSearchEntry(122, "mr-mime"))
}
