package com.cristianalmendro.pokeapi.data

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PokemonFilterRepositoryTest {
    private val pokemon = Pokemon(1, "bulbasaur", listOf("grass"), null, 7, 69)
    private val defenses = TypeDefenses("grass", listOf("fire"), listOf("water"), emptyList())

    @Test fun typeAndRangeFiltersReadAllSavedRowsWithoutAnyNetwork() = runTest {
        val local = FakePokemonLocalDataSource()
        local.savePokemon(pokemon)
        local.savePokemon(pokemon.copy(id = 10025, types = listOf("water")))
        val result = PokeApiRepository(OfflineApi(), local).getFilteredCache(PokemonFilters(types = setOf("grass")))
        assertEquals(listOf(pokemon), result.pokemon)
        assertEquals(2, result.cachedCount)
        assertEquals(10025, result.maximumId)
    }

    @Test fun sizeBackfillOnlyFetchesMatchingSavedIdsAndIsReusedOffline() = runTest {
        val local = FakePokemonLocalDataSource()
        local.savePokemon(pokemon.copy(heightDecimetres = null, weightHectograms = null))
        local.savePokemon(pokemon.copy(id = 2, types = listOf("water"), heightDecimetres = null))
        val requests = mutableListOf<Int>()
        val api = object : OfflineApi() {
            override suspend fun getPokemon(id: Int): Pokemon { requests += id; return pokemon }
        }
        val filters = PokemonFilters(types = setOf("grass"), heights = setOf(PokemonHeight.SHORT))
        assertEquals(listOf(pokemon), PokeApiRepository(api, local).getFilteredCache(filters).pokemon)
        assertEquals(listOf(1), requests)
        assertEquals(listOf(pokemon), PokeApiRepository(OfflineApi(), local).getFilteredCache(filters).pokemon)
    }

    @Test fun typeDefenseDataIsSavedAndReusedAcrossRepositoryInstances() = runTest {
        val local = FakePokemonLocalDataSource()
        local.savePokemon(pokemon)
        var calls = 0
        val api = object : OfflineApi() {
            override suspend fun getTypeDefenses(name: String): TypeDefenses { calls++; return defenses }
        }
        val filters = PokemonFilters(weaknesses = setOf("fire"))
        assertEquals(listOf(pokemon), PokeApiRepository(api, local).getFilteredCache(filters).pokemon)
        assertEquals(listOf(pokemon), PokeApiRepository(OfflineApi(), local).getFilteredCache(filters).pokemon)
        assertEquals(1, calls)
    }

    @Test fun missingWeaknessDataFailsRatherThanReturningFalseEmptyResults() = runTest {
        val local = FakePokemonLocalDataSource()
        local.savePokemon(pokemon)
        try {
            PokeApiRepository(OfflineApi(), local).getFilteredCache(PokemonFilters(weaknesses = setOf("fire")))
            fail("Expected missing metadata failure")
        } catch (_: IOException) { }
        assertEquals(pokemon, local.getPokemon(1))
    }

    private open class OfflineApi : PokemonApi {
        override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences = error("Must not download a catalogue")
        override suspend fun getPokemon(id: Int): Pokemon = throw IOException("offline")
        override suspend fun getTypeDefenses(name: String): TypeDefenses = throw IOException("offline")
    }
}
