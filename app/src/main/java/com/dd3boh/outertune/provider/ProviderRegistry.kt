package com.dd3boh.outertune.provider

/**
 * Registro global + kill-switch.
 * Si un proveedor cae (ej. Qobuz cambia API), se desactiva sin update
 * vía DataStore (ProviderKillSwitch) y la app sigue con el siguiente.
 */
object ProviderRegistry {
    private val providers = mutableListOf<MusicProvider>()
    private val disabled = mutableSetOf<String>()

    fun register(p: MusicProvider) {
        providers.removeAll { it.providerId == p.providerId }
        providers.add(p)
    }

    fun chain(): ProviderChain = ProviderChain(providers.toList())

    fun setDisabled(providerId: String, off: Boolean) {
        if (off) disabled.add(providerId) else disabled.remove(providerId)
    }

    fun isDisabled(providerId: String): Boolean = disabled.contains(providerId)

    fun ids(): List<String> = providers.map { it.providerId }
}
