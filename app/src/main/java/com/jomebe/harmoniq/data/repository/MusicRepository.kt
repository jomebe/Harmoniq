package com.jomebe.harmoniq.data.repository

import com.jomebe.harmoniq.BuildConfig
import com.jomebe.harmoniq.data.local.HarmoniqDao
import com.jomebe.harmoniq.data.local.LocalMusicDataSource
import com.jomebe.harmoniq.data.local.toHistoryEntity
import com.jomebe.harmoniq.data.local.toSavedEntity
import com.jomebe.harmoniq.data.remote.YouTubeApi
import com.jomebe.harmoniq.data.remote.YouTubeItem
import com.jomebe.harmoniq.domain.Artist
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val api: YouTubeApi,
    private val localMusic: LocalMusicDataSource,
    private val dao: HarmoniqDao,
    private val recommendationEngine: RecommendationEngine
) {
    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    fun observeHistory(): Flow<List<Track>> = dao.observeHistory().map { it.distinctBy { row -> row.trackId }.map { row -> row.toTrack() } }
    fun observeSaved(): Flow<List<Track>> = dao.observeSavedTracks().map { it.map { row -> row.toTrack() } }

    suspend fun popular(): List<Track> = coroutineScope {
        listOf("popular music", "official music video", "new music").map { query ->
            async { requireKey { api.searchVideos(apiKey, query = query).items.mapNotNull(::toTrack) } }
        }.flatMap { it.await() }.distinctBy(Track::id).take(120)
    }

    suspend fun search(query: String): List<Track> = coroutineScope {
        val trimmed = query.trim()
        val local = async { localMusic.search(trimmed) }
        val videos = async {
            requireKey {
                api.searchVideos(apiKey, query = trimmed).items.mapNotNull(::toTrack)
                    .sortedByDescending { track -> isOfficialArtistResult(track, trimmed) }
            }
        }
        (local.await() + videos.await()).distinctBy(Track::id)
    }

    suspend fun artists(query: String): List<Artist> = requireKey {
        api.searchChannels(apiKey, query = query.trim()).items.mapNotNull { item ->
            item.id.channelId?.let { Artist(it, item.snippet.channelTitle, thumbnail(item)) }
        }.sortedByDescending { it.name.equals(query.trim(), ignoreCase = true) }
    }

    suspend fun tracksForArtist(artist: Artist): List<Track> = requireKey {
        api.searchVideos(apiKey, query = "${artist.name} official music").items.mapNotNull(::toTrack)
    }

    suspend fun personalized(): List<Track> = coroutineScope {
        val recent = dao.recentHistory().map { it.toTrack() }
        if (recent.isEmpty()) return@coroutineScope popular().shuffled()
        val candidates = recommendationEngine.seedQueries(recent).take(4)
            .map { query -> async { runCatching { search(query) }.getOrDefault(emptyList()) } }.flatMap { it.await() }
        recommendationEngine.rank(candidates, recent).take(40)
    }

    suspend fun related(track: Track): List<Track> = search(track.tags.firstOrNull() ?: track.artist).filterNot { it.id == track.id }
    suspend fun markStarted(track: Track) = dao.insertHistory(track.toHistoryEntity(false))
    suspend fun markCompleted(track: Track) = dao.insertHistory(track.toHistoryEntity(true))
    suspend fun save(track: Track) = dao.save(track.toSavedEntity())
    suspend fun unsave(trackId: String) = dao.unsave(trackId)
    suspend fun clearHistory() = dao.clearHistory()

    private fun toTrack(item: YouTubeItem): Track? {
        val videoId = item.id.videoId ?: return null
        return Track(
            id = "youtube:$videoId", title = item.snippet.title, artist = item.snippet.channelTitle,
            thumbnailUrl = thumbnail(item), externalUrl = "https://www.youtube.com/watch?v=$videoId",
            tags = listOf("YouTube")
        )
    }

    private fun thumbnail(item: YouTubeItem): String = item.snippet.thumbnails.high?.url
        ?: item.snippet.thumbnails.medium?.url ?: item.snippet.thumbnails.default?.url.orEmpty()

    private fun isOfficialArtistResult(track: Track, query: String): Boolean {
        val artist = track.artist.lowercase()
        val normalizedQuery = query.lowercase()
        return artist == normalizedQuery || artist == "$normalizedQuery - topic" ||
            artist.contains(normalizedQuery) && track.title.lowercase().contains("official")
    }

    private suspend inline fun <T> requireKey(block: suspend () -> T): T {
        check(apiKey.isNotBlank()) { "YouTube API 키가 설정되지 않았습니다." }
        return block()
    }
}
