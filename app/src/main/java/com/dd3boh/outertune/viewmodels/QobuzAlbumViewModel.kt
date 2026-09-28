package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.provider.ProviderCache
import com.dd3boh.outertune.provider.ProviderRegistry
import com.dd3boh.outertune.provider.UnifiedAlbum
import com.dd3boh.outertune.provider.UnifiedTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QobuzAlbumState(
    val album: UnifiedAlbum? = null,
    val tracks: List<UnifiedTrack> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class QobuzAlbumViewModel @Inject constructor(
    private val cache: ProviderCache,
) : ViewModel() {
    private val _state = MutableStateFlow(QobuzAlbumState())
    val state: StateFlow<QobuzAlbumState> = _state.asStateFlow()

    fun load(albumId: String) {
        viewModelScope.launch {
            _state.value = QobuzAlbumState(loading = true)
            val hit = ProviderRegistry.chain().getAlbumFirstHit(albumId)
            if (hit != null) {
                cache.persistAlbum(hit.first, hit.second)
                _state.value = QobuzAlbumState(hit.first, hit.second, false)
            } else {
                _state.value = QobuzAlbumState(loading = false)
            }
        }
    }
}
