package com.cristianalmendro.pokeapi.ui.home

import com.cristianalmendro.pokeapi.data.*
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeFiltersViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun draftsApplyOnlyOnRequestAndResetRestoresPagination() = runTest(dispatcher) {
        val repo = FilterRepository()
        val model = HomeViewModel(repo)
        advanceUntilIdle()
        model.openFilters()
        advanceUntilIdle()
        assertEquals(10025, model.state.value.filterMaximumId)
        val grass = PokemonFilters(types = setOf("grass"))
        model.updateDraft(grass)
        assertFalse(model.state.value.filters.isActive)
        model.dismissFilters()
        model.openFilters()
        advanceUntilIdle()
        assertEquals(PokemonFilters(), model.state.value.draftFilters)
        model.updateDraft(grass)
        model.applyFilters()
        advanceUntilIdle()
        assertEquals(listOf(3), model.state.value.pokemon.map { it.id })
        assertFalse(model.state.value.showFilters)
        model.loadNextPage()
        advanceUntilIdle()
        assertEquals(1, repo.pageRequests)
        model.openFilters()
        advanceUntilIdle()
        model.resetDraft()
        assertEquals(grass, model.state.value.filters)
        model.applyFilters()
        assertEquals(listOf(1), model.state.value.pokemon.map { it.id })
        model.loadNextPage()
        advanceUntilIdle()
        assertEquals(2, repo.pageRequests)
        assertEquals(listOf(1, 2), model.state.value.pokemon.map { it.id })
    }

    @Test fun failedApplyKeepsExistingResultsAndDraftForRetry() = runTest(dispatcher) {
        val repo = FilterRepository()
        val model = HomeViewModel(repo)
        advanceUntilIdle()
        model.openFilters()
        advanceUntilIdle()
        model.updateDraft(PokemonFilters(weaknesses = setOf("fire")))
        repo.failFilters = true
        model.applyFilters()
        advanceUntilIdle()
        assertTrue(model.state.value.showFilters)
        assertTrue(model.state.value.filterError)
        assertFalse(model.state.value.filters.isActive)
        assertEquals(listOf(1), model.state.value.pokemon.map { it.id })
        repo.failFilters = false
        model.applyFilters()
        advanceUntilIdle()
        assertFalse(model.state.value.filterError)
        assertTrue(model.state.value.filters.isActive)
    }

    @Test fun dismissDuringPreparationCancelsWithoutApplyingLateResults() = runTest(dispatcher) {
        val repo = FilterRepository()
        val model = HomeViewModel(repo)
        advanceUntilIdle()
        model.openFilters()
        advanceUntilIdle()
        model.updateDraft(PokemonFilters(types = setOf("grass")))
        repo.pending = CompletableDeferred()
        model.applyFilters()
        runCurrent()
        model.dismissFilters()
        repo.pending?.complete(Unit)
        advanceUntilIdle()
        assertFalse(model.state.value.showFilters)
        assertFalse(model.state.value.isPreparingFilters)
        assertFalse(model.state.value.filters.isActive)
        assertEquals(listOf(1), model.state.value.pokemon.map { it.id })
    }

    private class FilterRepository : PokemonRepository {
        var pageRequests = 0
        var failFilters = false
        var pending: CompletableDeferred<Unit>? = null
        override suspend fun getPage(offset: Int, limit: Int): PokemonPage {
            pageRequests++
            return PokemonPage(listOf(Pokemon(pageRequests, "pokemon", emptyList(), null)), offset + 20)
        }
        override suspend fun getFilteredCache(filters: PokemonFilters): CachedPokemonResult {
            pending?.await()
            if (failFilters) throw IOException("offline")
            return CachedPokemonResult(listOf(Pokemon(3, "saved", listOf("grass"), null)), 20, 10025)
        }
    }
}
