package com.cristianalmendro.pokeapi.ui.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristianalmendro.pokeapi.data.PokeApiRepository
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.data.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PokemonListState {
    data object Loading : PokemonListState
    data class Success(val pokemon: List<Pokemon>) : PokemonListState
    data object Error : PokemonListState
}

class PokemonListViewModel(
    private val repository: PokemonRepository = PokeApiRepository(),
) : ViewModel() {
    private val mutableState = MutableStateFlow<PokemonListState>(PokemonListState.Loading)
    val state = mutableState.asStateFlow()

    init {
        loadPokemon()
    }

    fun retry() {
        if (state.value !is PokemonListState.Loading) loadPokemon()
    }

    private fun loadPokemon() {
        mutableState.value = PokemonListState.Loading
        viewModelScope.launch {
            try {
                mutableState.value = PokemonListState.Success(repository.getFirstTwentyPokemon())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = PokemonListState.Error
            }
        }
    }
}
