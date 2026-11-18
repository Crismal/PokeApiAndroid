package com.cristianalmendro.pokeapi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PokemonEntity::class, PokemonPageEntity::class, PokemonPageEntry::class, TypeDefenseEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(PokemonTypeConverters::class)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    companion object {
        val MIGRATION_1_3 = filterMigration(1)
        // Version 2 is already used by the separate search feature.
        val MIGRATION_2_3 = filterMigration(2)

        private fun filterMigration(from: Int) = object : Migration(from, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pokemon ADD COLUMN heightDecimetres INTEGER")
                db.execSQL("ALTER TABLE pokemon ADD COLUMN weightHectograms INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS type_defenses (name TEXT NOT NULL PRIMARY KEY, doubleDamageFrom TEXT NOT NULL, halfDamageFrom TEXT NOT NULL, noDamageFrom TEXT NOT NULL)")
            }
        }
    }
}
