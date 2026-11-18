package com.cristianalmendro.pokeapi.data

import com.cristianalmendro.pokeapi.data.local.PokemonLocalDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class PokeApiRepository(
    private val api: PokemonApi,
    private val local: PokemonLocalDataSource,
) : PokemonRepository {
    private val detailRequests = Semaphore(4)

    override suspend fun getFilteredCache(filters: PokemonFilters): CachedPokemonResult = coroutineScope {
        val saved = local.getAllPokemon()
        val candidates = saved.filter { it.id >= filters.minimumId &&
            (filters.maximumId == null || it.id <= filters.maximumId) &&
            (filters.types.isEmpty() || it.types.any { type -> type in filters.types }) }
        val enriched = candidates.map { pokemon ->
            async {
                val needsHeight = filters.heights.isNotEmpty() && pokemon.heightDecimetres == null
                val needsWeight = filters.weights.isNotEmpty() && pokemon.weightHectograms == null
                if (needsHeight || needsWeight) detailRequests.withPermit {
                    api.getPokemon(pokemon.id).also {
                        if ((needsHeight && it.heightDecimetres == null) ||
                            (needsWeight && it.weightHectograms == null)) {
                            throw java.io.IOException("Missing cached filter attributes")
                        }
                        local.savePokemon(it)
                    }
                } else pokemon
            }
        }.awaitAll()
        val defenses = if (filters.weaknesses.isEmpty()) emptyMap() else {
            enriched.flatMap { it.types }.distinct().associateWith { name ->
                local.getTypeDefenses(name) ?: api.getTypeDefenses(name).also { local.saveTypeDefenses(it) }
            }
        }
        CachedPokemonResult(enriched.filter { filters.matches(it, defenses) }, saved.size,
            maxOf(1000, saved.maxOfOrNull { it.id } ?: 1))
    }

    override suspend fun getPage(offset: Int, limit: Int): PokemonPage = coroutineScope {
        require(offset >= 0 && limit > 0)
        local.getPage(offset, limit)?.let { return@coroutineScope it }
        val references = api.getReferences(offset, limit)
        // awaitAll retains the API's ordering even when details finish out of order.
        val items = references.ids.map { id ->
            async {
                local.getPokemon(id) ?: detailRequests.withPermit {
                    local.getPokemon(id) ?: api.getPokemon(id).also { local.savePokemon(it) }
                }
            }
        }.awaitAll()
        val page = PokemonPage(items, references.nextOffset)
        local.savePage(offset, limit, page)
        // Return the committed local snapshot, keeping Room as the source of truth.
        local.getPage(offset, limit) ?: throw java.io.IOException("Page was not saved completely")
    }
}
