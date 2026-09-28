package com.dd3boh.outertune.provider

import com.dd3boh.outertune.models.MediaMetadata

/** Convierte tracks unificados a MediaMetadata que ExoPlayer/Room ya entienden. */
fun UnifiedTrack.toMediaMetadata(): MediaMetadata = MediaMetadata(
    id = providerId,
    title = title,
    artists = listOf(MediaMetadata.Artist(id = null, name = artistName)),
    duration = durationSec,
    thumbnailUrl = coverUrl,
    trackNumber = trackNumber,
    discNumber = discNumber,
    album = if (albumId != null) MediaMetadata.Album(id = albumId, title = albumTitle.orEmpty()) else null,
    genre = null,
    year = year,
    isLocal = false,
    localPath = null,
)
