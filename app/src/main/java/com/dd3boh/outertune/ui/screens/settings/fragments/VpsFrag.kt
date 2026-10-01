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
import com.dd3boh.outertune.constants.VpsBaseUrlKey
import com.dd3boh.outertune.constants.VpsEnabledKey
import com.dd3boh.outertune.constants.VpsTokenKey
import com.dd3boh.outertune.provider.ProviderRegistry
import com.dd3boh.outertune.provider.vps.VpsConfig
import com.dd3boh.outertune.provider.vps.VpsCredentialStore
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.launch

/**
 * Ajustes TubeOther: el servidor ya viene configurado; aquí se puede
 * apuntar a otro servidor o rotar el token, y probar la conexión.
 */
@Composable
fun VpsFrag() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by rememberPreference(VpsBaseUrlKey, "")
    var token by rememberPreference(VpsTokenKey, "")
    var enabled by rememberPreference(VpsEnabledKey, true)
    var status by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        SwitchPreference(
            title = { Text("Activar TubeOther") },
            checked = enabled,
            onCheckedChange = {
                enabled = it
                ProviderRegistry.setDisabled("vps", !it)
            }
        )
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            label = { Text("Servidor (vacío = ${VpsConfig.DEFAULT_BASE_URL})") },
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = token, onValueChange = { token = it },
            label = { Text("Token (vacío = incluido)") },
            modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                status = try {
                    val store = VpsCredentialStore(context)
                    val hits = store.api().searchTracks("test", 1)
                    "Conexión OK (${hits.optJSONArray("tracks")?.length() ?: 0} resultado(s))"
                } catch (e: Exception) {
                    "Error: ${e.message?.take(200)}"
                }
            }
        }, modifier = Modifier.padding(top = 8.dp)) { Text("Probar conexión") }
        if (status.isNotEmpty()) Text(status, Modifier.padding(top = 8.dp))
        PreferenceEntry(
            title = { Text("Buscar APKs nuevas al abrir (aviso con descarga)") },
            description = "Compara contra /apk/version del servidor",
            onClick = { }
        )
    }
}
