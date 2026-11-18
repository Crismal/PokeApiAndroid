package com.cristianalmendro.pokeapi.data.local

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonPage
import com.cristianalmendro.pokeapi.data.TypeDefenses
import java.io.IOException

class RoomPokemonLocalDataSource(
    private val database: PokemonDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) : PokemonLocalDataSource {
    private val dao = database.pokemonDao()

    override suspend fun getAllPokemon() = storageOperation { dao.getAllPokemon().map { it.toPokemon() } }
    override suspend fun getTypeDefenses(name: String) = storageOperation { dao.getTypeDefenses(name)?.toDefenses() }
    override suspend fun saveTypeDefenses(defenses: TypeDefenses) = storageOperation {
        dao.upsertTypeDefenses(defenses.toEntity())
    }

    override suspend fun getPage(offset: Int, limit: Int): PokemonPage? = storageOperation {
        database.withTransaction {
            val page = dao.getPage(offset, limit) ?: return@withTransaction null
            val pokemon = dao.getPagePokemon(offset, limit)
            // Never return a partial page with a cursor that would skip missing rows.
            if (pokemon.size != page.itemCount) return@withTransaction null
            PokemonPage(pokemon.map { it.toPokemon() }, page.nextOffset)
        }
    }

    override suspend fun getPokemon(id: Int): Pokemon? = storageOperation {
        dao.getPokemon(id)?.toPokemon()
    }

    override suspend fun savePokemon(pokemon: Pokemon) = storageOperation {
        dao.upsertPokemon(listOf(pokemon.toEntity()))
    }

    override suspend fun savePage(offset: Int, limit: Int, page: PokemonPage) = storageOperation {
        database.withTransaction {
            dao.upsertPokemon(page.items.map { it.toEntity() })
            dao.upsertPage(PokemonPageEntity(offset, limit, page.nextOffset, page.items.size, clock()))
            dao.deleteEntries(offset, limit)
            dao.insertEntries(page.items.mapIndexed { position, pokemon ->
                PokemonPageEntry(offset, limit, position, pokemon.id)
            })
        }
    }

    private suspend fun <T> storageOperation(block: suspend () -> T): T = try {
        block()
    } catch (error: SQLiteException) {
        throw IOException("Could not access saved Pokemon", error)
    }
}
