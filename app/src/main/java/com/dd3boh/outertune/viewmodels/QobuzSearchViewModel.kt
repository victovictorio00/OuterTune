package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.provider.ProviderCache
import com.dd3boh.outertune.provider.ProviderRegistry
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QobuzSearchResult(
    val tracks: List<UnifiedTrack> = emptyList(),
    val albums: List<UnifiedAlbum> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class QobuzSearchViewModel @Inject constructor(
    private val cache: ProviderCache,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _result = MutableStateFlow(QobuzSearchResult())
    val result: StateFlow<QobuzSearchResult> = _result.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            query.debounce(400).distinctUntilChanged().collect { q ->
                if (q.isBlank()) {
                    _result.value = QobuzSearchResult()
                    return@collect
                }
                _result.value = _result.value.copy(loading = true, error = null)
                try {
                    val chain = ProviderRegistry.chain()
                    val tracks = chain.searchTracksMerged(q, 25)
                    val albums = chain.searchAlbumsMerged(q, 15)
                    cache.persistTracks(tracks)
                    _result.value = QobuzSearchResult(tracks, albums, false, null)
                } catch (e: Exception) {
                    // Invisible al usuario: mensaje genérico
                    _result.value = _result.value.copy(loading = false, error = "No disponible")
                }
            }
        }
    }
}
