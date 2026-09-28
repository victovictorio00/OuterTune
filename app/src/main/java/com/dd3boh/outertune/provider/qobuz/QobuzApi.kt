package com.dd3boh.outertune.provider.qobuz

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cliente HTTP mínimo sin dependencias externas (HttpURLConnection + org.json de Android).
 * Endpoints usados: /user/login, /catalog/search, /album/get, /track/getFileUrl.
 */
class QobuzApi(
    private val appId: String,
    private val userToken: String = "",
    private val appSecret: String = "",
) {
    private fun get(path: String, params: Map<String, String>): JSONObject = request("GET", path, params, null)
    private fun post(path: String, params: Map<String, String>): JSONObject = request("POST", path, emptyMap(), params)

    private fun request(method: String, path: String, query: Map<String, String>, form: Map<String, String>?): JSONObject {
        val qs = (query + mapOf("app_id" to appId)).entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val url = URL("${QobuzConfig.BASE_URL}$path?$qs")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", QobuzConfig.USER_AGENT)
            setRequestProperty("Accept", "application/json")
            if (userToken.isNotEmpty()) setRequestProperty("X-User-Auth-Token", userToken)
            if (method == "POST") {
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                val body = (form ?: emptyMap()).entries.joinToString("&") {
                    "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
                }
                outputStream.use { it.write(body.toByteArray()) }
            }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream.bufferedReader().use { it.readText() }
        conn.disconnect()
        if (code !in 200..299) throw Exception("Qobuz $path HTTP $code: ${text.take(300)}")
        return JSONObject(text)
    }

    suspend fun login(email: String, password: String): JSONObject = withContext(Dispatchers.IO) {
        post("/user/login", mapOf("email" to email, "password" to password))
    }

    suspend fun search(query: String, limit: Int = 25, offset: Int = 0): JSONObject = withContext(Dispatchers.IO) {
        get("/catalog/search", mapOf("query" to query, "limit" to limit.toString(), "offset" to offset.toString()))
    }

    suspend fun albumGet(albumId: String): JSONObject = withContext(Dispatchers.IO) {
        get("/album/get", mapOf("album_id" to albumId))
    }

    suspend fun fileUrl(trackId: String, formatId: Int): JSONObject = withContext(Dispatchers.IO) {
        get("/track/getFileUrl", mapOf("track_id" to trackId, "format_id" to formatId.toString()))
    }
}
