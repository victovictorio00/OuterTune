# FASE 6 — Integración UI

## Cambios
- `ui/screens/search/SearchScreen.kt` — tras `LocalSearchScreen`, si `query>=3` muestra
  `QobuzSearchSection` (misma query, sin tabs nuevos para no romper navegación).
- `ui/screens/search/QobuzSearchScreen.kt` — sección `Online · Qobuz`: tracks (tap=play inmediato)
  + álbumes (tap=`qobuz_album/<id>`). Portadas con Coil `AsyncImage`.
- `ui/screens/search/QobuzAlbumScreen.kt` + ruta `MainActivity.qobuz_album/{albumId}`.
- `ui/screens/settings/QobuzSettings.kt` + `fragments/QobuzFrag.kt` (appId, secret, email,
  password solo-login, botón Guardar y conectar, ciclo de calidad, switch enable).
- `ui/screens/settings/SettingsScreen.kt` — tarjeta Qobuz con icono Cloud.
- `MainActivity.kt` — rutas `settings/qobuz`, `qobuz_album/{albumId}` + imports.
- `res/values/strings-ot.xml` — `qobuz_settings_title/description`.

## Probar
- Ajustes muestra `Qobuz` bajo `Player y audio`.
- Buscar `query>=3` -> aparecen locales arriba + Qobuz abajo.
- Álbum Qobuz -> play track N empieza en N (`startIndex`).
