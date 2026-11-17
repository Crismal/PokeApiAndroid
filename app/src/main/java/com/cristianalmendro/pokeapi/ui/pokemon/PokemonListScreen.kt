package com.cristianalmendro.pokeapi.ui.pokemon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cristianalmendro.pokeapi.R

@Composable
fun PokemonListScreen(
    state: PokemonListState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        PokemonListState.Loading -> Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(stringResource(R.string.pokemon_loading))
        }
        PokemonListState.Error -> Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.pokemon_load_error))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        is PokemonListState.Success -> {
            if (state.pokemon.isEmpty()) {
                Text(stringResource(R.string.pokemon_empty), modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    items(state.pokemon, key = { it.id }) { pokemon ->
                        Text(
                            text = stringResource(R.string.pokemon_list_item, pokemon.id, pokemon.name),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
