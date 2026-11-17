package com.cristianalmendro.pokeapi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.ui.pokemon.PokemonListScreen
import com.cristianalmendro.pokeapi.ui.pokemon.PokemonListState
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonListScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listDisplaysIdsAndNamesAndScrollsToLastPokemon() {
        val pokemon = (1..20).map { Pokemon(it, "pokemon-$it") }
        compose.setContent {
            PokeApiTheme {
                PokemonListScreen(PokemonListState.Success(pokemon), onRetry = {})
            }
        }
        compose.onNodeWithText("1 — pokemon-1").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(19)
        compose.onNodeWithText("20 — pokemon-20").assertIsDisplayed()
    }

    @Test
    fun errorOffersRetry() {
        var retried = false
        compose.setContent {
            PokemonListScreen(PokemonListState.Error, onRetry = { retried = true })
        }
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertTrue(retried) }
    }

    @Test
    fun emptyResponseShowsMessage() {
        compose.setContent {
            PokemonListScreen(PokemonListState.Success(emptyList()), onRetry = {})
        }
        compose.onNodeWithText("No Pokémon found.").assertIsDisplayed()
    }

    @Test
    fun loadingShowsMessage() {
        compose.setContent {
            PokemonListScreen(PokemonListState.Loading, onRetry = {})
        }
        compose.onNodeWithText("Loading Pokémon…").assertIsDisplayed()
    }
}
