package com.cristianalmendro.pokeapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristianalmendro.pokeapi.ui.home.HomeScreen
import com.cristianalmendro.pokeapi.ui.home.HomeViewModel
import com.cristianalmendro.pokeapi.ui.theme.PokeApiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = (application as PokeApiApplication).pokemonRepository
            val factory = remember(repository) { HomeViewModel.factory(repository) }
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            val state by homeViewModel.state.collectAsStateWithLifecycle()
            PokeApiTheme(darkTheme = false, dynamicColor = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        state = state,
                        onLoadMore = homeViewModel::loadNextPage,
                        onRetry = homeViewModel::retry,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
