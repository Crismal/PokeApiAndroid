package com.cristianalmendro.pokeapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristianalmendro.pokeapi.ui.pokemon.PokemonListScreen
import com.cristianalmendro.pokeapi.ui.pokemon.PokemonListViewModel
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PokemonListViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            PokeApiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PokemonListScreen(
                        state = state,
                        onRetry = viewModel::retry,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
