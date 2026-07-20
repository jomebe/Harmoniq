package com.jomebe.harmoniq.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlaybackConnection(context: Context) : Player.Listener {
    private val appContext = context.applicationContext
    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
    ).buildAsync()
    private var controller: MediaController? = null

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        controllerFuture.addListener({
            runCatching { controllerFuture.get() }.getOrNull()?.let {
                controller = it
                it.addListener(this)
                publishState(it)
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playQueue(tracks: List<Track>, startIndex: Int) = withController { player ->
        val playable = tracks.filter { it.streamUrl.isNotBlank() }
        if (playable.isEmpty()) return@withController
        val selectedId = tracks.getOrNull(startIndex)?.id
        val playableIndex = playable.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
        player.setMediaItems(playable.map { it.toMediaItem() }, playableIndex, C.TIME_UNSET)
        player.prepare()
        player.play()
    }

    fun togglePlayback() = withController { if (it.isPlaying) it.pause() else it.play() }
    fun next() = withController { if (it.hasNextMediaItem()) it.seekToNextMediaItem() }
    fun previous() = withController {
        if (it.currentPosition > 5_000) it.seekTo(0)
        else if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        _currentMediaId.value = mediaItem?.mediaId
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    private fun publishState(player: Player) {
        _currentMediaId.value = player.currentMediaItem?.mediaId
        _isPlaying.value = player.isPlaying
    }

    private fun withController(action: (MediaController) -> Unit) {
        controller?.let(action) ?: controllerFuture.addListener({
            runCatching { controllerFuture.get() }.getOrNull()?.let(action)
        }, ContextCompat.getMainExecutor(appContext))
    }

    private fun Track.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(thumbnailUrl.takeIf(String::isNotBlank)?.let(Uri::parse))
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }
}
