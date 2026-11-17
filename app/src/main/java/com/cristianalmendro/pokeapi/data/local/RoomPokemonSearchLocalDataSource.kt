package com.cristianalmendro.pokeapi.data.local

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.cristianalmendro.pokeapi.data.search.PokemonSearchEntry
import com.cristianalmendro.pokeapi.data.search.PokemonSearchLocalDataSource
import java.io.IOException
import java.util.Locale

class RoomPokemonSearchLocalDataSource(
    private val database: PokemonDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) : PokemonSearchLocalDataSource {
    private val dao = database.searchDao()

    override suspend fun hasCompleteIndex(): Boolean = storageOperation {
        database.withTransaction {
            val state = dao.indexState()
            state != null && state.itemCount > 0 && state.itemCount == dao.count()
        }
    }

    override suspend fun replaceIndex(entries: List<PokemonSearchEntry>) = storageOperation {
        database.withTransaction {
            dao.deleteIndex()
            dao.insertIndex(entries.map { PokemonSearchEntity(it.id, it.name, normalize(it.name)) })
            dao.saveState(PokemonSearchIndexState(itemCount = entries.size, syncedAtMillis = clock()))
        }
    }

    override suspend fun search(query: String, offset: Int, limit: Int): List<PokemonSearchEntry> =
        storageOperation {
            val normalized = normalize(query)
            val exactId = normalized.removePrefix("#").toIntOrNull()
            val literal = normalized.replace("!", "!!").replace("%", "!%").replace("_", "!_")
            dao.search("%$literal%", exactId, offset, limit).map { PokemonSearchEntry(it.id, it.name) }
        }

    private fun normalize(value: String) = value.trim().lowercase(Locale.ROOT).replace('-', ' ')
    private suspend fun <T> storageOperation(block: suspend () -> T): T = try {
        block()
    } catch (error: SQLiteException) {
        throw IOException("Could not access the local search index", error)
    }
}
