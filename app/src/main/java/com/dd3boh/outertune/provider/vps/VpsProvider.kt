package com.dd3boh.outertune.provider.vps

import com.dd3boh.outertune.provider.MusicProvider
import com.dd3boh.outertune.provider.ResolvedStream
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor TubeOther: buscador + álbum + stream del servidor propio.
 * Siempre configurado (URL y token incluidos); se apaga en Ajustes > TubeOther.
 * La API no tiene búsqueda de álbumes: se agrupan los tracks por álbum.
 */
@Singleton
class VpsProvider @Inject constructor(
    private val creds: VpsCredentialStore,
) : MusicProvider {
    override val providerId = "vps"
    override val displayName = "TubeOther"

    override suspend fun isConfigured(): Boolean =
        creds.enabled() && creds.baseUrl().isNotBlank() && creds.token().isNotBlank()

    private fun api(): VpsApi = creds.api()

    override suspend fun searchTracks(query: String, limit: Int): List<UnifiedTrack> {
        val arr = api().searchTracks(query, limit).optJSONArray("tracks") ?: return emptyList()
        return List(arr.length()) { VpsMapper.toTrack(arr.getJSONObject(it), api()) }
    }

    override suspend fun searchAlbums(query: String, limit: Int): List<UnifiedAlbum> {
        // Sin endpoint de álbumes: se agrupa por álbum desde los tracks.
        val seen = LinkedHashMap<String, UnifiedAlbum>()
        for (t in searchTracks(query, 50)) {
            val aid = t.albumId ?: continue
            if (seen.size >= limit) break
            seen.putIfAbsent(aid, UnifiedAlbum(
                providerId = aid,
                title = t.albumTitle ?: "Unknown",
                artistName = t.artistName,
                coverUrl = t.coverUrl,
                releaseYear = t.year,
                trackCount = null,
                durationSec = null,
            ))
        }
        return seen.values.toList()
    }

    override suspend fun getAlbum(albumId: String): Pair<UnifiedAlbum, List<UnifiedTrack>> {
        val json = api().album(albumId)
        val album = VpsMapper.toAlbum(json.getJSONObject("album"))
        val arr = json.optJSONArray("tracks") ?: return album to emptyList()
        val tracks = List(arr.length()) { VpsMapper.toTrack(arr.getJSONObject(it), api()) }
        return album to tracks
    }

    override suspend fun resolveStream(trackId: String): ResolvedStream {
        val a = api()
        // El stream vive en la metadata: se re-resuelve barato vía search por ID.
        // Atajo: la URL sigue el patrón /stream/<provider_id>.
        val streamUrl = "${creds.baseUrl().trimEnd('/')}/stream/$trackId"
        return VpsMapper.toStream(
            UnifiedTrack(providerId = trackId, title = "", artistName = ""),
            a, streamUrl,
        )
    }

    override fun coverUrl(track: UnifiedTrack, sizePx: Int): String? = track.coverUrl

    /** Versión publicada en el servidor para el aviso de updates. */
    suspend fun serverApk(): Pair<String, Int> {
        val j = api().apkVersion()
        if (!j.optBoolean("available")) throw Exception("sin APK publicada")
        return j.optString("version") to j.optInt("version_code")
    }

    fun apkUrl(): String = "${creds.baseUrl().trimEnd('/')}/apk/latest"
}
