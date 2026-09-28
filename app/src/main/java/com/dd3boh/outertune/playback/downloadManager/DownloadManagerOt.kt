package com.dd3boh.outertune.playback.downloadManager

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


sealed class DownloadEvent {
    data class Progress(val mediaId: String, val bytesRead: Long, val contentLength: Long) : DownloadEvent()
    data class Success(val mediaId: String, val file: Uri) : DownloadEvent()
    data class Failure(val mediaId: String, val error: Throwable) : DownloadEvent()
}

class DownloadManagerOt(
    private val local: DownloadDirectoryManagerOt,
) {
    private val _events = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 100)
    val events = _events.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * FASE 4: descarga real desde URL firmada (Qobuz file_url).
     * Descarga en background, guarda vía DownloadDirectoryManagerOt y emite Progress/Success/Failure.
     */
    fun enqueue(mediaId: String, url: String, displayName: String? = null, abort: Boolean = false) {

        // if already exists, immediately emit success
        local.getFilePathIfExists(mediaId)?.let {
            _events.tryEmit(DownloadEvent.Success(mediaId, it))
            return
        }

        if (abort || url.isBlank()) {
            _events.tryEmit(DownloadEvent.Failure(mediaId, Exception("Could not resolve download: $displayName")))
            return
        }

        scope.launch {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20_000
                    readTimeout = 20_000
                    setRequestProperty("User-Agent", "OuterTune-Qobuz/1.0")
                    instanceFollowRedirects = true
                }
                val code = conn.responseCode
                if (code !in 200..299) throw IOException("HTTP $code")
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: -1L
                var downloaded = 0L
                val src: InputStream = conn.inputStream
                val counting: InputStream = object : InputStream() {
                    override fun read(): Int {
                        val b = src.read()
                        if (b >= 0) {
                            downloaded++
                            _events.tryEmit(DownloadEvent.Progress(mediaId, downloaded, total))
                        }
                        return b
                    }
                    override fun read(b: ByteArray, off: Int, len: Int): Int {
                        val c = src.read(b, off, len)
                        if (c > 0) {
                            downloaded += c
                            _events.tryEmit(DownloadEvent.Progress(mediaId, downloaded, total))
                        }
                        return c
                    }
                    override fun close() = src.close()
                }
                counting.use { input ->
                    val saved = local.saveFile(mediaId, input, displayName = displayName)
                    if (saved != null) _events.tryEmit(DownloadEvent.Success(mediaId, saved))
                    else throw IOException("Failed to save file")
                }
            } catch (e: Throwable) {
                _events.tryEmit(DownloadEvent.Failure(mediaId, e))
            } finally {
                conn?.disconnect()
            }
        }
    }

    fun enqueue(mediaId: String, data: ByteArray, displayName: String? = null) {
        // if already exists, immediately emit success
        local.getFilePathIfExists(mediaId)?.let {
            _events.tryEmit(DownloadEvent.Success(mediaId, it))
            return
        }

        try {
            val total = data.size.toLong()
            var downloaded = 0L

            // wrap the source to track progress
            val source = data.inputStream()
            val countingStream = object : InputStream() {
                override fun read(): Int {
                    val byte = source.read()
                    if (byte >= 0) {
                        downloaded++
                        _events.tryEmit(DownloadEvent.Progress(mediaId, downloaded, total))
                    }
                    return byte
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val count = source.read(b, off, len)
                    if (count > 0) {
                        downloaded += count
                        _events.tryEmit(DownloadEvent.Progress(mediaId, downloaded, total))
                    }
                    return count
                }

                override fun close() {
                    source.close()
                }
            }

            // save to disk
            val saved = local.saveFile(mediaId, countingStream, displayName = displayName)
            if (saved != null) {
                _events.tryEmit(DownloadEvent.Success(mediaId, saved))
            } else {
                throw IOException("Failed to save file")
            }
        } catch (e: Throwable) {
            _events.tryEmit(DownloadEvent.Failure(mediaId, e))
        }
    }

    fun enqueueAll(pairs: List<Pair<String, String>>) {
        pairs.forEach { (id, url) -> enqueue(id, url) }
    }

    fun getFilePath(mediaId: String): Uri? = local.getFilePathIfExists(mediaId)
}