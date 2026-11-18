package com.cristianalmendro.pokeapi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonFilters
import com.cristianalmendro.pokeapi.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

data class HomeUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val hasError: Boolean = false,
    val filters: PokemonFilters = PokemonFilters(),
    val draftFilters: PokemonFilters = PokemonFilters(),
    val showFilters: Boolean = false,
    val isPreparingFilters: Boolean = false,
    val filterError: Boolean = false,
    val filterMaximumId: Int = 1000,
    val cachedCount: Int = 0,
)

class HomeViewModel(private val repository: PokemonRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()
    private var nextOffset = 0
    private var unfilteredPokemon: List<Pokemon> = emptyList()
    private var unfilteredHasMore = true
    private var unfilteredHasError = false
    private var filterJob: Job? = null

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        val current = state.value
        if (current.showFilters || current.filters.isActive || current.isLoading || current.hasError || !current.hasMore) return
        loadPage()
    }

    fun retry() {
        if (!state.value.filters.isActive && state.value.hasError && !state.value.isLoading) loadPage()
    }

    fun openFilters() {
        if (state.value.showFilters) return
        mutableState.value = state.value.copy(showFilters = true, draftFilters = state.value.filters,
            filterError = false, isPreparingFilters = true)
        filterJob = viewModelScope.launch {
            try {
                val cache = repository.getFilteredCache(PokemonFilters())
                mutableState.value = state.value.copy(filterMaximumId = cache.maximumId,
                    cachedCount = cache.cachedCount, isPreparingFilters = false)
            } catch (_: IOException) {
                mutableState.value = state.value.copy(filterError = true, isPreparingFilters = false)
            }
        }
    }

    fun dismissFilters() {
        filterJob?.cancel()
        mutableState.value = state.value.copy(showFilters = false, isPreparingFilters = false)
    }

    fun updateDraft(filters: PokemonFilters) {
        if (!state.value.isPreparingFilters) {
            mutableState.value = state.value.copy(draftFilters = filters, filterError = false)
        }
    }

    fun resetDraft() = updateDraft(PokemonFilters())

    fun applyFilters() {
        val current = state.value
        if (current.isLoading || current.isPreparingFilters || !current.showFilters) return
        val filters = current.draftFilters
        if (!filters.isActive) {
            mutableState.value = current.copy(filters = filters, showFilters = false, filterError = false,
                pokemon = unfilteredPokemon, hasMore = unfilteredHasMore, hasError = unfilteredHasError)
            return
        }
        mutableState.value = current.copy(isPreparingFilters = true, filterError = false)
        filterJob = viewModelScope.launch {
            try {
                val cache = repository.getFilteredCache(filters)
                mutableState.value = state.value.copy(pokemon = cache.pokemon, filters = filters,
                    hasMore = false, hasError = false, showFilters = false, isPreparingFilters = false,
                    cachedCount = cache.cachedCount, filterMaximumId = cache.maximumId)
            } catch (_: IOException) {
                mutableState.value = state.value.copy(isPreparingFilters = false, filterError = true)
            }
        }
    }

    private fun loadPage() {
        unfilteredHasError = false
        mutableState.value = state.value.copy(isLoading = true, hasError = false)
        viewModelScope.launch {
            try {
                val page = repository.getPage(nextOffset, PAGE_SIZE)
                val combined = (unfilteredPokemon + page.items).distinctBy { it.id }
                if (page.nextOffset != null &&
                    (page.nextOffset <= nextOffset || combined.size == unfilteredPokemon.size)) {
                    throw IOException("Pagination made no progress")
                }
                page.nextOffset?.let { nextOffset = it }
                unfilteredPokemon = combined
                unfilteredHasMore = page.nextOffset != null
                mutableState.value = state.value.copy(
                    isLoading = false, hasError = false,
                    filterMaximumId = maxOf(state.value.filterMaximumId, combined.maxOfOrNull { it.id } ?: 1),
                    pokemon = combined,
                    hasMore = page.nextOffset != null,
                )
            } catch (_: IOException) {
                unfilteredHasError = true
                mutableState.value = state.value.copy(isLoading = false, hasError = true)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        fun factory(repository: PokemonRepository) = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
