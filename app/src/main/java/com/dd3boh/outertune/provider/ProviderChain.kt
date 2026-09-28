package com.dd3boh.outertune.provider

import android.util.Log
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Cadena con failover invisible.
 * El usuario nunca sabe qué proveedor falló: solo recibe el primer éxito
 * o una excepción genérica "No disponible".
 */
class ProviderChain(
    private val providers: List<MusicProvider>,
) {
    private val tag = "ProviderChain"

    fun ordered(): List<MusicProvider> = providers.filter { !ProviderRegistry.isDisabled(it.providerId) }

    suspend fun searchTracksMerged(query: String, limit: Int = 25): List<UnifiedTrack> {
        if (query.isBlank()) return emptyList()
        val out = LinkedHashMap<String, UnifiedTrack>()
        for (p in ordered()) {
            try {
                if (!p.isConfigured()) continue
                val r = withTimeoutOrNull(12_000) { p.searchTracks(query, limit) } ?: continue
                for (t in r) {
                    val key = "${t.title.lowercase()}|${t.artistName.lowercase()}|${t.durationSec / 2}"
                    out.putIfAbsent(key, t)
                    if (out.size >= limit) break
                }
                if (out.isNotEmpty()) break // Qobuz primero: si hay hits no seguir (ahorra cuota)
            } catch (e: Exception) {
                Log.w(tag, "search fail ${p.providerId}: ${e.message}")
            }
        }
        return out.values.toList()
    }

    suspend fun searchAlbumsMerged(query: String, limit: Int = 25): List<UnifiedAlbum> {
        if (query.isBlank()) return emptyList()
        for (p in ordered()) {
            try {
                if (!p.isConfigured()) continue
                val r = withTimeoutOrNull(12_000) { p.searchAlbums(query, limit) } ?: continue
                if (r.isNotEmpty()) return r
            } catch (e: Exception) {
                Log.w(tag, "album search fail ${p.providerId}: ${e.message}")
            }
        }
        return emptyList()
    }

    /** Resuelve stream probando proveedores en orden. Lanza genérica si todo falla. */
    suspend fun resolveStreamSilent(providerTrackId: String): ResolvedStream {
        var last: Exception? = null
        for (p in ordered()) {
            try {
                if (!p.isConfigured()) continue
                // Solo el proveedor dueño del id intenta primero (qb:track: -> qobuz)
                val r = withTimeoutOrNull(15_000) { p.resolveStream(providerTrackId) } ?: continue
                return r
            } catch (e: Exception) {
                Log.w(tag, "stream fail ${p.providerId} $providerTrackId: ${e.message}")
                last = e as? Exception ?: Exception(e.message)
            }
        }
        throw last ?: Exception("No disponible")
    }

    suspend fun getAlbumFirstHit(albumId: String): Pair<UnifiedAlbum, List<UnifiedTrack>>? {
        for (p in ordered()) {
            try {
                if (!p.isConfigured()) continue
                return withTimeoutOrNull(15_000) { p.getAlbum(albumId) }
            } catch (e: Exception) {
                Log.w(tag, "album fail ${p.providerId}: ${e.message}")
            }
        }
        return null
    }
}
