package com.dd3boh.outertune.provider.deezer

import com.dd3boh.outertune.provider.MusicProvider
import com.dd3boh.outertune.provider.ResolvedStream
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub Deezer para FASE 5: la interfaz ya existe para failover futuro.
 * Implementación real pendiente (requiere ARL / API privada).
 * ProviderChain lo salta porque isConfigured()=false.
 */
@Singleton
class DeezerProvider @Inject constructor() : MusicProvider {
    override val providerId = "deezer"
    override val displayName = "Deezer"
    override suspend fun isConfigured(): Boolean = false
    override suspend fun searchTracks(query: String, limit: Int): List<UnifiedTrack> = emptyList()
    override suspend fun searchAlbums(query: String, limit: Int): List<UnifiedAlbum> = emptyList()
    override suspend fun getAlbum(albumId: String): Pair<UnifiedAlbum, List<UnifiedTrack>> =
        throw Exception("Deezer no configurado")
    override suspend fun resolveStream(trackId: String): ResolvedStream =
        throw Exception("Deezer no configurado")
}
