package com.dd3boh.outertune.provider.vps

import android.content.Context

/**
 * Aviso de APK nueva: compara version_code instalado vs /apk/version.
 * Devuelve (versión, url) si hay algo más nuevo, null si no.
 */
object VpsUpdate {
    suspend fun check(context: Context, currentCode: Int): Pair<String, String>? {
        return try {
            val store = VpsCredentialStore(context)
            val provider = VpsProvider(store)
            val (ver, code) = provider.serverApk()
            if (code > currentCode) ver to provider.apkUrl() else null
        } catch (_: Exception) {
            null // sin red o sin APK: silencio
        }
    }
}
