package com.dd3boh.outertune.provider.qobuz

import com.dd3boh.outertune.provider.ResolvedStream
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import org.json.JSONObject

/** Mapeo Qobuz JSON -> modelos unificados + IDs prefijados qb:*. */
object QobuzMapper {
    fun trackId(raw: String) = "qb:track:$raw"
    fun albumId(raw: String) = "qb:album:$raw"
    fun artistId(raw: String) = "qb:artist:$raw"
    fun rawFromPrefixed(prefixed: String) = prefixed.substringAfterLast(":")

    private fun coverOf(o: JSONObject): String? {
        val img = o.optJSONObject("image") ?: return null
        return img.optString("mega").takeIf { it.isNotEmpty() }
            ?: img.optString("large").takeIf { it.isNotEmpty() }
            ?: img.optString("medium").takeIf { it.isNotEmpty() }
            ?: img.optString("small").takeIf { it.isNotEmpty() }
    }

    fun toTrack(o: JSONObject): UnifiedTrack {
        val id = o.optString("id")
        val title = o.optString("title", "Unknown")
        val performer = o.optJSONObject("performer")?.optString("name")
            ?: o.optJSONObject("artist")?.optString("name")
            ?: o.optString("performer", "Unknown")
        val album = o.optJSONObject("album")
        val dur = o.optInt("duration", -1)
        return UnifiedTrack(
            providerId = trackId(id),
            title = title,
            artistName = performer,
            albumId = album?.optString("id")?.takeIf { it.isNotEmpty() }?.let { albumId(it) },
            albumTitle = album?.optString("title"),
            coverUrl = coverOf(o).let { it } ?: album?.let { coverOf(it) },
            durationSec = dur,
            trackNumber = o.optInt("track_number").takeIf { it > 0 },
            discNumber = o.optInt("media_number").takeIf { it > 0 },
            year = o.optString("release_date_original").take(4).toIntOrNull()
                ?: album?.optString("release_date_original")?.take(4)?.toIntOrNull(),
            isrc = o.optString("isrc").takeIf { it.isNotEmpty() },
            explicit = o.optBoolean("parental_warning", false),
        )
    }

    fun toAlbum(o: JSONObject): UnifiedAlbum {
        val id = o.optString("id")
        return UnifiedAlbum(
            providerId = albumId(id),
            title = o.optString("title", "Unknown"),
            artistName = o.optJSONObject("artist")?.optString("name")
                ?: o.optString("artist", "Unknown"),
            coverUrl = coverOf(o),
            releaseYear = o.optString("release_date_original").take(4).toIntOrNull(),
            trackCount = o.optInt("tracks_count").takeIf { it > 0 },
            durationSec = o.optInt("duration").takeIf { it > 0 },
        )
    }

    fun toStream(o: JSONObject): ResolvedStream {
        val url = o.optString("url")
        if (url.isEmpty()) throw Exception(o.optString("message", "track no disponible para tu plan"))
        val mime = o.optString("mime_type").takeIf { it.isNotEmpty() }
        return ResolvedStream(url = url, mimeHint = mime)
    }
}
