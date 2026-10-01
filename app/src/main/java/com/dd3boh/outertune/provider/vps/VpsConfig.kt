package com.dd3boh.outertune.provider.vps

/**
 * TubeOther: API propia (vps-music-api). URL y token vienen incluidos;
 * se pueden sobreescribir en Ajustes > TubeOther (VpsCredentialStore).
 * El token va como Bearer en API y como ?token= en stream/cover
 * (ExoPlayer/Coil no mandan headers).
 */
object VpsConfig {
    const val DEFAULT_BASE_URL = "https://13-140-169-157.sslip.io/music-api"
    // Token incluido para uso directo. Si se abusa, se rota en el servidor
    // y se publica APK nueva (la app avisa sola contra /apk/version).
    const val DEFAULT_TOKEN = "7c376546e55f35de2d9d47b37316e698b0640bac0c9ce4fb5712b4b576a478e1"
    const val USER_AGENT = "TubeOther-Android/1.0"
}
