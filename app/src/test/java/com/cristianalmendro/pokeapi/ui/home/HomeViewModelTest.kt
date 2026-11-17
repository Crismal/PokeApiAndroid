package com.cristianalmendro.pokeapi.ui.home

import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonPage
import com.cristianalmendro.pokeapi.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun loadsPagesInOrderAndStopsAtEnd() = runTest(dispatcher) {
        val repository = FakeRepository({ page(listOf(1, 2), 20) }, { page(listOf(3), null) })
        val model = HomeViewModel(repository)
        advanceUntilIdle()
        assertEquals(listOf(1, 2), model.state.value.pokemon.map { it.id })
        model.loadNextPage()
        advanceUntilIdle()
        model.loadNextPage()
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), model.state.value.pokemon.map { it.id })
        assertEquals(listOf(0 to 20, 20 to 20), repository.requests)
        assertFalse(model.state.value.hasMore)
    }

    @Test fun preventsDuplicateRequestsWhileLoading() = runTest(dispatcher) {
        val response = CompletableDeferred<PokemonPage>()
        val repository = FakeRepository({ response.await() })
        val model = HomeViewModel(repository)
        runCurrent()
        repeat(5) { model.loadNextPage() }
        runCurrent()
        assertEquals(1, repository.requests.size)
        response.complete(page(listOf(1), null))
        advanceUntilIdle()
    }

    @Test fun appendFailurePreservesRowsAndRetriesSamePageOnlyOnRequest() = runTest(dispatcher) {
        val repository = FakeRepository(
            { page(listOf(1), 20) },
            { throw IOException("offline") },
            { page(listOf(2), null) },
        )
        val model = HomeViewModel(repository)
        advanceUntilIdle()
        model.loadNextPage()
        advanceUntilIdle()
        assertTrue(model.state.value.hasError)
        assertEquals(listOf(1), model.state.value.pokemon.map { it.id })
        model.loadNextPage()
        advanceUntilIdle()
        assertEquals(2, repository.requests.size)
        model.retry()
        advanceUntilIdle()
        assertEquals(listOf(0 to 20, 20 to 20, 20 to 20), repository.requests)
        assertEquals(listOf(1, 2), model.state.value.pokemon.map { it.id })
        assertFalse(model.state.value.hasError)
    }

    @Test fun initialFailureCanBeRetried() = runTest(dispatcher) {
        val repository = FakeRepository({ throw IOException("offline") }, { page(listOf(1), null) })
        val model = HomeViewModel(repository)
        advanceUntilIdle()
        assertTrue(model.state.value.hasError)
        assertTrue(model.state.value.pokemon.isEmpty())
        model.retry()
        advanceUntilIdle()
        assertEquals(listOf(0 to 20, 0 to 20), repository.requests)
        assertFalse(model.state.value.hasError)
    }

    @Test fun overlappingPagesDoNotDuplicateCards() = runTest(dispatcher) {
        val model = HomeViewModel(FakeRepository(
            { page(listOf(1, 2), 20) }, { page(listOf(2, 3), null) },
        ))
        advanceUntilIdle()
        model.loadNextPage()
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), model.state.value.pokemon.map { it.id })
    }

    @Test fun emptyFinalPageShowsEmptyStateAndStops() = runTest(dispatcher) {
        val model = HomeViewModel(FakeRepository({ page(emptyList(), null) }))
        advanceUntilIdle()
        assertTrue(model.state.value.pokemon.isEmpty())
        assertFalse(model.state.value.hasMore)
        assertFalse(model.state.value.hasError)
    }

    @Test fun nonAdvancingCursorShowsErrorInsteadOfLooping() = runTest(dispatcher) {
        val model = HomeViewModel(FakeRepository({ page(listOf(1), 0) }))
        advanceUntilIdle()
        assertTrue(model.state.value.hasError)
        assertTrue(model.state.value.pokemon.isEmpty())
    }

    private fun page(ids: List<Int>, next: Int?) = PokemonPage(
        ids.map { Pokemon(it, "pokemon-$it", listOf("grass"), null) }, next,
    )

    private class FakeRepository(vararg responses: suspend () -> PokemonPage) : PokemonRepository {
        private val responses = ArrayDeque(responses.toList())
        val requests = mutableListOf<Pair<Int, Int>>()
        override suspend fun getPage(offset: Int, limit: Int): PokemonPage {
            requests += offset to limit
            return responses.removeFirst().invoke()
        }
    }
}
