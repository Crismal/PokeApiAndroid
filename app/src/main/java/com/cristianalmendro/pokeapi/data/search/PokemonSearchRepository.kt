package com.cristianalmendro.pokeapi.data.search

import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonApi
import com.cristianalmendro.pokeapi.data.PokemonDetailsRepository
import java.io.IOException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class PokemonSearchEntry(val id: Int, val name: String)
data class PokemonSearchPage(val entries: List<PokemonSearchEntry>, val hasMore: Boolean)

interface PokemonSearchLocalDataSource {
    suspend fun hasCompleteIndex(): Boolean
    suspend fun replaceIndex(entries: List<PokemonSearchEntry>)
    suspend fun search(query: String, offset: Int, limit: Int): List<PokemonSearchEntry>
}

interface PokemonSearchRepository {
    suspend fun ensureIndex()
    suspend fun search(query: String, offset: Int, limit: Int): PokemonSearchPage
    suspend fun getPokemon(id: Int): Pokemon
}

class LocalPokemonSearchRepository(
    private val api: PokemonApi,
    private val local: PokemonSearchLocalDataSource,
    private val details: PokemonDetailsRepository,
) : PokemonSearchRepository {
    private val indexMutex = Mutex()

    override suspend fun ensureIndex() = indexMutex.withLock {
        if (local.hasCompleteIndex()) return@withLock
        val entries = linkedMapOf<Int, PokemonSearchEntry>()
        var offset = 0
        var pages = 0
        while (true) {
            if (++pages > 1000) throw IOException("Catalogue pagination exceeded its safety limit")
            val page = api.getReferences(offset, 200)
            if (page.ids.size != page.names.size) throw IOException("Catalogue names are missing")
            val previousSize = entries.size
            page.ids.zip(page.names).forEach { (id, name) ->
                if (name.isBlank()) throw IOException("Empty Pokemon name")
                entries[id] = PokemonSearchEntry(id, name)
            }
            val next = page.nextOffset ?: break
            if (next <= offset || previousSize == entries.size) {
                throw IOException("Catalogue pagination did not advance")
            }
            offset = next
        }
        if (entries.isEmpty()) throw IOException("Empty Pokemon catalogue")
        // Publish all names and the completion marker together, never a partial catalogue.
        local.replaceIndex(entries.values.toList())
    }

    override suspend fun search(query: String, offset: Int, limit: Int): PokemonSearchPage {
        require(offset >= 0 && limit in 1..100)
        if (query.isBlank()) return PokemonSearchPage(emptyList(), false)
        val matches = local.search(query, offset, limit + 1)
        return PokemonSearchPage(matches.take(limit), matches.size > limit)
    }

    override suspend fun getPokemon(id: Int): Pokemon = details.getPokemon(id)
}
