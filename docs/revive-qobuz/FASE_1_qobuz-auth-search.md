# FASE 1 — Qobuz: auth, buscador, metadata, portada, álbum

## Archivos
- `provider/qobuz/QobuzConfig.kt` — BASE_URL + `QobuzFormat` (5,6,7,27).
- `provider/qobuz/QobuzApi.kt` — HttpURLConnection + org.json, sin dependencias nuevas.
  `login / catalog/search / album/get / track/getFileUrl`.
- `provider/qobuz/QobuzCredentialStore.kt` — DataStore: `QobuzAppIdKey`, `QobuzAppSecretKey`,
  `QobuzAuthTokenKey`, `QobuzEmailKey`, `QobuzQualityKey`.
- `provider/qobuz/QobuzMapper.kt` — `qb:track|album|artist`, cover `mega>large>medium>small`,
  año desde `release_date_original`, ISRC, track/disc number.
- `provider/qobuz/QobuzProvider.kt` — `searchTracks/SearchAlbums/getAlbum/resolveStream`
  con cascada de calidad `wanted -> 27,7,6,5`. Sin app_id -> `isConfigured=false`.
- `constants/PreferenceKeys.kt` — `Qobuz*Key` + `ProviderKillSwitchKey`.
- `constants/Settings.kt` — `QobuzAudioQuality`.
- `viewmodels/QobuzSearchViewModel.kt` — debounce 400ms, `ProviderChain.searchTracksMerged`,
  persiste en Room, error genérico `No disponible`.
- `App.kt` — registra Qobuz + Deezer al arranque + aplica kill-switch.

## Cómo probar
1. Ajustes > Qobuz > poner App ID (+ secret opcional) > email/pass > Guardar y conectar.
2. Buscar (>=3 letras) -> sección `Online · Qobuz` con tracks + álbumes, portadas Coil.
3. Sin credenciales: la sección sale vacía, sin crash (failover silencioso).
