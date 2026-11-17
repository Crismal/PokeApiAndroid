package com.cristianalmendro.pokeapi.data

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class PokeApiRepository(private val api: PokemonApi) : PokemonRepository {
    private val pokemonCache = ConcurrentHashMap<Int, Pokemon>()
    private val pageCache = ConcurrentHashMap<Pair<Int, Int>, PokemonPage>()
    private val detailRequests = Semaphore(4)

    override suspend fun getPage(offset: Int, limit: Int): PokemonPage = coroutineScope {
        require(offset >= 0 && limit > 0)
        val key = offset to limit
        pageCache[key]?.let { return@coroutineScope it }
        val references = api.getReferences(offset, limit)
        // awaitAll retains the API's ordering even when details finish out of order.
        val items = references.ids.map { id ->
            async {
                pokemonCache[id] ?: detailRequests.withPermit {
                    pokemonCache[id] ?: api.getPokemon(id).also { pokemonCache[id] = it }
                }
            }
        }.awaitAll()
        PokemonPage(items, references.nextOffset).also { pageCache[key] = it }
    }
}
