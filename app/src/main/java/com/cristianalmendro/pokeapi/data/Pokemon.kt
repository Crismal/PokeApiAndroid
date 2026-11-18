package com.cristianalmendro.pokeapi.data

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val artworkUrl: String?,
    val heightDecimetres: Int? = null,
    val weightHectograms: Int? = null,
)

data class PokemonPage(val items: List<Pokemon>, val nextOffset: Int?)

interface PokemonRepository {
    suspend fun getPage(offset: Int, limit: Int): PokemonPage
    suspend fun getFilteredCache(filters: PokemonFilters): CachedPokemonResult
}
