package com.dd3boh.outertune.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.viewmodels.QobuzAlbumViewModel

/** FASE 3: detalle álbum Qobuz (portada + metadata + tracks). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QobuzAlbumScreen(
    albumId: String,
    navController: NavController,
    viewModel: QobuzAlbumViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val playerConnection = LocalPlayerConnection.current
    LaunchedEffect(albumId) { viewModel.load(albumId) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.album?.title ?: "Álbum") },
            navigationIcon = { IconButton(onClick = navController::navigateUp) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
        )
        if (state.loading) {
            CircularProgressIndicator(Modifier.padding(16.dp))
            return@Column
        }
        state.album?.let { a ->
            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                AsyncImage(model = a.coverUrl, contentDescription = null)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(a.title)
                    Text(a.artistName)
                    Text("${a.releaseYear ?: ""} · ${a.trackCount ?: state.tracks.size} temas")
                }
            }
        }
        LazyColumn {
            items(state.tracks, key = { it.providerId }) { t ->
                Row(Modifier.fillMaxWidth().clickable {
                    val items = state.tracks.map { it.toMediaMetadata() }
                    val idx = state.tracks.indexOfFirst { it.providerId == t.providerId }
                    playerConnection?.playQueue(ListQueue(title = state.album?.title ?: t.title, items = items, startIndex = idx.coerceAtLeast(0)))
                }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("${t.trackNumber ?: ""}. ", Modifier.padding(end = 8.dp))
                    Column {
                        Text(t.title)
                        Text(t.artistName)
                    }
                }
            }
        }
    }
}
