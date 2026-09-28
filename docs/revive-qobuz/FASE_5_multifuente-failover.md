# FASE 5 — Multifuente con failover invisible

## Cambios
- `provider/MusicProvider.kt` — contrato único para Qobuz/Deezer/Apple-futuro.
- `provider/ProviderChain.kt`:
  - `searchTracksMerged`: dedup por `title|artist|dur/2`, corta al primer proveedor con hits.
  - `resolveStreamSilent`: prueba en orden con `withTimeoutOrNull(15s)`, log `ProviderChain`,
    excepción final genérica.
  - `getAlbumFirstHit`, `searchAlbumsMerged` igual.
- `provider/ProviderRegistry.kt` — `register/chain/setDisabled/isDisabled`.
- `provider/deezer/DeezerProvider.kt` — stub `isConfigured=false` (listo para implementar ARL).
- `App.kt` — aplica `ProviderKillSwitchKey` (csv) al arranque.
- `constants/PreferenceKeys.kt` — `QobuzEnabledKey`, `ProviderKillSwitchKey`.
- `ui/.../QobuzFrag.kt` — switch `Activar Qobuz` -> `Registry.setDisabled("qobuz", !it)`.

## Regla UX
- Nunca se muestra `Qobuz falló, probando Deezer`. Solo `No disponible` si todo falla.
- Logs con `providerId` solo en logcat para diagnóstico.

## Añadir un proveedor nuevo
1. Implementar `MusicProvider` (+ mapper a `Unified*` con prefijo propio `xx:track:`).
2. `ProviderRegistry.register()` en `App.onCreate`.
3. Sin cambios en UI/playback/descargas.
