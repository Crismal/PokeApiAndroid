package com.cristianalmendro.pokeapi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon WHERE id = :id")
    suspend fun getPokemon(id: Int): PokemonEntity?

    @Query("SELECT * FROM pokemon_pages WHERE offset = :offset AND pageSize = :limit")
    suspend fun getPage(offset: Int, limit: Int): PokemonPageEntity?

    @Query("""
        SELECT pokemon.* FROM pokemon
        INNER JOIN pokemon_page_entries AS entry ON entry.pokemonId = pokemon.id
        WHERE entry.offset = :offset AND entry.pageSize = :limit
        ORDER BY entry.position
    """)
    suspend fun getPagePokemon(offset: Int, limit: Int): List<PokemonEntity>

    @Upsert suspend fun upsertPokemon(pokemon: List<PokemonEntity>)
    @Upsert suspend fun upsertPage(page: PokemonPageEntity)
    @Insert suspend fun insertEntries(entries: List<PokemonPageEntry>)

    @Query("DELETE FROM pokemon_page_entries WHERE offset = :offset AND pageSize = :limit")
    suspend fun deleteEntries(offset: Int, limit: Int)
}
