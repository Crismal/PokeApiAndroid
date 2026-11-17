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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cristianalmendro.pokeapi.R
import com.cristianalmendro.pokeapi.data.Pokemon
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeScreen(
    state: HomeUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    searchState: PokemonSearchUiState = PokemonSearchUiState(),
    onQueryChange: (String) -> Unit = {},
    onSearchLoadMore: () -> Unit = {},
    onSearchRetry: () -> Unit = {},
    onLoadDetails: (Int) -> Unit = {},
    onRetryDetails: (Int) -> Unit = {},
) {
    val homeListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val searching = searchState.isActive
    val listState = if (searching) searchListState else homeListState
    val loading = if (searching) searchState.isLoading else state.isLoading
    val hasMore = if (searching) searchState.hasMore else state.hasMore
    val hasError = if (searching) searchState.hasError else state.hasError
    val count = if (searching) searchState.results.size else state.pokemon.size
    val loadMore by rememberUpdatedState(if (searching) onSearchLoadMore else onLoadMore)
    LaunchedEffect(searchState.query) { searchListState.scrollToItem(0) }
    LaunchedEffect(listState, count, loading, hasMore, hasError) {
        if (count > 0 && !loading && hasMore && !hasError) {
            snapshotFlow {
                val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                last >= count - 2
            }.distinctUntilChanged().collect { nearEnd -> if (nearEnd) loadMore() }
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(Color.White).testTag("pokemon-list"),
        contentPadding = PaddingValues(horizontal = HomeDimensions.pagePadding, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") { HomeHeader(searchState.query, onQueryChange) }
        if (searching) {
            items(searchState.results, key = { it.entry.id }) { result ->
                LaunchedEffect(result.entry.id) { onLoadDetails(result.entry.id) }
                Column {
                    PokemonCard(result.pokemon ?: Pokemon(result.entry.id, result.entry.name, emptyList(), null))
                    if (result.detailFailed) {
                        Text(stringResource(R.string.search_details_error), fontSize = 12.sp)
                        TextButton(onClick = { onRetryDetails(result.entry.id) }) {
                            Text(stringResource(R.string.retry))
                        }
                    } else if (result.isLoadingDetails) {
                        Text(stringResource(R.string.search_details_loading), fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(state.pokemon, key = { it.id }) { PokemonCard(it) }
        }
        item(key = "status") {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    loading -> {
                        CircularProgressIndicator(color = colorResource(R.color.pokemon_gray),
                            modifier = Modifier.size(28.dp))
                        val message = when {
                            searching && searchState.isPreparingIndex -> R.string.search_preparing
                            searching -> R.string.search_loading
                            count == 0 -> R.string.home_loading
                            else -> R.string.home_loading_more
                        }
                        Text(stringResource(message))
                    }
                    hasError -> {
                        Text(stringResource(when {
                            searching -> R.string.search_error
                            count == 0 -> R.string.home_error
                            else -> R.string.home_append_error
                        }))
                        Button(onClick = if (searching) onSearchRetry else onRetry) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                    count == 0 -> Text(stringResource(if (searching) R.string.search_empty else R.string.home_empty))
                    !hasMore -> Text(stringResource(if (searching) R.string.search_end else R.string.home_end),
                        color = colorResource(R.color.pokemon_gray))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(query: String, onQueryChange: (String) -> Unit) {
    // Input owns its editing state; asynchronous search results must not rewrite IME edits.
    val input = rememberTextFieldState(initialText = query)
    val queryChanged by rememberUpdatedState(onQueryChange)
    LaunchedEffect(input) {
        snapshotFlow { input.text.toString() }.distinctUntilChanged().collect { queryChanged(it) }
    }
    Box(Modifier.fillMaxWidth().height(244.dp)) {
        AsyncImage(model = R.raw.pokemon_pokeball, contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFFF2F2F2)),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-120).dp).size(300.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.home_title), fontSize = 32.sp,
                fontWeight = FontWeight.Bold, color = colorResource(R.color.pokemon_black))
            Text(stringResource(R.string.home_description), fontSize = 16.sp,
                color = colorResource(R.color.pokemon_gray))
            TextField(
                state = input,
                modifier = Modifier.fillMaxWidth().testTag("pokemon-search"),
                placeholder = { Text(stringResource(R.string.search_hint), fontSize = 14.sp) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                trailingIcon = if (input.text.isNotEmpty()) {
                    { TextButton(onClick = {
                        input.edit { replace(0, length, "") }
                        onQueryChange("")
                    }) { Text(stringResource(R.string.search_clear)) } }
                } else null,
                lineLimits = TextFieldLineLimits.SingleLine, shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF2F2F2), focusedContainerColor = Color(0xFFE2E2E2),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colorResource(R.color.pokemon_black),
                    unfocusedTextColor = colorResource(R.color.pokemon_black),
                ),
            )
        }
    }
}
