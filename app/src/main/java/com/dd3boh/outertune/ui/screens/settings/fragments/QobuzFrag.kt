package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.constants.QobuzAppIdKey
import com.dd3boh.outertune.constants.QobuzAppSecretKey
import com.dd3boh.outertune.constants.QobuzEmailKey
import com.dd3boh.outertune.constants.QobuzEnabledKey
import com.dd3boh.outertune.constants.QobuzQualityKey
import com.dd3boh.outertune.constants.QobuzAudioQuality
import com.dd3boh.outertune.provider.ProviderRegistry
import com.dd3boh.outertune.provider.qobuz.QobuzCredentialStore
import com.dd3boh.outertune.provider.qobuz.QobuzFormat
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.launch

/**
 * FASE 1: login Qobuz + calidad. Sin app_id/token el ProviderChain salta Qobuz en silencio.
 */
@Composable
fun QobuzFrag() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var appId by rememberPreference(QobuzAppIdKey, "")
    var secret by rememberPreference(QobuzAppSecretKey, "")
    var email by rememberPreference(QobuzEmailKey, "")
    var enabled by rememberPreference(QobuzEnabledKey, true)
    var quality by rememberEnumPreference(QobuzQualityKey, QobuzAudioQuality.FLAC_44_1_16)
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        SwitchPreference(
            title = { Text("Activar Qobuz") },
            checked = enabled,
            onCheckedChange = {
                enabled = it
                ProviderRegistry.setDisabled("qobuz", !it)
            }
        )
        OutlinedTextField(value = appId, onValueChange = { appId = it }, label = { Text("App ID Qobuz") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = secret, onValueChange = { secret = it }, label = { Text("App Secret (opcional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña (solo login)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                try {
                    val store = QobuzCredentialStore(context)
                    store.save(appId, secret, store.userToken(), email)
                    val provider = com.dd3boh.outertune.provider.qobuz.QobuzProvider(store)
                    provider.login(email, password)
                    status = "Login OK"
                    password = ""
                } catch (e: Exception) {
                    status = "Error: ${e.message?.take(200)}"
                }
            }
        }, modifier = Modifier.padding(top = 8.dp)) { Text("Guardar y conectar") }
        if (status.isNotEmpty()) Text(status, Modifier.padding(top = 8.dp))
        PreferenceEntry(
            title = { Text("Calidad: ${quality.name} -> ${runCatching { QobuzFormat.valueOf(quality.name) }.getOrNull()}") },
            onClick = {
                // Cicla calidades para no depender de EnumListPreference
                val vals = QobuzAudioQuality.entries
                quality = vals[(vals.indexOf(quality) + 1) % vals.size]
            }
        )
    }
}
