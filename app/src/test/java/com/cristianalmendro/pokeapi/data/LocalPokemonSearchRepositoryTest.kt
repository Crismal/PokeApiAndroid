package com.cristianalmendro.pokeapi.data

import com.cristianalmendro.pokeapi.data.search.*
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LocalPokemonSearchRepositoryTest {
    @Test fun buildsCompleteIndexWithoutDownloadingAnyDetailsAndReusesIt() = runTest {
        val local = FakeIndex()
        val offsets = mutableListOf<Int>()
        val api = object : PokemonApi {
            override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences {
                assertEquals(200, limit)
                offsets += offset
                return if (offset == 0) PokemonReferences(listOf(1), 200, listOf("bulbasaur"))
                else PokemonReferences(listOf(25), null, listOf("pikachu"))
            }
            override suspend fun getPokemon(id: Int): Pokemon = error("Index must not fetch details")
        }
        val details = object : PokemonDetailsRepository {
            override suspend fun getPokemon(id: Int): Pokemon = error("No details expected")
        }
        LocalPokemonSearchRepository(api, local, details).ensureIndex()
        LocalPokemonSearchRepository(api, local, details).ensureIndex()
        assertEquals(listOf(0, 200), offsets)
        assertEquals(listOf(1, 25), local.entries.map { it.id })
        assertEquals(1, local.replacements)
    }

    @Test fun failedCatalogueIsNotPublishedAndRetryCanCompleteIt() = runTest {
        val local = FakeIndex()
        var fail = true
        val api = object : PokemonApi {
            override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences {
                if (offset == 200 && fail) throw IOException("offline")
                return if (offset == 0) PokemonReferences(listOf(1), 200, listOf("bulbasaur"))
                else PokemonReferences(listOf(25), null, listOf("pikachu"))
            }
            override suspend fun getPokemon(id: Int): Pokemon = error("No details expected")
        }
        val repository = LocalPokemonSearchRepository(api, local, noDetails)
        try {
            repository.ensureIndex()
            fail("Expected failed catalogue")
        } catch (_: IOException) { }
        assertFalse(local.hasCompleteIndex())
        assertEquals(0, local.replacements)
        fail = false
        repository.ensureIndex()
        assertTrue(local.hasCompleteIndex())
    }

    @Test fun localSearchFetchesOneExtraRowForPaginationWithoutApiCalls() = runTest {
        val local = FakeIndex().apply {
            entries = (1..21).map { PokemonSearchEntry(it, "pokemon-$it") }
        }
        val api = object : PokemonApi {
            override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences = error("No network")
            override suspend fun getPokemon(id: Int): Pokemon = error("No network")
        }
        val repository = LocalPokemonSearchRepository(api, local, noDetails)
        assertEquals(PokemonSearchPage(local.entries.take(20), true), repository.search("pokemon", 0, 20))
        assertEquals(PokemonSearchPage(local.entries.takeLast(1), false), repository.search("pokemon", 20, 20))
    }

    private val noDetails = object : PokemonDetailsRepository {
        override suspend fun getPokemon(id: Int): Pokemon = error("No details expected")
    }

    private class FakeIndex : PokemonSearchLocalDataSource {
        var entries = emptyList<PokemonSearchEntry>()
        var replacements = 0
        override suspend fun hasCompleteIndex() = replacements > 0
        override suspend fun replaceIndex(entries: List<PokemonSearchEntry>) {
            this.entries = entries
            replacements++
        }
        override suspend fun search(query: String, offset: Int, limit: Int) = entries.drop(offset).take(limit)
    }
}
