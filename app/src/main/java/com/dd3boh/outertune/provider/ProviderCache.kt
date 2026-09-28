package com.dd3boh.outertune.provider

import com.dd3boh.outertune.db.MusicDatabase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste resultados Qobuz en Room para que AlbumScreen / colas / historial funcionen
 * sin cambios: OuterTune sigue siendo solo-local a nivel DB.
 * Usa DatabaseDao.insert(MediaMetadata) que ya crea song+artist+album.
 */
@Singleton
class ProviderCache @Inject constructor(
    private val db: MusicDatabase,
) {
    suspend fun persistTracks(tracks: List<UnifiedTrack>) {
        if (tracks.isEmpty()) return
        // insert() es @Transaction; llamar uno por uno es seguro en IO.
        tracks.forEach { t ->
            runCatching { db.insert(t.toMediaMetadata()) }
        }
    }

    suspend fun persistAlbum(album: UnifiedAlbum, tracks: List<UnifiedTrack>) {
        persistTracks(tracks)
    }
}
