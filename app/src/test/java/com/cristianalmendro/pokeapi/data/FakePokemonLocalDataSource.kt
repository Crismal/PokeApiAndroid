package com.cristianalmendro.pokeapi.data

import com.cristianalmendro.pokeapi.data.local.PokemonLocalDataSource

internal class FakePokemonLocalDataSource : PokemonLocalDataSource {
    private val pokemon = mutableMapOf<Int, Pokemon>()
    private val pages = mutableMapOf<Pair<Int, Int>, PokemonPage>()

    override suspend fun getPokemon(id: Int) = pokemon[id]
    override suspend fun getPage(offset: Int, limit: Int) = pages[offset to limit]
    override suspend fun savePokemon(pokemon: Pokemon) { this.pokemon[pokemon.id] = pokemon }
    override suspend fun savePage(offset: Int, limit: Int, page: PokemonPage) {
        page.items.forEach { savePokemon(it) }
        pages[offset to limit] = page
    }
}
