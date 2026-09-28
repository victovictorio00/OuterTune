package com.dd3boh.outertune.provider.qobuz

import com.dd3boh.outertune.provider.MusicProvider
import com.dd3boh.outertune.provider.ResolvedStream
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor Qobuz: buscador + metadata + portada + info álbum + stream.
 * Sin credenciales -> isConfigured=false y ProviderChain lo salta en silencio.
 */
@Singleton
class QobuzProvider @Inject constructor(
    private val creds: QobuzCredentialStore,
) : MusicProvider {
    override val providerId = "qobuz"
    override val displayName = "Qobuz"

    private fun api(): QobuzApi {
        val appId = creds.appId()
        if (appId.isBlank()) throw Exception("Qobuz sin app_id")
        return QobuzApi(appId = appId, userToken = creds.userToken(), appSecret = creds.appSecret())
    }

    override suspend fun isConfigured(): Boolean = creds.appId().isNotBlank()

    override suspend fun searchTracks(query: String, limit: Int): List<UnifiedTrack> {
        val json = api().search(query, limit)
        val arr = json.optJSONObject("tracks")?.optJSONArray("items") ?: return emptyList()
        return List(arr.length()) { QobuzMapper.toTrack(arr.getJSONObject(it)) }
    }

    override suspend fun searchAlbums(query: String, limit: Int): List<UnifiedAlbum> {
        val json = api().search(query, limit)
        val arr = json.optJSONObject("albums")?.optJSONArray("items") ?: return emptyList()
        return List(arr.length()) { QobuzMapper.toAlbum(arr.getJSONObject(it)) }
    }

    override suspend fun getAlbum(albumId: String): Pair<UnifiedAlbum, List<UnifiedTrack>> {
        val raw = QobuzMapper.rawFromPrefixed(albumId)
        val json = api().albumGet(raw)
        val album = QobuzMapper.toAlbum(json)
        val arr = json.optJSONObject("tracks")?.optJSONArray("items")
        val tracks = if (arr != null) List(arr.length()) { QobuzMapper.toTrack(arr.getJSONObject(it)) } else emptyList()
        return album to tracks
    }

    override suspend fun resolveStream(trackId: String): ResolvedStream {
        val raw = QobuzMapper.rawFromPrefixed(trackId)
        // Intenta calidad elegida y baja en cascada: 27 -> 7 -> 6 -> 5
        val wanted = creds.quality().formatId
        val order = listOf(wanted, 27, 7, 6, 5).distinct()
        var last: Exception? = null
        for (f in order) {
            try {
                return QobuzMapper.toStream(api().fileUrl(raw, f))
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: Exception("No disponible")
    }

    suspend fun login(email: String, password: String): String {
        // login usa app_id; devuelve user_auth_token
        val tmp = QobuzApi(appId = creds.appId())
        val json = tmp.login(email, password)
        val token = json.optJSONObject("user_auth_token")
            ?: json.optJSONObject("user")?.optString("credential", "")
        val tokenStr = when (token) {
            is org.json.JSONObject -> token.optString("token")
            else -> token.toString()
        }.toString()
        if (tokenStr.isBlank() || tokenStr == "null") {
            // Formato alternativo: user_auth_token como string
            val alt = json.optString("user_auth_token")
            if (alt.isNotEmpty()) {
                creds.saveToken(alt)
                return alt
            }
            throw Exception("Login Qobuz sin token")
        }
        creds.saveToken(tokenStr)
        return tokenStr
    }
}
