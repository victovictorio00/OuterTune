package com.dd3boh.outertune.provider.qobuz

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dd3boh.outertune.constants.QobuzAppIdKey
import com.dd3boh.outertune.constants.QobuzAppSecretKey
import com.dd3boh.outertune.constants.QobuzAuthTokenKey
import com.dd3boh.outertune.constants.QobuzEmailKey
import com.dd3boh.outertune.constants.QobuzQualityKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Guarda credenciales Qobuz en DataStore (settings). El token es sensible: no se exporta en backups. */
@Singleton
class QobuzCredentialStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun appId(): String = context.dataStore.get(QobuzAppIdKey, "")
    fun appSecret(): String = context.dataStore.get(QobuzAppSecretKey, "")
    fun userToken(): String = context.dataStore.get(QobuzAuthTokenKey, "")
    fun email(): String = context.dataStore.get(QobuzEmailKey, "")
    fun qualityName(): String = context.dataStore.get(QobuzQualityKey, QobuzFormat.FLAC_44_1_16.name)

    fun quality(): QobuzFormat = try {
        QobuzFormat.valueOf(qualityName())
    } catch (_: Exception) { QobuzFormat.FLAC_44_1_16 }

    suspend fun save(appId: String, secret: String, token: String, email: String) {
        context.dataStore.edit {
            it[QobuzAppIdKey] = appId
            it[QobuzAppSecretKey] = secret
            it[QobuzAuthTokenKey] = token
            it[QobuzEmailKey] = email
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[QobuzAuthTokenKey] = token }
    }

    suspend fun observeToken() = context.dataStore.data.map { it[QobuzAuthTokenKey].orEmpty() }.first()
}
