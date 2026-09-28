package com.dd3boh.outertune.provider

/**
 * Modelos unificados. Todo proveedor (Qobuz, Deezer, ...) mapea a esto.
 * OuterTune solo conoce estos modelos + MediaMetadata.
 */
data class UnifiedArtist(
    val providerId: String, // ej "qb:artist:123"
    val name: String,
    val pictureUrl: String? = null,
)

data class UnifiedAlbum(
    val providerId: String, // ej "qb:album:456"
    val title: String,
    val artistName: String,
    val coverUrl: String? = null,
    val releaseYear: Int? = null,
    val trackCount: Int? = null,
    val durationSec: Int? = null,
)

data class UnifiedTrack(
    val providerId: String, // ej "qb:track:789"
    val title: String,
    val artistName: String,
    val albumId: String? = null,
    val albumTitle: String? = null,
    val coverUrl: String? = null,
    val durationSec: Int = -1,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val isrc: String? = null,
    val explicit: Boolean = false,
)

/** URL lista para ExoPlayer. Caduca: no persistir, resolver al vuelo. */
data class ResolvedStream(
    val url: String,
    val mimeHint: String? = null, // "audio/flac", "audio/mpeg"
    val bitrateKbps: Int? = null,
)

sealed class ProviderResult<out T> {
    data class Ok<T>(val value: T) : ProviderResult<T>()
    data class Fail(val providerId: String, val error: Throwable) : ProviderResult<Nothing>()
}
