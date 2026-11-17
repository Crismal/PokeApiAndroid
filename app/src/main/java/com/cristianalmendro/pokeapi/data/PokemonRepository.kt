package com.cristianalmendro.pokeapi.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Pokemon(val id: Int, val name: String)

interface PokemonRepository {
    suspend fun getFirstTwentyPokemon(): List<Pokemon>
}

class PokeApiRepository : PokemonRepository {
    private var cachedPokemon: List<Pokemon>? = null

    override suspend fun getFirstTwentyPokemon(): List<Pokemon> = withContext(Dispatchers.IO) {
        cachedPokemon?.let { return@withContext it }

        val connection = URL("https://pokeapi.co/api/v2/pokemon?limit=20&offset=0")
            .openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("PokeAPI returned HTTP ${connection.responseCode}")
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(response).getJSONArray("results")
            List(results.length()) { index ->
                val resource = results.getJSONObject(index)
                // The list endpoint returns an ID in each resource URL, not a separate ID field.
                val id = resource.getString("url").trimEnd('/').substringAfterLast('/').toInt()
                Pokemon(id = id, name = resource.getString("name"))
            }.also { cachedPokemon = it }
        } finally {
            connection.disconnect()
        }
    }
}
