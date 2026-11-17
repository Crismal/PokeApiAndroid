package com.cristianalmendro.pokeapi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.mutableStateOf
import com.cristianalmendro.pokeapi.data.search.PokemonSearchEntry
import com.cristianalmendro.pokeapi.ui.home.*
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PokemonSearchScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun textFieldSendsTypedQueries() {
        var query = ""
        val search = mutableStateOf(PokemonSearchUiState())
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(), {}, {}, searchState = search.value, onQueryChange = {
                    query = it
                    search.value = search.value.copy(query = it)
                })
            }
        }
        compose.onNodeWithTag("pokemon-search").performTextInput("pika")
        compose.runOnIdle { assertEquals("pika", query) }
    }

    @Test fun indexOnlyResultShowsNameAndIdAndLoadsOnlyComposedRows() {
        val details = mutableListOf<Int>()
        var homeLoads = 0
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(hasMore = true), { homeLoads++ }, {},
                    searchState = PokemonSearchUiState(query = "pika", results = listOf(result)),
                    onLoadDetails = { details += it })
            }
        }
        compose.onNodeWithText("Pikachu").assertIsDisplayed()
        compose.onNodeWithText("#025").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(listOf(25), details)
            assertEquals(0, homeLoads)
        }
    }

    @Test fun editingIsPreservedWhenExternalSearchStateHasNotCaughtUp() {
        var query = ""
        compose.setContent {
            PokeApiTheme {
                // Keep the external state unchanged to simulate a delayed asynchronous collector.
                HomeScreen(HomeUiState(), {}, {}, onQueryChange = { query = it })
            }
        }
        compose.onNodeWithTag("pokemon-search").performTextInput("pika")
        compose.onNodeWithTag("pokemon-search").performTextInput("chu")
        compose.onNodeWithTag("pokemon-search").assertTextEquals("pikachu")
        compose.runOnIdle { assertEquals("pikachu", query) }
    }

    @Test fun offlineDetailsRetainResultAndOfferRetryAndClear() {
        var retriedId: Int? = null
        var query: String? = null
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(), {}, {},
                    searchState = PokemonSearchUiState(query = "pika", results = listOf(result.copy(detailFailed = true))),
                    onQueryChange = { query = it }, onRetryDetails = { retriedId = it })
            }
        }
        compose.onNodeWithText("Pikachu").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertEquals(25, retriedId) }
        compose.onNodeWithText("Clear search").performClick()
        compose.runOnIdle { assertEquals("", query) }
    }

    @Test fun incompleteCatalogueFailureDoesNotClaimNoMatches() {
        compose.setContent {
            PokeApiTheme {
                HomeScreen(HomeUiState(), {}, {}, searchState = PokemonSearchUiState(query = "pika", hasError = true))
            }
        }
        compose.onNodeWithText("Retry").assertIsDisplayed()
        compose.onNodeWithText("No matching Pokémon.").assertDoesNotExist()
    }

    private val result = PokemonSearchResult(PokemonSearchEntry(25, "pikachu"))
}
