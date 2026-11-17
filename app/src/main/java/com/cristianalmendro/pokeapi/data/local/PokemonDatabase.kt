package com.cristianalmendro.pokeapi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PokemonEntity::class, PokemonPageEntity::class, PokemonPageEntry::class,
        PokemonSearchEntity::class, PokemonSearchIndexState::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(PokemonTypeConverters::class)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun searchDao(): PokemonSearchDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `pokemon_search_index` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `normalizedName` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `pokemon_search_state` (`id` INTEGER NOT NULL, `itemCount` INTEGER NOT NULL, `syncedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}
