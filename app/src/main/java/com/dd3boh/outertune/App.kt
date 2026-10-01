/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.allowHardware
import coil3.request.crossfade
import com.dd3boh.outertune.utils.CoilBitmapLoader
import com.dd3boh.outertune.utils.LocalArtworkPathKeyer
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {
    private val TAG = App::class.simpleName.toString()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }

        instance = this

        // FASE 1+5: registra proveedores (Qobuz primero, Deezer stub) + kill-switch.
        runCatching {
            val creds = com.dd3boh.outertune.provider.qobuz.QobuzCredentialStore(this)
            com.dd3boh.outertune.provider.ProviderRegistry.register(
                com.dd3boh.outertune.provider.qobuz.QobuzProvider(creds)
            )
            com.dd3boh.outertune.provider.ProviderRegistry.register(
                com.dd3boh.outertune.provider.deezer.DeezerProvider()
            )
            val vpsStore = com.dd3boh.outertune.provider.vps.VpsCredentialStore(this)
            if (vpsStore.enabled()) {
                com.dd3boh.outertune.provider.ProviderRegistry.register(
                    com.dd3boh.outertune.provider.vps.VpsProvider(vpsStore)
                )
            }
            val killed = dataStore.get(
                com.dd3boh.outertune.constants.ProviderKillSwitchKey, ""
            ).split(",").map { it.trim() }.filter { it.isNotEmpty() }
            killed.forEach { com.dd3boh.outertune.provider.ProviderRegistry.setDisabled(it, true) }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {

        return ImageLoader.Builder(this)
            .components {
                add(CoilBitmapLoader.Factory(this@App))
                add(LocalArtworkPathKeyer())
            }
            .crossfade(true)
            .allowHardware(false)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.3)
                    .build()
            }
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}