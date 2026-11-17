package com.cristianalmendro.pokeapi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cristianalmendro.pokeapi.R
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeScreen(
    state: HomeUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val loadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, state.pokemon.size, state.isLoading, state.hasMore, state.hasError) {
        if (state.pokemon.isNotEmpty() && !state.isLoading && state.hasMore && !state.hasError) {
            snapshotFlow {
                val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                // One header precedes the cards. Prefetch when the last three cards are visible.
                last >= state.pokemon.size - 2
            }.distinctUntilChanged().collect { nearEnd -> if (nearEnd) loadMore() }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(Color.White).testTag("pokemon-list"),
        contentPadding = PaddingValues(horizontal = HomeDimensions.pagePadding, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") { HomeHeader() }
        items(state.pokemon, key = { it.id }) { PokemonCard(it) }
        item(key = "status") {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(color = colorResource(R.color.pokemon_gray),
                            modifier = Modifier.size(28.dp))
                        Text(stringResource(if (state.pokemon.isEmpty()) R.string.home_loading
                            else R.string.home_loading_more))
                    }
                    state.hasError -> {
                        Text(stringResource(if (state.pokemon.isEmpty()) R.string.home_error
                            else R.string.home_append_error))
                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                    state.pokemon.isEmpty() -> Text(stringResource(R.string.home_empty))
                    !state.hasMore -> Text(stringResource(R.string.home_end),
                        color = colorResource(R.color.pokemon_gray))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Box(Modifier.fillMaxWidth().height(156.dp)) {
        AsyncImage(model = R.raw.pokemon_pokeball, contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFFF2F2F2)),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-120).dp).size(300.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.home_title), fontSize = 32.sp,
                fontWeight = FontWeight.Bold, color = colorResource(R.color.pokemon_black))
            Text(stringResource(R.string.home_description), fontSize = 16.sp,
                color = colorResource(R.color.pokemon_gray))
        }
    }
}
