# Revivir OuterTune con Qobuz + failover invisible — Índice

> OuterTune 0.11.0-a2 quedó solo-local tras el nuke de `innertube/` (`2ca4acea`, `5e575e24`).
> `MusicService.createDataSourceFactory()` hoy lanza `No datasource for song` para todo lo no-local.
> Este plan conserva OuterTune como **solo UI + DB local + ExoPlayer** y añade una capa `provider/`
> con Qobuz como fuente principal y cadena con failover silencioso (Deezer/otros a futuro).

## Fases

- `FASE_0_base-y-arquitectura.md` — diagnóstico + nueva arquitectura + contratos.
- `FASE_1_qobuz-auth-search.md` — credenciales, login, búsqueda, metadata, portadas.
- `FASE_2_playback-streaming.md` — resolver `streamUrl` en `MusicService`, caché Exo.
- `FASE_3_album-metadata-cover.md` — detalle álbum, persistencia Room, covers.
- `FASE_4_descarga-offline.md` — descarga permanente con `DownloadManagerOt`, taggeo.
- `FASE_5_multifuente-failover.md` — `ProviderChain`, Deezer stub, kill-switch invisible.
- `FASE_6_ui-integracion.md` — Search tabs, rutas, ajustes Qobuz.
- `CAMBIOS.md` — log de cada archivo creado/modificado en esta implementación.

## Regla de UX

El usuario nunca elige proveedor ni ve errores de proveedor. Solo ve:
`Reproduciendo / Descargando / No disponible`. Los fallos se loguean en `ProviderChain`.
