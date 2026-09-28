package com.dd3boh.outertune.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.provider.toMediaMetadata
import com.dd3boh.outertune.viewmodels.QobuzSearchViewModel

/**
 * FASE 1+6: resultados Qobuz (tracks + álbumes) con metadata y portada.
 * Al reproducir: persiste en Room (vía ViewModel) y encola ListQueue.
 */
@Composable
fun QobuzSearchSection(
    query: String,
    navController: NavController,
    viewModel: QobuzSearchViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val result by viewModel.result.collectAsState()

    LaunchedEffect(query) { viewModel.query.value = query }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Online · Qobuz", style = MaterialTheme.typography.titleMedium)
        if (result.loading) {
            CircularProgressIndicator(Modifier.padding(8.dp))
        }
        result.error?.let { Text("No disponible", style = MaterialTheme.typography.bodySmall) }
        LazyColumn {
            items(result.tracks, key = { it.providerId }) { t ->
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth().clickable {
                        val mm = t.toMediaMetadata()
                        playerConnection.playQueue(ListQueue(title = t.title, items = listOf(mm)))
                    }.padding(8.dp)
                ) {
                    AsyncImage(
                        model = t.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(t.title, style = MaterialTheme.typography.bodyLarge)
                        Text(t.artistName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            items(result.albums, key = { it.providerId }) { a ->
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth().clickable {
                        navController.navigate("qobuz_album/${a.providerId}")
                    }.padding(8.dp)
                ) {
                    AsyncImage(model = a.coverUrl, contentDescription = null)
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(a.title, style = MaterialTheme.typography.bodyLarge)
                        Text(a.artistName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
