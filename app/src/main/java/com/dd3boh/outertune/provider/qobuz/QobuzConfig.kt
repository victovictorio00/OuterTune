package com.dd3boh.outertune.provider.qobuz

/** Endpoints y calidades Qobuz. format_id oficial: 5=MP3_320, 6=FLAC_16/44.1, 7=FLAC_24/96, 27=FLAC_24/192. */
object QobuzConfig {
    const val BASE_URL = "https://www.qobuz.com/api.json/0.2"
    // El usuario debe poner su app_id + secret (o user_auth_token) en Ajustes > Qobuz.
    // No se hardcodea secreto por seguridad/rotación. Ver QobuzCredentialStore.
    const val APP_ID_PLACEHOLDER = ""
    const val USER_AGENT = "OuterTune-Qobuz/1.0"
}

enum class QobuzFormat(val formatId: Int, val label: String) {
    MP3_320(5, "MP3 320"),
    FLAC_44_1_16(6, "FLAC 16/44.1"),
    FLAC_96_24(7, "FLAC 24/96"),
    FLAC_192_24(27, "FLAC 24/192"),
}
