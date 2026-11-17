package com.cristianalmendro.pokeapi.data

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class PokeApiServiceTest {
    @Test fun extractsActualIdsAndServerCursorRatherThanUsingRowPositions() = runTest {
        val service = PokeApiService(JsonHttpClient { url ->
            assertEquals("https://pokeapi.co/api/v2/pokemon?limit=20&offset=0", url)
            JSONObject("""{"next":"https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                "results":[{"name":"test","url":"https://pokeapi.co/api/v2/pokemon/99/"},
                {"name":"test2","url":"https://pokeapi.co/api/v2/pokemon/101/"}]}""")
        })
        assertEquals(PokemonReferences(listOf(99, 101), 20, listOf("test", "test2")), service.getReferences(0, 20))
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
