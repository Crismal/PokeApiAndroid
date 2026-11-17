package com.cristianalmendro.pokeapi.data

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

fun interface JsonHttpClient {
    suspend fun get(url: String): JSONObject
}

class OkHttpJsonClient(private val client: OkHttpClient) : JsonHttpClient {
    override suspend fun get(url: String): JSONObject = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(Request.Builder().url(url).build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = response.use {
                        if (!it.isSuccessful) throw IOException("PokeAPI HTTP ${it.code}")
                        val body = it.body ?: throw IOException("Empty PokeAPI response")
                        JSONObject(body.string())
                    }
                    continuation.resume(json)
                } catch (error: IOException) {
                    continuation.resumeWithException(error)
                } catch (error: JSONException) {
                    continuation.resumeWithException(IOException("Invalid PokeAPI JSON", error))
                }
            }
        })
    }
}
