package com.cristianalmendro.pokeapi.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.cristianalmendro.pokeapi.data.Pokemon
import org.json.JSONArray

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val types: List<String>,
    val artworkUrl: String?,
) {
    fun toPokemon() = Pokemon(id, name, types, artworkUrl)
}

fun Pokemon.toEntity() = PokemonEntity(id, name, types, artworkUrl)

@Entity(tableName = "pokemon_pages", primaryKeys = ["offset", "pageSize"])
data class PokemonPageEntity(
    val offset: Int,
    val pageSize: Int,
    val nextOffset: Int?,
    val itemCount: Int,
    val fetchedAtMillis: Long,
)

@Entity(
    tableName = "pokemon_page_entries",
    primaryKeys = ["offset", "pageSize", "position"],
    foreignKeys = [
        ForeignKey(entity = PokemonPageEntity::class,
            parentColumns = ["offset", "pageSize"], childColumns = ["offset", "pageSize"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PokemonEntity::class,
            parentColumns = ["id"], childColumns = ["pokemonId"],
            onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("pokemonId")],
)
data class PokemonPageEntry(
    val offset: Int,
    val pageSize: Int,
    val position: Int,
    val pokemonId: Int,
)

class PokemonTypeConverters {
    @TypeConverter fun encodeTypes(types: List<String>): String = JSONArray(types).toString()
    @TypeConverter fun decodeTypes(value: String): List<String> {
        val json = JSONArray(value)
        return List(json.length()) { json.getString(it) }
    }
}
