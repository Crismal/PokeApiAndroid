package com.cristianalmendro.pokeapi.data

import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONException
import org.json.JSONObject

data class PokemonReferences(val ids: List<Int>, val nextOffset: Int?)

interface PokemonApi {
    suspend fun getReferences(offset: Int, limit: Int): PokemonReferences
    suspend fun getPokemon(id: Int): Pokemon
}

class PokeApiService(private val http: JsonHttpClient) : PokemonApi {
    override suspend fun getReferences(offset: Int, limit: Int): PokemonReferences {
        require(offset >= 0 && limit > 0)
        return parseResponse {
            val json = http.get("${BASE_URL}pokemon?limit=$limit&offset=$offset")
            if (!json.has("next")) throw IOException("Missing pagination cursor")
            val results = json.getJSONArray("results")
            val ids = List(results.length()) { index ->
                val url = results.getJSONObject(index).getString("url").toHttpUrlOrNull()
                    ?: throw IOException("Invalid Pokemon resource URL")
                if (url.scheme != "https" || url.host != "pokeapi.co" ||
                    !url.encodedPath.matches(Regex("/api/v2/pokemon/[0-9]+/?"))) {
                    throw IOException("Unexpected Pokemon resource URL")
                }
                url.encodedPath.trimEnd('/').substringAfterLast('/').toIntOrNull()
                    ?.takeIf { it > 0 } ?: throw IOException("Invalid Pokemon ID")
            }
            val next = if (json.isNull("next")) null else {
                val url = json.getString("next").toHttpUrlOrNull()
                    ?: throw IOException("Invalid next page URL")
                if (url.scheme != "https" || url.host != "pokeapi.co" ||
                    url.encodedPath.trimEnd('/') != "/api/v2/pokemon") {
                    throw IOException("Unexpected next page URL")
                }
                url.queryParameter("offset")?.toIntOrNull()?.takeIf { it > offset }
                    ?: throw IOException("Pagination did not advance")
            }
            if (ids.isEmpty() && next != null) throw IOException("Empty page with a next cursor")
            PokemonReferences(ids, next)
        }
    }

    override suspend fun getPokemon(id: Int): Pokemon = parseResponse {
        val json = http.get("${BASE_URL}pokemon/$id/")
        val types = json.getJSONArray("types")
        val sprites = json.optJSONObject("sprites")
        val artwork = sprites?.optJSONObject("other")?.optJSONObject("official-artwork")
        val actualId = json.getInt("id")
        if (actualId != id) throw IOException("Unexpected Pokemon identity")
        Pokemon(
            id = actualId,
            name = json.getString("name"),
            types = List(types.length()) { types.getJSONObject(it) }
                .sortedBy { it.getInt("slot") }
                .map { it.getJSONObject("type").getString("name") },
            artworkUrl = artwork.nonEmptyString("front_default")
                ?: sprites.nonEmptyString("front_default"),
        )
    }

    private suspend fun <T> parseResponse(block: suspend () -> T): T = try {
        block()
    } catch (error: JSONException) {
        throw IOException("Invalid PokeAPI response fields", error)
    }

    private fun JSONObject?.nonEmptyString(key: String): String? =
        if (this == null || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
