# FASE 0 — Base y arquitectura

## Diagnóstico (verificado en código)

- Eliminado: `innertube/` (`YouTube.kt`, `InnerTube.kt`, `NewPipe.kt`), `YTPlayerUtils.kt`,
  `SyncUtils.kt`, `YouTubeQueue.kt`, `YouTubeAlbumRadio.kt`, `OnlineSearchScreen`, menús `YouTube*`.
- Roto: `playback/MusicService.kt:571-618` `createDataSourceFactory()`:
  solo resuelve `localPath` / `DownloadManagerOt` / `SimpleCache`. Si no hay cache -> `throw PlaybackException(No datasource)`.
- Huérfano: `playback/DownloadUtil.kt:112-124` crea `DownloadRequest(id.toUri())` sin resolver URL real.
- Vivo: Room (`SongEntity`, `AlbumEntity`, `ArtistEntity`), `QueueBoard`, `LocalMediaScanner`,
  ExoPlayer + `playerCache`/`downloadCache` (`di/AppModule.kt`), `DownloadDirectoryManagerOt.saveFile()`.

## Decisión arquitectura

```text
UI (Compose) + Room + ExoPlayer  <-  provider/MusicProvider  <-  Qobuz / Deezer / ...
```

- OuterTune **no** guarda URLs con expiración. Guarda `id = qb:track:<id>` + metadata + cover URL.
- La URL de stream se resuelve **en el momento de reproducir/descargar** vía `ResolvingDataSource`.
- `ProviderChain` intenta proveedores en orden, con timeout, sin exponer errores al usuario.

## Contratos nuevos

- `provider/ProviderModels.kt`: `UnifiedTrack`, `UnifiedAlbum`, `UnifiedArtist`, `ResolvedStream`.
- `provider/MusicProvider.kt`: `searchTracks, searchAlbums, getAlbum, resolveStream, getCoverUrl`.
- `provider/ProviderChain.kt`: `searchTracksMerged(), resolveStreamSilent(), getAlbumFirstHit()`.
- `provider/ProviderRegistry.kt`: singleton mutable, kill-switch por `DataStore`.

## IDs

- Qobuz track -> `qb:track:<qobuzId>` ; álbum -> `qb:album:<qobuzId>` ; artista -> `qb:artist:<qobuzId>`.
- Deezer futuro -> `dz:track:<id>`. Así `findSong()`, `database.song(id)`, `customCacheKey` siguen funcionando.
