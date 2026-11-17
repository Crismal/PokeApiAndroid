package com.cristianalmendro.pokeapi

import android.app.Application
import com.cristianalmendro.pokeapi.data.OkHttpJsonClient
import com.cristianalmendro.pokeapi.data.PokeApiRepository
import com.cristianalmendro.pokeapi.data.PokeApiService
import com.cristianalmendro.pokeapi.data.PokemonRepository
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient

class PokeApiApplication : Application() {
    val pokemonRepository: PokemonRepository by lazy {
        val client = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "pokeapi-http"), 20L * 1024 * 1024))
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        PokeApiRepository(PokeApiService(OkHttpJsonClient(client)))
    }
}
