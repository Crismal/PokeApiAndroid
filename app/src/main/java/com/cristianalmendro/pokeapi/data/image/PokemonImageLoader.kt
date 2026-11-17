package com.cristianalmendro.pokeapi.data.image

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import java.io.File
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath

/** One app-wide loader; image bytes stay outside the database. */
internal fun createPokemonImageLoader(
    context: Context,
    client: OkHttpClient = OkHttpClient(),
    directory: File = File(context.cacheDir, "pokemon-artwork"),
): ImageLoader = ImageLoader.Builder(context)
    .components { add(OkHttpNetworkFetcherFactory(callFactory = { client })) }
    .memoryCache { MemoryCache.Builder().maxSizePercent(context, .15).build() }
    .diskCache {
        DiskCache.Builder().directory(directory.absolutePath.toPath())
            .maxSizeBytes(100L * 1024 * 1024).build()
    }
    .build()
