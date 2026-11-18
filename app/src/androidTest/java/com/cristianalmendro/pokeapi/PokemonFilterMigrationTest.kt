package com.cristianalmendro.pokeapi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.cristianalmendro.pokeapi.data.TypeDefenses
import com.cristianalmendro.pokeapi.data.local.PokemonDatabase
import com.cristianalmendro.pokeapi.data.local.RoomPokemonLocalDataSource
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class PokemonFilterMigrationTest {
    @Test fun releasedAndSearchSchemasMigrateWithoutLosingSavedPokemonOrPages() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (version in listOf(1, 2)) {
            val name = "filter-migration-${UUID.randomUUID()}.db"
            try {
                val fixture = InstrumentationRegistry.getInstrumentation().context.assets
                    .open("com.cristianalmendro.pokeapi.data.local.PokemonDatabase/$version.json")
                    .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
                context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { legacy ->
                    val entities = fixture.getJSONArray("entities")
                    repeat(entities.length()) { index ->
                        val entity = entities.getJSONObject(index)
                        legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                        val indices = entity.optJSONArray("indices")
                        repeat(indices?.length() ?: 0) { position ->
                            legacy.execSQL(requireNotNull(indices).getJSONObject(position).getString("createSql")
                                .replace("\${TABLE_NAME}", entity.getString("tableName")))
                        }
                    }
                    legacy.execSQL("INSERT INTO pokemon (id,name,types,artworkUrl) VALUES (1,'bulbasaur','[\"grass\"]',NULL)")
                    legacy.execSQL("INSERT INTO pokemon_pages VALUES (0,20,20,1,1234)")
                    legacy.execSQL("INSERT INTO pokemon_page_entries VALUES (0,20,0,1)")
                    legacy.version = version
                }
                val database = Room.databaseBuilder(context, PokemonDatabase::class.java, name)
                    .addMigrations(PokemonDatabase.MIGRATION_1_3, PokemonDatabase.MIGRATION_2_3).build()
                try {
                    val local = RoomPokemonLocalDataSource(database)
                    val pokemon = requireNotNull(local.getPokemon(1))
                    assertNull(pokemon.heightDecimetres)
                    assertEquals(20, local.getPage(0, 20)?.nextOffset)
                    local.savePokemon(pokemon.copy(heightDecimetres = 7, weightHectograms = 69))
                    local.saveTypeDefenses(TypeDefenses("grass", listOf("fire"), emptyList(), emptyList()))
                } finally { database.close() }
                val reopened = Room.databaseBuilder(context, PokemonDatabase::class.java, name).build()
                try {
                    val local = RoomPokemonLocalDataSource(reopened)
                    assertEquals(7, local.getPokemon(1)?.heightDecimetres)
                    assertEquals(listOf("fire"), local.getTypeDefenses("grass")?.doubleDamageFrom)
                } finally { reopened.close() }
            } finally { context.deleteDatabase(name) }
        }
    }
}
