package com.dd3boh.outertune.provider

/**
 * Contrato que todo sistema de descarga/streaming debe cumplir.
 * Qobuz es la primera implementación. Deezer/Apple son stubs futuros.
 */
interface MusicProvider {
    val providerId: String // "qobuz", "deezer"
    val displayName: String
    suspend fun isConfigured(): Boolean
    suspend fun searchTracks(query: String, limit: Int = 25): List<UnifiedTrack>
    suspend fun searchAlbums(query: String, limit: Int = 25): List<UnifiedAlbum>
    suspend fun getAlbum(albumId: String): Pair<UnifiedAlbum, List<UnifiedTrack>>
    suspend fun resolveStream(trackId: String): ResolvedStream
    fun coverUrl(track: UnifiedTrack, sizePx: Int = 1000): String? = track.coverUrl
}
