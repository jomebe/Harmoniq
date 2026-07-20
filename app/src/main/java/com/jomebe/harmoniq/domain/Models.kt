package com.jomebe.harmoniq.domain

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationText: String = "",
    val publishedAt: String = "",
    val tags: List<String> = emptyList()
)

data class ArtistSeed(
    val channelId: String,
    val name: String,
    val thumbnailUrl: String = ""
)

data class PlaybackQueue(
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = -1
) {
    val current: Track? get() = tracks.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex in 0 until tracks.lastIndex

    fun start(track: Track, source: List<Track>): PlaybackQueue {
        val uniqueSource = source.distinctBy(Track::id)
        val selectedIndex = uniqueSource.indexOfFirst { it.id == track.id }
        val ordered = if (selectedIndex >= 0) {
            uniqueSource.drop(selectedIndex) + uniqueSource.take(selectedIndex)
        } else {
            listOf(track) + uniqueSource
        }
        return PlaybackQueue(ordered.distinctBy(Track::id), 0)
    }

    fun next(): PlaybackQueue = if (hasNext) copy(currentIndex = currentIndex + 1) else this
    fun previous(): PlaybackQueue = if (currentIndex > 0) copy(currentIndex = currentIndex - 1) else this
}

data class UserProfile(
    val displayName: String,
    val email: String,
    val photoUrl: String?
)
