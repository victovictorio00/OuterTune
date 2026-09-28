# FASE 2 — Playback streaming (resolver URL al vuelo)

## Cambio
- `playback/MusicService.kt:571-~640` `createDataSourceFactory()`:
  - Mantiene flujo local (`localPath`/`isLocal`), `DownloadManagerOt`, `downloadCache`/`playerCache`.
  - Nuevo: si `mediaId` empieza con `qb:`/`dz:` y no está en cache:
    1. mira `songUrlCache` en memoria (TTL 5 min),
    2. `runBlocking { ProviderRegistry.chain().resolveStreamSilent(mediaId) }`,
    3. `dataSpec.withUri(url)`.
  - Si falla: log + cae a `PlaybackException(No datasource)` -> UI muestra genérico,
    `SkipOnErrorKey` decide saltar o parar (sin exponer proveedor).

## Por qué así
- Las `file_url` de Qobuz expiran. No se guardan en Room. Solo `qb:track:id` persiste.
- `ResolvingDataSource` ya era el punto correcto (antes YT lo usaba para stream).
- TTL evita pedir firma en cada seek dentro de la misma canción.

## Probar
- Reproducir un track Qobuz desde búsqueda/álbum -> log `PLAYING: provider resolved`.
- Activar avión tras iniciar -> usa `playerCache`; si no hay cache -> `waitOnNetworkError` existente.
