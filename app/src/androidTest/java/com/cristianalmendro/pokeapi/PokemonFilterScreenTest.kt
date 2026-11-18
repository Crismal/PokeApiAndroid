package com.cristianalmendro.pokeapi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cristianalmendro.pokeapi.data.PokemonFilters
import com.cristianalmendro.pokeapi.ui.home.HomeScreen
import com.cristianalmendro.pokeapi.ui.home.HomeUiState
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PokemonFilterScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun typeAndWeaknessSelectionsAreIndependentAndLimitedToThree() {
        var state by mutableStateOf(HomeUiState(showFilters = true, hasMore = false))
        compose.setContent {
            PokeApiTheme { HomeScreen(state, {}, {}, onDraftChange = { state = state.copy(draftFilters = it) }) }
        }
        compose.onNodeWithTag("filters-sheet").performTouchInput { swipeUp() }
        for (type in listOf("bug", "dark", "dragon")) compose.onNodeWithTag("type-$type").performClick()
        compose.onNodeWithTag("type-electric").assertIsNotEnabled()
        compose.onNodeWithTag("weakness-electric").performClick()
        compose.onNodeWithTag("type-dark").performClick()
        compose.onNodeWithTag("type-electric").assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals(setOf("bug", "dragon", "electric"), state.draftFilters.types)
            assertEquals(setOf("electric"), state.draftFilters.weaknesses)
        }
    }

    @Test fun resetChangesDraftAndApplyUsesIt() {
        var state by mutableStateOf(HomeUiState(showFilters = true, hasMore = false,
            draftFilters = PokemonFilters(types = setOf("grass"))))
        var applied: PokemonFilters? = null
        compose.setContent {
            PokeApiTheme { HomeScreen(state, {}, {},
                onDraftChange = { state = state.copy(draftFilters = it) },
                onResetFilters = { state = state.copy(draftFilters = PokemonFilters()) },
                onApplyFilters = { applied = state.draftFilters }) }
        }
        compose.onNodeWithTag("filters-sheet").performTouchInput { swipeUp() }
        compose.onNodeWithText("Reset").performClick()
        compose.onNodeWithTag("apply-filters").performClick()
        compose.runOnIdle { assertEquals(PokemonFilters(), applied) }
    }

    @Test fun filtersOpenFromHomeAndEmptyCacheResultsHaveAnActionableMessage() {
        var opened = false
        compose.setContent {
            PokeApiTheme { HomeScreen(HomeUiState(hasMore = false,
                filters = PokemonFilters(types = setOf("fire"))), {}, {}, onOpenFilters = { opened = true }) }
        }
        compose.onNodeWithText("No saved Pokémon match these filters. Edit or reset your filters.").assertIsDisplayed()
        compose.onNodeWithTag("open-filters").performClick()
        compose.runOnIdle { assertTrue(opened) }
    }
}
