package com.cristianalmendro.pokeapi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PokemonEntity::class, PokemonPageEntity::class, PokemonPageEntry::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(PokemonTypeConverters::class)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
