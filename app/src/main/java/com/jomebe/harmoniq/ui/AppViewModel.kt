package com.jomebe.harmoniq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jomebe.harmoniq.AppContainer
import com.jomebe.harmoniq.data.repository.MusicRepository
import com.jomebe.harmoniq.domain.PlaybackQueue
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.player.PlaybackConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryState(
    val history: List<Track> = emptyList(),
    val saved: List<Track> = emptyList()
)

data class AppUiState(
    val popular: List<Track> = emptyList(),
    val personalized: List<Track> = emptyList(),
    val searchResults: List<Track> = emptyList(),
    val searchQuery: String = "",
    val queue: PlaybackQueue = PlaybackQueue(),
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppViewModel(
    private val repository: MusicRepository,
    private val playback: PlaybackConnection
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    val library = combine(repository.observeHistory(), repository.observeSaved(), ::LibraryState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState())

    init {
        refreshHome()
        viewModelScope.launch {
            var lastId: String? = null
            playback.currentMediaId.collect { id ->
                if (id == null) return@collect
                val queue = _uiState.value.queue
                val index = queue.tracks.indexOfFirst { it.id == id }
                if (index >= 0) {
                    _uiState.value = _uiState.value.copy(queue = queue.copy(currentIndex = index))
                    if (lastId != id) {
                        lastId?.let { oldId -> queue.tracks.firstOrNull { it.id == oldId } }
                            ?.let { repository.markCompleted(it) }
                        queue.tracks[index].let { repository.markStarted(it) }
                        lastId = id
                    }
                }
            }
        }
        viewModelScope.launch {
            playback.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isPlaying = playing)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        launchLoading {
            _uiState.value = _uiState.value.copy(searchResults = repository.search(query))
        }
    }

    fun refreshHome() = launchLoading {
        val popular = repository.popular()
        val personalized = runCatching { repository.personalized() }.getOrDefault(popular.shuffled())
        _uiState.value = _uiState.value.copy(popular = popular, personalized = personalized)
    }

    fun play(track: Track, source: List<Track>) {
        val queue = _uiState.value.queue.start(track, source)
        _uiState.value = _uiState.value.copy(queue = queue)
        playback.playQueue(queue.tracks, queue.currentIndex)
    }

    fun togglePlayback() = playback.togglePlayback()

    fun playNext() {
        val queue = _uiState.value.queue
        if (queue.hasNext) playback.next() else extendQueueAndPlay()
    }

    fun playPrevious() = playback.previous()

    fun save(track: Track) = viewModelScope.launch { repository.save(track) }
    fun unsave(track: Track) = viewModelScope.launch { repository.unsave(track.id) }
    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
        refreshHome()
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun extendQueueAndPlay() {
        val current = _uiState.value.queue.current ?: return
        viewModelScope.launch {
            val additions = runCatching { repository.related(current) }.getOrDefault(emptyList())
            if (additions.isNotEmpty()) {
                val old = _uiState.value.queue
                val tracks = (old.tracks + additions).distinctBy(Track::id)
                val nextIndex = (old.currentIndex + 1).coerceAtMost(tracks.lastIndex)
                val extended = PlaybackQueue(tracks, nextIndex)
                _uiState.value = _uiState.value.copy(queue = extended)
                playback.playQueue(extended.tracks, extended.currentIndex)
            }
        }
    }

    private fun launchLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { block() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "요청을 처리하지 못했습니다.") }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(container.repository, container.playback) as T
        }
    }
}
