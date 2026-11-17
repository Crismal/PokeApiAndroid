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
) : PokemonRepository, PokemonDetailsRepository {
    private val detailRequests = Semaphore(4)

    override suspend fun getPage(offset: Int, limit: Int): PokemonPage = coroutineScope {
        require(offset >= 0 && limit > 0)
        local.getPage(offset, limit)?.let { return@coroutineScope it }
        val references = api.getReferences(offset, limit)
        // awaitAll retains the API's ordering even when details finish out of order.
        val items = references.ids.map { id ->
            async { getPokemon(id) }
        }.awaitAll()
        val page = PokemonPage(items, references.nextOffset)
        local.savePage(offset, limit, page)
        // Return the committed local snapshot, keeping Room as the source of truth.
        local.getPage(offset, limit) ?: throw java.io.IOException("Page was not saved completely")
    }

    override suspend fun getPokemon(id: Int): Pokemon =
        local.getPokemon(id) ?: detailRequests.withPermit {
            local.getPokemon(id) ?: api.getPokemon(id).also { local.savePokemon(it) }
        }
}
