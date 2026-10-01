package com.dd3boh.outertune.provider.vps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cliente HTTP mínimo (mismo estilo que QobuzApi): HttpURLConnection + org.json.
 * Endpoints: search, album por id, apk version. Todo con Bearer salvo apk.
 */
class VpsApi(
    private val baseUrl: String,
    private val token: String,
) {
    private suspend fun get(path: String, params: Map<String, String> = emptyMap(),
                        auth: Boolean = true): JSONObject = withContext(Dispatchers.IO) {
        val qs = params.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val url = URL("$baseUrl$path${if (qs.isEmpty()) "" else "?$qs"}")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", VpsConfig.USER_AGENT)
            setRequestProperty("Accept", "application/json")
            if (auth && token.isNotEmpty()) setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw Exception("VPS $path HTTP $code: ${text.take(200)}")
            JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 25): JSONObject =
        get("/search", mapOf("q" to query, "limit" to limit.toString()))

    suspend fun album(albumId: String): JSONObject =
        get("/album/" + URLEncoder.encode(albumId, "UTF-8"))

    suspend fun apkVersion(): JSONObject = get("/apk/version", auth = false)

    /** URLs de media con ?token= para ExoPlayer/Coil (no mandan headers). */
    fun withToken(url: String): String {
        if (token.isEmpty()) return url
        val sep = if (url.contains("?")) "&" else "?"
        return "$url${sep}token=${URLEncoder.encode(token, "UTF-8")}"
    }
}
