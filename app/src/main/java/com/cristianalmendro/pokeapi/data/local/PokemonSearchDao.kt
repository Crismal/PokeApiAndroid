package com.cristianalmendro.pokeapi.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

@Entity(tableName = "pokemon_search_index")
data class PokemonSearchEntity(@PrimaryKey val id: Int, val name: String, val normalizedName: String)

@Entity(tableName = "pokemon_search_state")
data class PokemonSearchIndexState(@PrimaryKey val id: Int = 0, val itemCount: Int, val syncedAtMillis: Long)

@Dao
interface PokemonSearchDao {
    @Query("SELECT * FROM pokemon_search_state WHERE id = 0")
    suspend fun indexState(): PokemonSearchIndexState?
    @Query("SELECT COUNT(*) FROM pokemon_search_index")
    suspend fun count(): Int
    @Query("DELETE FROM pokemon_search_index")
    suspend fun deleteIndex()
    @Insert suspend fun insertIndex(entries: List<PokemonSearchEntity>)
    @Upsert suspend fun saveState(state: PokemonSearchIndexState)

    @Query("""
        SELECT * FROM pokemon_search_index
        WHERE (:exactId IS NOT NULL AND id = :exactId)
            OR (:exactId IS NULL AND normalizedName LIKE :pattern ESCAPE '!')
        ORDER BY id LIMIT :limit OFFSET :offset
    """)
    suspend fun search(pattern: String, exactId: Int?, offset: Int, limit: Int): List<PokemonSearchEntity>
}
