package com.cristianalmendro.pokeapi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.cristianalmendro.pokeapi.data.Pokemon
import com.cristianalmendro.pokeapi.ui.home.HomeScreen
import com.cristianalmendro.pokeapi.ui.home.HomeUiState
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cardsShowIdsNamesAndTypesWithoutSearch() {
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(pokemon = listOf(bulbasaur), hasMore = false), {}, {})
            }
        }
        compose.onNodeWithText("Pokédex").assertIsDisplayed()
        compose.onNodeWithText("#001").assertIsDisplayed()
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("Grass").assertIsDisplayed()
        compose.onNodeWithText("Poison").assertIsDisplayed()
        compose.onNodeWithText("What Pokémon are you looking for?").assertDoesNotExist()
    }

    @Test fun scrollingNearEndRequestsNextPage() {
        var requests = 0
        val items = (1..20).map { bulbasaur.copy(id = it, name = "pokemon-$it") }
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(pokemon = items), { requests++ }, {})
            }
        }
        compose.runOnIdle { assertEquals(0, requests) }
        compose.onNodeWithTag("pokemon-list").performScrollToIndex(19)
        compose.waitUntil { requests > 0 }
    }

    @Test fun appendFailureKeepsCardsAndOffersRetryWithoutAutoLoading() {
        var retried = false
        var automaticLoads = 0
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(pokemon = listOf(bulbasaur), hasError = true),
                    { automaticLoads++ }, { retried = true })
            }
        }
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle {
            assertTrue(retried)
            assertEquals(0, automaticLoads)
        }
    }

    @Test fun finalPageDoesNotLoadAgain() {
        var requests = 0
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(pokemon = listOf(bulbasaur), hasMore = false),
                    { requests++ }, {})
            }
        }
        compose.onNodeWithText("You have reached the end of the Pokédex.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, requests) }
    }

    @Test fun emptyStateCanBeRendered() {
        compose.setContent {
            PokeApiTheme { HomeScreen(HomeUiState(hasMore = false), {}, {}) }
        }
        compose.onNodeWithText("No Pokémon found.").assertIsDisplayed()
    }

    @Test fun loadingStateCanBeRendered() {
        compose.setContent {
            PokeApiTheme { HomeScreen(HomeUiState(isLoading = true), {}, {}) }
        }
        compose.onNodeWithText("Loading Pokémon…").assertIsDisplayed()
    }

    @Test fun initialFailureOffersRetry() {
        var retried = false
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(hasError = true), {}, { retried = true })
            }
        }
        compose.onNodeWithText("Could not load Pokémon. Please try again.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertTrue(retried) }
    }

    private val bulbasaur = Pokemon(1, "bulbasaur", listOf("grass", "poison"), null)
}
