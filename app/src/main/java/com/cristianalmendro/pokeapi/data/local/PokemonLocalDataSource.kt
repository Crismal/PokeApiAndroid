package com.cristianalmendro.pokeapi.data.local

import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonPage

/** Persistent records; a missing page is different from a saved, empty final page. */
interface PokemonLocalDataSource {
    suspend fun getPage(offset: Int, limit: Int): PokemonPage?
    suspend fun getPokemon(id: Int): Pokemon?
    suspend fun savePokemon(pokemon: Pokemon)
    suspend fun savePage(offset: Int, limit: Int, page: PokemonPage)
}
