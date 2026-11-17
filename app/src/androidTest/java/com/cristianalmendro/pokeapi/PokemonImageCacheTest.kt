package com.cristianalmendro.pokeapi

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.cristianalmendro.pokeapi.data.image.createPokemonImageLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class PokemonImageCacheTest {
    @Test fun newLoaderReusesDownloadedImageFromDiskWithoutNetwork() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.cacheDir, "image-cache-test-${UUID.randomUUID()}")
        val requests = AtomicInteger()
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val bytes = ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.toByteArray()
        }
        bitmap.recycle()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            if (requests.incrementAndGet() > 1) throw IOException("network unavailable")
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").header("Cache-Control", "max-age=86400")
                .body(bytes.toResponseBody("image/png".toMediaType())).build()
        }.build()
        val request = ImageRequest.Builder(context)
            .data("https://images.example.test/pokemon.png").size(2, 2).build()
        var loader = createPokemonImageLoader(context, client, directory)
        try {
            assertTrue(loader.execute(request) is SuccessResult)
            loader.shutdown()
            loader = createPokemonImageLoader(context, client, directory)
            val cached = loader.execute(request)
            assertTrue(cached is SuccessResult)
            assertEquals(DataSource.DISK, (cached as SuccessResult).dataSource)
            assertEquals(1, requests.get())
        } finally {
            loader.shutdown()
            directory.deleteRecursively()
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }
}
