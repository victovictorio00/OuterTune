# FASE 3 — Álbum, metadata y portada (OuterTune solo interfaz)

## Cambios
- `provider/ProviderMappers.kt` — `UnifiedTrack.toMediaMetadata()` (id, título, artista, duración,
  `thumbnailUrl=cover`, track/disc, álbum, año).
- `provider/ProviderCache.kt` — `persistTracks/persistAlbum` vía `DatabaseDao.insert(MediaMetadata)`
  (crea song+artist+album sin tocar schema v23).
- `provider/qobuz/QobuzProvider.getAlbum()` — `album/get` + tracks.
- `viewmodels/QobuzAlbumViewModel.kt` — `load(albumId)` -> `getAlbumFirstHit` + persist.
- `ui/screens/search/QobuzAlbumScreen.kt` — ruta `qobuz_album/{albumId}`: cover grande (Coil),
  título/artista/año/nº temas, lista tracks -> `ListQueue` con `startIndex`.
- `MainActivity.kt` — ruta `qobuz_album/{albumId}` + import.

## Probar
- Buscar álbum -> tap -> `qobuz_album/qb:album:xxx` muestra portada + tracks.
- Volver a Biblioteca local: el álbum ya existe en Room (insert), `AlbumScreen` local sigue igual.
