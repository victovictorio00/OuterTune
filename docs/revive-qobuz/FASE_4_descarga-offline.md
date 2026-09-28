# FASE 4 — Descarga offline permanente

## Cambios
- `playback/downloadManager/DownloadManagerOt.kt`:
  - `enqueue(mediaId, url)` antes era no-op. Ahora descarga real en `Dispatchers.IO`:
    `HttpURLConnection` + `instanceFollowRedirects`, emite `Progress/Success/Failure`,
    guarda con `DownloadDirectoryManagerOt.saveFile(mediaId, stream, displayName)` (`*.mka`).
  - Respeta `getFilePathIfExists` (no re-descarga) y `abort/url.isBlank`.
- `playback/DownloadUtil.kt` — nuevo `downloadProviderTrack(id, title)`:
  - ids `qb:/dz:` -> `resolveStreamSilent` -> `downloadMgr.enqueue(url)` ->
    `updateDownloadStatus(now)` + `downloads` map. Error -> toast `No disponible`.
  - ids legacy -> `downloadSong` (Exo cache) como antes.
- Reproduce offline: `MusicService` ya prioriza `localMgr.getFilePathIfExists` antes que red.

## Pendiente fino (no bloqueante)
- Taggeo FLAC/MP3 + embed cover con `ffMetadataEx` (flavor `full`) al completar `Success`.
- Re-escaneo `rescanDownloads()` tras `Success` para `dateDownload` consistente.
- Usar este método desde `SongMenu` (hoy llama `download(song)` legacy): cambiar a
  `downloadProviderTrack` cuando `id.startsWith("qb:")`.

## Probar
- Llamar `downloadUtil.downloadProviderTrack("qb:track:xxx","titulo")` -> progreso en
  `DownloadManagerOt.events`, fichero `[titulo] [qb:track:xxx].mka` en carpeta descargas,
  reproducir en avión OK.
