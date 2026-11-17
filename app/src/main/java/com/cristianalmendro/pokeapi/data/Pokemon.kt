package com.cristianalmendro.pokeapi.data

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val artworkUrl: String?,
)

data class PokemonPage(val items: List<Pokemon>, val nextOffset: Int?)

interface PokemonRepository {
    suspend fun getPage(offset: Int, limit: Int): PokemonPage
}
