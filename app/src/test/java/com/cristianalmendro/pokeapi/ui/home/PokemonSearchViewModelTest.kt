package com.cristianalmendro.pokeapi.ui.home

import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.search.*
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonSearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun debouncesTypingAndNeverFetchesDetailsUntilRowIsRequested() = runTest(dispatcher) {
        val repository = FakeSearch()
        val model = PokemonSearchViewModel(repository)
        model.onQueryChange("p")
        advanceTimeBy(100)
        model.onQueryChange("pi")
        advanceTimeBy(100)
        model.onQueryChange("pika")
        advanceUntilIdle()
        assertEquals(listOf("pika" to 0), repository.searches)
        assertEquals(0, repository.detailCalls)
        assertEquals("pika", model.state.value.query)
        model.loadDetails(25)
        advanceUntilIdle()
        assertEquals(1, repository.detailCalls)
        assertNotNull(model.state.value.results.single().pokemon)
    }

    @Test fun queryChangesShareCatalogueDownloadAndClearingDoesNotDisplayStaleResults() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeSearch().apply { indexGate = gate }
        val model = PokemonSearchViewModel(repository)
        model.onQueryChange("pika")
        advanceTimeBy(300)
        runCurrent()
        model.onQueryChange("bulba")
        advanceTimeBy(300)
        runCurrent()
        assertEquals(1, repository.indexCalls)
        model.onQueryChange("")
        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(model.state.value.isActive)
        assertTrue(model.state.value.results.isEmpty())
        assertTrue(repository.searches.isEmpty())
    }

    @Test fun indexFailureOffersRetryAndCanRecover() = runTest(dispatcher) {
        val repository = FakeSearch().apply { failIndex = true }
        val model = PokemonSearchViewModel(repository)
        model.onQueryChange("pika")
        advanceUntilIdle()
        assertTrue(model.state.value.hasError)
        repository.failIndex = false
        model.retry()
        advanceUntilIdle()
        assertEquals(2, repository.indexCalls)
        assertFalse(model.state.value.hasError)
        assertEquals(25, model.state.value.results.single().entry.id)
    }

    @Test fun uncachedOfflineDetailsKeepSearchableNamesAndAllowExplicitRetry() = runTest(dispatcher) {
        val repository = FakeSearch().apply { failDetails = true }
        val model = PokemonSearchViewModel(repository)
        model.onQueryChange("pika")
        advanceUntilIdle()
        model.loadDetails(25)
        advanceUntilIdle()
        assertEquals("pikachu", model.state.value.results.single().entry.name)
        assertTrue(model.state.value.results.single().detailFailed)
        assertFalse(model.state.value.hasError)
        model.loadDetails(25)
        advanceUntilIdle()
        assertEquals(1, repository.detailCalls)
        repository.failDetails = false
        model.loadDetails(25, retry = true)
        advanceUntilIdle()
        assertFalse(model.state.value.results.single().detailFailed)
        assertNotNull(model.state.value.results.single().pokemon)
    }

    @Test fun paginatesMatchesWithoutDuplicateRequestsAndPreservesRowsOnFailure() = runTest(dispatcher) {
        val repository = FakeSearch().apply { multiplePages = true }
        val model = PokemonSearchViewModel(repository)
        model.onQueryChange("pika")
        advanceUntilIdle()
        repository.failSearch = true
        model.loadNextPage()
        model.loadNextPage()
        advanceUntilIdle()
        assertTrue(model.state.value.hasError)
        assertEquals(1, model.state.value.results.size)
        repository.failSearch = false
        model.retry()
        advanceUntilIdle()
        assertEquals(listOf("pika" to 0, "pika" to 1, "pika" to 1), repository.searches)
        assertEquals(listOf(25, 26), model.state.value.results.map { it.entry.id })
        assertFalse(model.state.value.hasMore)
    }

    private class FakeSearch : PokemonSearchRepository {
        val searches = mutableListOf<Pair<String, Int>>()
        var indexCalls = 0
        var detailCalls = 0
        var indexGate: CompletableDeferred<Unit>? = null
        var failIndex = false
        var failDetails = false
        var failSearch = false
        var multiplePages = false
        override suspend fun ensureIndex() {
            indexCalls++
            indexGate?.await()
            if (failIndex) throw IOException("offline")
        }
        override suspend fun search(query: String, offset: Int, limit: Int): PokemonSearchPage {
            searches += query to offset
            if (failSearch) throw IOException("storage unavailable")
            return PokemonSearchPage(listOf(PokemonSearchEntry(if (offset == 0) 25 else 26, "pikachu")),
                multiplePages && offset == 0)
        }
        override suspend fun getPokemon(id: Int): Pokemon {
            detailCalls++
            if (failDetails) throw IOException("offline")
            return Pokemon(id, "pikachu", listOf("electric"), null)
        }
    }
}
