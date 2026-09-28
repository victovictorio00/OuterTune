# CAMBIOS — log de implementación

## Creados (13)
- `app/.../provider/ProviderModels.kt` — UnifiedTrack/Album/Artist, ResolvedStream.
- `app/.../provider/MusicProvider.kt` — interfaz.
- `app/.../provider/ProviderChain.kt` — failover invisible + dedup + timeouts.
- `app/.../provider/ProviderRegistry.kt` — registro + kill-switch memoria.
- `app/.../provider/ProviderMappers.kt` — UnifiedTrack -> MediaMetadata.
- `app/.../provider/ProviderCache.kt` — persistencia Room vía `insert(MediaMetadata)`.
- `app/.../provider/qobuz/QobuzConfig.kt` — BASE_URL + QobuzFormat(5,6,7,27).
- `app/.../provider/qobuz/QobuzApi.kt` — HttpURLConnection + org.json (login/search/album/fileUrl).
- `app/.../provider/qobuz/QobuzCredentialStore.kt` — DataStore credenciales.
- `app/.../provider/qobuz/QobuzMapper.kt` — JSON->Unified + ids qb:*.
- `app/.../provider/qobuz/QobuzProvider.kt` — search/album/stream con cascada calidad + login.
- `app/.../provider/deezer/DeezerProvider.kt` — stub isConfigured=false.
- `app/.../viewmodels/QobuzSearchViewModel.kt`, `QobuzAlbumViewModel.kt`.
- `app/.../ui/screens/search/QobuzSearchScreen.kt` (sección), `QobuzAlbumScreen.kt`.
- `app/.../ui/screens/settings/QobuzSettings.kt`, `fragments/QobuzFrag.kt`.
- `docs/revive-qobuz/00_INDICE.md`, `FASE_0..6`, `CAMBIOS.md` (este archivo).

## Modificados (8)
- `constants/PreferenceKeys.kt` — QobuzAppId/Secret/AuthToken/Email/Quality/Enabled + ProviderKillSwitch.
- `constants/Settings.kt` — enum QobuzAudioQuality.
- `App.kt` — registra qobuz+deezer + aplica kill-switch DataStore.
- `playback/MusicService.kt` — `createDataSourceFactory` resuelve qb:/dz: vía chain + caché TTL 5min.
- `playback/DownloadUtil.kt` — `downloadProviderTrack()` (stream->DownloadManagerOt->Room).
- `playback/downloadManager/DownloadManagerOt.kt` — `enqueue(url)` descarga real con progreso.
- `ui/screens/search/SearchScreen.kt` — monta QobuzSearchSection si query>=3.
- `MainActivity.kt` — rutas settings/qobuz + qobuz_album/{albumId}.
- `ui/screens/settings/SettingsScreen.kt` — entry Qobuz.
- `res/values/strings-ot.xml` — qobuz_settings_title/description.

## No tocado a propósito
- Schema Room v23, `SongEntity/AlbumEntity`, `QueueBoard`, `LocalMediaScanner`, Exo caches.
- `DownloadDirectoryManagerOt.saveFile` (.mka), `ExoDownloadService`, letras, Auto, widgets.

## Riesgos conocidos
- Qobuz exige app_id válido + suscripción para FLAC; sin eso `fileUrl` da 404/403 -> se ve `No disponible`.
- `QobuzProvider.login()` parsea 2 formatos de token; si Qobuz cambia JSON hay que ajustar.
- `QobuzFrag` usa `OutlinedTextField` + `SwitchPreference`; si el design system cambia, ajustar imports.
- Falta cablear `SongMenu Descargar` a `downloadProviderTrack` (1 línea cuando se quiera).
- Apple Music no incluido en v1 (requiere MusicKit dev token, sin descarga directa).

## Verificación build (2026-09-28)
- `git submodule update --init` ejecutado: `media/` restaurado (estaba vacío, bloqueaba settings).
- `./gradlew :app:assembleDebug` llega a task-graph OK, falla solo por entorno:
  `Cannot find Java 21 toolchain (solo JDK 17 instalado)`. No es error de código.
- Verificación estática hecha: `PreferenceEntry(description:String?)` corregido en SettingsScreen,
  `SwitchPreference`/`PreferenceGroupTitle` firmas OK, `DatabaseDao.insert(MediaMetadata)` existe,
  imports `toUri/dlCoroutine/reportException` presentes. Instalar JDK 21 y re-ejecutar build.
