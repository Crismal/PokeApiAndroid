package com.cristianalmendro.pokeapi.data

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class PokeApiServiceTest {
    @Test fun parsesSizeUnitsAndIncomingTypeDefenses() = runTest {
        val service = PokeApiService(JsonHttpClient { url ->
            if (url.contains("type/grass/")) JSONObject("""{"name":"grass","damage_relations":{
                "double_damage_from":[{"name":"fire"}],"half_damage_from":[{"name":"water"}],"no_damage_from":[]}}""")
            else JSONObject("""{"id":1,"name":"bulbasaur","height":7,"weight":69,"types":[],"sprites":{}}""")
        })
        assertEquals(7, service.getPokemon(1).heightDecimetres)
        assertEquals(69, service.getPokemon(1).weightHectograms)
        assertEquals(2.0, service.getTypeDefenses("grass").multiplier("fire"), 0.0)
        assertEquals(0.5, service.getTypeDefenses("grass").multiplier("water"), 0.0)
    }
    @Test fun extractsActualIdsAndServerCursorRatherThanUsingRowPositions() = runTest {
        val service = PokeApiService(JsonHttpClient { url ->
            assertEquals("https://pokeapi.co/api/v2/pokemon?limit=20&offset=0", url)
            JSONObject("""{"next":"https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                "results":[{"name":"test","url":"https://pokeapi.co/api/v2/pokemon/99/"},
                {"name":"test2","url":"https://pokeapi.co/api/v2/pokemon/101/"}]}""")
        })
        assertEquals(PokemonReferences(listOf(99, 101), 20), service.getReferences(0, 20))
    }

    @Test fun rejectsUntrustedPaginationUrl() = runTest {
        val service = PokeApiService(JsonHttpClient {
            JSONObject("""{"next":"https://example.com/api/v2/pokemon?offset=20","results":[]}""")
        })
        try {
            service.getReferences(0, 20)
            fail("Expected IOException")
        } catch (_: IOException) { }
    }

    @Test fun mapsTypesBySlotAndFallsBackToSprite() = runTest {
        val service = PokeApiService(JsonHttpClient {
            JSONObject("""{"id":1,"name":"bulbasaur","types":[
                {"slot":2,"type":{"name":"poison"}}, {"slot":1,"type":{"name":"grass"}}],
                "sprites":{"front_default":"https://example.com/sprite.png",
                "other":{"official-artwork":{"front_default":null}}}}""")
        })
        val pokemon = service.getPokemon(1)
        assertEquals(listOf("grass", "poison"), pokemon.types)
        assertEquals("https://example.com/sprite.png", pokemon.artworkUrl)
    }

    @Test fun missingArtworkRemainsNullRatherThanStringNull() = runTest {
        val service = PokeApiService(JsonHttpClient {
            JSONObject("""{"id":1,"name":"bulbasaur","types":[],"sprites":{"front_default":null}}""")
        })
        assertNull(service.getPokemon(1).artworkUrl)
    }

    @Test fun malformedResponseBecomesRecoverableIoError() = runTest {
        val service = PokeApiService(JsonHttpClient { JSONObject("""{"id":1}""") })
        try {
            service.getPokemon(1)
            fail("Expected IOException")
        } catch (_: IOException) { }
    }
}
