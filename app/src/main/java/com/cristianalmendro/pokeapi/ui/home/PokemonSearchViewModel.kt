package com.cristianalmendro.pokeapi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.search.PokemonSearchEntry
import com.cristianalmendro.pokeapi.data.search.PokemonSearchRepository
import java.io.IOException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PokemonSearchResult(
    val entry: PokemonSearchEntry,
    val pokemon: Pokemon? = null,
    val isLoadingDetails: Boolean = false,
    val detailFailed: Boolean = false,
)

data class PokemonSearchUiState(
    val query: String = "",
    val results: List<PokemonSearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isPreparingIndex: Boolean = false,
    val hasMore: Boolean = false,
    val hasError: Boolean = false,
) {
    val isActive: Boolean get() = query.isNotBlank()
}

class PokemonSearchViewModel(private val repository: PokemonSearchRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PokemonSearchUiState())
    val state = mutableState.asStateFlow()
    private var searchJob: Job? = null
    private var indexTask: Deferred<Result<Unit>>? = null
    private val detailJobs = mutableMapOf<Int, Job>()
    private var generation = 0
    private var nextOffset = 0

    fun onQueryChange(query: String) {
        if (query == state.value.query) return
        startSearch(query, debounce = true)
    }

    private fun startSearch(query: String, debounce: Boolean) {
        val token = ++generation
        searchJob?.cancel()
        detailJobs.values.forEach { it.cancel() }
        detailJobs.clear()
        nextOffset = 0
        mutableState.value = PokemonSearchUiState(query = query, isLoading = query.isNotBlank())
        if (query.isBlank()) return
        searchJob = viewModelScope.launch {
            if (debounce) delay(300)
            try {
                mutableState.value = state.value.copy(isPreparingIndex = true)
                ensureIndex()
                if (token != generation) return@launch
                mutableState.value = state.value.copy(isPreparingIndex = false)
                fetchPage(query, token)
            } catch (_: IOException) {
                if (token == generation) mutableState.value = state.value.copy(
                    isLoading = false, isPreparingIndex = false, hasError = true,
                )
            }
        }
    }

    // A changed/cleared query cancels its lookup, not the shared catalogue download.
    private suspend fun ensureIndex() {
        val task = indexTask ?: viewModelScope.async {
            try {
                repository.ensureIndex()
                Result.success(Unit)
            } catch (error: IOException) {
                Result.failure(error)
            }
        }.also { indexTask = it }
        val result = task.await()
        if (result.isFailure && indexTask === task) indexTask = null
        result.getOrThrow()
    }

    fun loadNextPage() {
        if (!state.value.isActive || state.value.isLoading || state.value.hasError || !state.value.hasMore) return
        loadPage()
    }

    fun retry() {
        if (!state.value.hasError || state.value.isLoading) return
        if (state.value.results.isEmpty()) startSearch(state.value.query, debounce = false)
        else loadPage()
    }

    private fun loadPage() {
        val token = generation
        val query = state.value.query
        mutableState.value = state.value.copy(isLoading = true, hasError = false)
        searchJob = viewModelScope.launch {
            try {
                fetchPage(query, token)
            } catch (_: IOException) {
                if (token == generation) mutableState.value = state.value.copy(isLoading = false, hasError = true)
            }
        }
    }

    private suspend fun fetchPage(query: String, token: Int) {
        val page = repository.search(query, nextOffset, 20)
        if (token != generation) return
        nextOffset += page.entries.size
        mutableState.value = state.value.copy(
            results = (state.value.results + page.entries.map { PokemonSearchResult(it) })
                .distinctBy { it.entry.id },
            isLoading = false, hasMore = page.hasMore, hasError = false,
        )
    }

    fun loadDetails(id: Int, retry: Boolean = false) {
        val row = state.value.results.firstOrNull { it.entry.id == id } ?: return
        if (row.pokemon != null || row.isLoadingDetails || (row.detailFailed && !retry)) return
        val token = generation
        updateRow(id) { it.copy(isLoadingDetails = true, detailFailed = false) }
        detailJobs[id] = viewModelScope.launch {
            try {
                val pokemon = repository.getPokemon(id)
                if (token == generation) updateRow(id) { it.copy(pokemon = pokemon, isLoadingDetails = false) }
            } catch (_: IOException) {
                if (token == generation) updateRow(id) { it.copy(isLoadingDetails = false, detailFailed = true) }
            } finally {
                if (token == generation) detailJobs.remove(id)
            }
        }
    }

    private fun updateRow(id: Int, transform: (PokemonSearchResult) -> PokemonSearchResult) {
        mutableState.value = state.value.copy(results = state.value.results.map {
            if (it.entry.id == id) transform(it) else it
        })
    }

    companion object {
        fun factory(repository: PokemonSearchRepository) = viewModelFactory {
            initializer { PokemonSearchViewModel(repository) }
        }
    }
}
