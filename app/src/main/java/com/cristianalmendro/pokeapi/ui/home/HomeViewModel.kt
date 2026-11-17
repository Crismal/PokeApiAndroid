package com.cristianalmendro.pokeapi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val pokemon: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val hasError: Boolean = false,
)

class HomeViewModel(private val repository: PokemonRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()
    private var nextOffset = 0

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        val current = state.value
        if (current.isLoading || current.hasError || !current.hasMore) return
        loadPage()
    }

    fun retry() {
        if (state.value.hasError && !state.value.isLoading) loadPage()
    }

    private fun loadPage() {
        mutableState.value = state.value.copy(isLoading = true, hasError = false)
        viewModelScope.launch {
            try {
                val page = repository.getPage(nextOffset, PAGE_SIZE)
                val combined = (state.value.pokemon + page.items).distinctBy { it.id }
                if (page.nextOffset != null &&
                    (page.nextOffset <= nextOffset || combined.size == state.value.pokemon.size)) {
                    throw IOException("Pagination made no progress")
                }
                page.nextOffset?.let { nextOffset = it }
                mutableState.value = HomeUiState(
                    pokemon = combined,
                    hasMore = page.nextOffset != null,
                )
            } catch (_: IOException) {
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
