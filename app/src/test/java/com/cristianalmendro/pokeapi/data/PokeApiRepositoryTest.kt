package com.cristianalmendro.pokeapi.data

import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PokeApiRepositoryTest {
    @Test fun preservesOrderAndLimitsDetailConcurrencyAndCachesPage() = runTest {
        var active = 0
        var maximumActive = 0
        var detailCalls = 0
        var pageCalls = 0
        val api = object : PokemonApi {
            override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences {
                pageCalls++
                assertEquals(20, offset)
                assertEquals(20, limit)
                return PokemonReferences((21..40).toList(), 40)
            }
            override suspend fun getPokemon(id: Int): Pokemon {
                detailCalls++
                active++
                maximumActive = maxOf(active, maximumActive)
                delay((41 - id).toLong())
                active--
                return Pokemon(id, "pokemon-$id", emptyList(), null)
            }
        }
        val repository = PokeApiRepository(api)
        val first = repository.getPage(20, 20)
        val cached = repository.getPage(20, 20)
        assertEquals((21..40).toList(), first.items.map { it.id })
        assertEquals(40, first.nextOffset)
        assertEquals(first, cached)
        assertTrue(maximumActive <= 4)
        assertEquals(20, detailCalls)
        assertEquals(1, pageCalls)
    }

    @Test fun failedPageDoesNotAdvanceAndRetainsSuccessfulDetailCache() = runTest {
        val calls = mutableListOf<Int>()
        var fail = true
        val api = object : PokemonApi {
            override suspend fun getReferences(offset: Int, limit: Int) =
                PokemonReferences(listOf(1, 2), null)
            override suspend fun getPokemon(id: Int): Pokemon {
                calls += id
                delay(id.toLong())
                if (id == 2 && fail) throw IOException("detail request failed")
                return Pokemon(id, "pokemon-$id", emptyList(), null)
            }
        }
        val repository = PokeApiRepository(api)
        try {
            repository.getPage(0, 20)
            fail("Expected IOException")
        } catch (_: IOException) { }
        fail = false
        assertEquals(listOf(1, 2), repository.getPage(0, 20).items.map { it.id })
        assertEquals(listOf(1, 2, 2), calls)
    }
}
