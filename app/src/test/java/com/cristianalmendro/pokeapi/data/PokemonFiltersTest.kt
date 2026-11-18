package com.cristianalmendro.pokeapi.data

import org.junit.Assert.*
import org.junit.Test

class PokemonFiltersTest {
    private val grass = TypeDefenses("grass", listOf("fire", "poison"), listOf("water"), emptyList())
    private val poison = TypeDefenses("poison", listOf("ground"), listOf("poison"), emptyList())
    private val pokemon = Pokemon(1, "bulbasaur", listOf("grass", "poison"), null, 7, 69)
    private val chart = mapOf("grass" to grass, "poison" to poison)

    @Test fun optionsMatchAnyWithinGroupAndAllAcrossGroups() {
        assertTrue(PokemonFilters(types = setOf("grass", "fire"), heights = setOf(PokemonHeight.SHORT))
            .matches(pokemon, chart))
        assertFalse(PokemonFilters(types = setOf("grass"), heights = setOf(PokemonHeight.TALL))
            .matches(pokemon, chart))
    }

    @Test fun weaknessRequiresNetMultiplierAboveOneAndHonorsImmunity() {
        assertTrue(PokemonFilters(weaknesses = setOf("fire")).matches(pokemon, chart))
        assertFalse(PokemonFilters(weaknesses = setOf("poison")).matches(pokemon, chart))
        assertFalse(PokemonFilters(weaknesses = setOf("water")).matches(pokemon, chart))
        val immune = chart + ("poison" to poison.copy(noDamageFrom = listOf("fire")))
        assertFalse(PokemonFilters(weaknesses = setOf("fire")).matches(pokemon, immune))
        assertFalse(PokemonFilters(weaknesses = setOf("fire")).matches(pokemon, emptyMap()))
    }

    @Test fun rangeIsInclusiveAndDefaultRangeDoesNotExcludeIdsOver1000() {
        assertTrue(PokemonFilters(minimumId = 1, maximumId = 1).matches(pokemon, chart))
        assertFalse(PokemonFilters(minimumId = 2).matches(pokemon, chart))
        assertTrue(PokemonFilters().matches(pokemon.copy(id = 10025), chart))
    }

    @Test fun sizeBoundariesAreExclusiveAtNextBucketAndUnknownValuesDoNotMatch() {
        for ((height, bucket) in listOf(9 to PokemonHeight.SHORT, 10 to PokemonHeight.MEDIUM,
            19 to PokemonHeight.MEDIUM, 20 to PokemonHeight.TALL)) {
            assertTrue(PokemonFilters(heights = setOf(bucket)).matches(pokemon.copy(heightDecimetres = height), chart))
        }
        for ((weight, bucket) in listOf(99 to PokemonWeight.LIGHT, 100 to PokemonWeight.MEDIUM,
            999 to PokemonWeight.MEDIUM, 1000 to PokemonWeight.HEAVY)) {
            assertTrue(PokemonFilters(weights = setOf(bucket)).matches(pokemon.copy(weightHectograms = weight), chart))
        }
        assertFalse(PokemonFilters(heights = setOf(PokemonHeight.SHORT)).matches(pokemon.copy(heightDecimetres = null), chart))
    }

    @Test fun rejectsMoreThanThreeSelectionsAndInvalidRanges() {
        for (create in listOf<() -> PokemonFilters>(
            { PokemonFilters(types = setOf("grass", "fire", "ice", "water")) },
            { PokemonFilters(weaknesses = setOf("grass", "fire", "ice", "water")) },
            { PokemonFilters(minimumId = 0) }, { PokemonFilters(minimumId = 2, maximumId = 1) },
        )) {
            try { create(); fail("Expected invalid filters to be rejected") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(3, PokemonFilters(heights = PokemonHeight.entries.toSet(),
            weights = PokemonWeight.entries.toSet()).heights.size)
    }
}
