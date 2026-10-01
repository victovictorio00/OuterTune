package com.dd3boh.outertune.provider.vps

import com.dd3boh.outertune.provider.ResolvedStream
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import org.json.JSONObject

/** Mapea TrackHit/AlbumResponse de vps-music-api a modelos unificados. */
object VpsMapper {
    fun toTrack(o: JSONObject, api: VpsApi): UnifiedTrack = UnifiedTrack(
        providerId = o.optString("provider_id"),
        title = o.optString("title", "Unknown"),
        artistName = o.optString("artist", "Unknown"),
        albumId = o.optString("album_provider_id").ifEmpty { null },
        albumTitle = o.optString("album").ifEmpty { null },
        coverUrl = o.optString("cover_url").ifEmpty { null }?.let(api::withToken),
        durationSec = o.optInt("duration_sec", -1),
        trackNumber = null,
        discNumber = null,
        year = if (o.isNull("year")) null else o.optInt("year"),
        isrc = o.optString("isrc").ifEmpty { null },
        explicit = false,
    )

    fun toAlbum(o: JSONObject): UnifiedAlbum = UnifiedAlbum(
        providerId = o.optString("provider_id"),
        title = o.optString("title", "Unknown"),
        artistName = o.optString("artist", "Unknown"),
        coverUrl = o.optString("cover_url").ifEmpty { null },
        releaseYear = if (o.isNull("year")) null else o.optInt("year"),
        trackCount = if (o.isNull("track_count")) null else o.optInt("track_count"),
        durationSec = null,
    )

    fun toStream(track: UnifiedTrack, api: VpsApi, streamUrl: String): ResolvedStream =
        ResolvedStream(
            url = api.withToken(streamUrl),
            mimeHint = null, // el servidor sirve opus/m4a/mp3/flac según origen
            bitrateKbps = null,
        )
}
