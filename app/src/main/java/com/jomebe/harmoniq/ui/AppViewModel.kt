package com.jomebe.harmoniq.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jomebe.harmoniq.AppContainer
import com.jomebe.harmoniq.auth.AuthState
import com.jomebe.harmoniq.data.repository.MusicRepository
import com.jomebe.harmoniq.domain.PlaybackQueue
import com.jomebe.harmoniq.domain.Track
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
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppViewModel(
    private val repository: MusicRepository,
    val authState: StateFlow<AuthState>,
    private val container: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    val library = combine(repository.observeHistory(), repository.observeSaved(), ::LibraryState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState())

    init {
        refreshHome()
        viewModelScope.launch {
            container.authManager.restoreSession()
            if (authState.value is AuthState.SignedIn) refreshHome()
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
        val personalized = runCatching { repository.personalized() }.getOrDefault(popular.shuffled().take(20))
        _uiState.value = _uiState.value.copy(popular = popular, personalized = personalized)
    }

    fun play(track: Track, source: List<Track>) {
        _uiState.value = _uiState.value.copy(queue = _uiState.value.queue.start(track, source))
        viewModelScope.launch { repository.markStarted(track) }
    }

    fun playNext() {
        val state = _uiState.value
        if (state.queue.hasNext) {
            val next = state.queue.next()
            _uiState.value = state.copy(queue = next)
            next.current?.let { viewModelScope.launch { repository.markStarted(it) } }
        } else {
            extendQueueAndPlay()
        }
    }

    fun playPrevious() {
        val queue = _uiState.value.queue.previous()
        _uiState.value = _uiState.value.copy(queue = queue)
        queue.current?.let { viewModelScope.launch { repository.markStarted(it) } }
    }

    fun onTrackEnded() {
        _uiState.value.queue.current?.let { current ->
            viewModelScope.launch {
                repository.markCompleted(current)
                playNext()
            }
        }
    }

    fun save(track: Track) = viewModelScope.launch { repository.save(track) }
    fun unsave(track: Track) = viewModelScope.launch { repository.unsave(track.id) }
    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
        refreshHome()
    }

    fun signInIntent(): Intent = container.authManager.signInIntent()
    fun completeSignIn(data: Intent?) = viewModelScope.launch {
        container.authManager.completeSignIn(data)
        if (authState.value is AuthState.SignedIn) refreshHome()
    }
    fun retryAuthorization() = viewModelScope.launch { container.authManager.retryToken() }
    fun signOut() = viewModelScope.launch { container.authManager.signOut() }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun extendQueueAndPlay() {
        val current = _uiState.value.queue.current ?: return
        viewModelScope.launch {
            val additions = runCatching { repository.related(current) }.getOrDefault(emptyList())
            if (additions.isNotEmpty()) {
                val old = _uiState.value.queue
                val extended = old.copy(tracks = (old.tracks + additions).distinctBy(Track::id)).next()
                _uiState.value = _uiState.value.copy(queue = extended)
                extended.current?.let { repository.markStarted(it) }
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
                AppViewModel(container.repository, container.authManager.state, container) as T
        }
    }
}
