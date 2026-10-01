package com.dd3boh.outertune.provider.vps

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dd3boh.outertune.constants.VpsBaseUrlKey
import com.dd3boh.outertune.constants.VpsEnabledKey
import com.dd3boh.outertune.constants.VpsTokenKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** URL/token del servidor propio. Vacío = valores incluidos en la app. */
@Singleton
class VpsCredentialStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun baseUrl(): String {
        val v = context.dataStore.get(VpsBaseUrlKey, "")
        return v.ifEmpty { VpsConfig.DEFAULT_BASE_URL }
    }

    fun token(): String {
        val v = context.dataStore.get(VpsTokenKey, "")
        return v.ifEmpty { VpsConfig.DEFAULT_TOKEN }
    }

    fun enabled(): Boolean = context.dataStore.get(VpsEnabledKey, true)

    fun api(): VpsApi = VpsApi(baseUrl().trimEnd('/'), token())

    suspend fun save(url: String, token: String) {
        context.dataStore.edit {
            it[VpsBaseUrlKey] = url
            it[VpsTokenKey] = token
        }
    }
}
