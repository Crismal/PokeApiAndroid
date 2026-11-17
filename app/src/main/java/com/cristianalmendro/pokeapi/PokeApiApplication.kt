package com.cristianalmendro.pokeapi

import android.app.Application
import android.content.Context
import androidx.room.Room
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.cristianalmendro.pokeapi.data.OkHttpJsonClient
import com.cristianalmendro.pokeapi.data.PokeApiRepository
import com.cristianalmendro.pokeapi.data.PokeApiService
import com.cristianalmendro.pokeapi.data.PokemonRepository
import com.cristianalmendro.pokeapi.data.image.createPokemonImageLoader
import com.cristianalmendro.pokeapi.data.local.PokemonDatabase
import com.cristianalmendro.pokeapi.data.local.RoomPokemonLocalDataSource
import com.cristianalmendro.pokeapi.data.local.RoomPokemonSearchLocalDataSource
import com.cristianalmendro.pokeapi.data.search.LocalPokemonSearchRepository
import com.cristianalmendro.pokeapi.data.search.PokemonSearchRepository
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient

class PokeApiApplication : Application(), SingletonImageLoader.Factory {
    private val database by lazy {
        Room.databaseBuilder(this, PokemonDatabase::class.java, "pokemon.db")
            .addMigrations(PokemonDatabase.MIGRATION_1_2).build()
    }
    private val api by lazy {
        val client = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "pokeapi-http"), 20L * 1024 * 1024))
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        PokeApiService(OkHttpJsonClient(client))
    }
    private val repository by lazy { PokeApiRepository(api, RoomPokemonLocalDataSource(database)) }
    val pokemonRepository: PokemonRepository get() = repository
    val searchRepository: PokemonSearchRepository by lazy {
        LocalPokemonSearchRepository(api, RoomPokemonSearchLocalDataSource(database), repository)
    }

    override fun newImageLoader(context: Context): ImageLoader = createPokemonImageLoader(context)
}
