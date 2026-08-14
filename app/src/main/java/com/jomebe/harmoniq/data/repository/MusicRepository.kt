package com.jomebe.harmoniq.data.repository

import androidx.core.text.HtmlCompat
import com.jomebe.harmoniq.BuildConfig
import com.jomebe.harmoniq.data.local.HarmoniqDao
import com.jomebe.harmoniq.data.local.LocalMusicDataSource
import com.jomebe.harmoniq.data.local.toHistoryEntity
import com.jomebe.harmoniq.data.local.toSavedEntity
import com.jomebe.harmoniq.data.remote.YouTubeApi
import com.jomebe.harmoniq.data.remote.YouTubeItem
import com.jomebe.harmoniq.data.remote.YouTubeSnippet
import com.jomebe.harmoniq.data.remote.YouTubeVideoItem
import com.jomebe.harmoniq.domain.Artist
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

class MusicRepository(
    private val api: YouTubeApi,
    private val localMusic: LocalMusicDataSource,
    private val dao: HarmoniqDao,
    private val recommendationEngine: RecommendationEngine
) {
    private val apiKey = BuildConfig.YOUTUBE_API_KEY
    private val searchMutex = Mutex()
    private val searchCache = linkedMapOf<String, CachedSearch>()
    private val artistTrackCache = linkedMapOf<String, CachedSearch>()
    @Volatile private var lastRemoteError: String? = null

    fun observeHistory(): Flow<List<Track>> = dao.observeHistory().map { it.distinctBy { row -> row.trackId }.map { row -> row.toTrack() } }
    fun observeSaved(): Flow<List<Track>> = dao.observeSavedTracks().map { it.map { row -> row.toTrack() } }

    suspend fun popular(): List<Track> {
        val ytTracks = runCatching {
            requireKey { api.popularMusic(apiKey).items.mapNotNull(::toTrack) }
        }.getOrDefault(emptyList())

        return if (ytTracks.isNotEmpty()) ytTracks else localMusic.search("")
    }

    suspend fun search(query: String): List<Track> = coroutineScope {
        val trimmed = query.trim()
        val local = async { localMusic.search(trimmed) }
        val videos = async {
            searchItems(trimmed).mapNotNull(::toTrack)
                .sortedByDescending { track -> isOfficialArtistResult(track, trimmed) }
        }
        (local.await() + videos.await()).distinctBy(Track::id)
    }

    suspend fun artists(query: String): List<Artist> = searchItems(query.trim())
        .mapNotNull { item ->
            item.snippet.channelId.takeIf(String::isNotBlank)
                ?.let { Artist(it, item.snippet.channelTitle, thumbnail(item.snippet)) }
        }
        .distinctBy(Artist::id)
        .sortedByDescending { it.name.equals(query.trim(), ignoreCase = true) }

    suspend fun tracksForArtist(artist: Artist): List<Track> = searchMutex.withLock {
        val now = System.currentTimeMillis()
        val fromSearch = searchCache.values.asSequence()
            .filter { it.expiresAt > now }
            .flatMap { it.items.asSequence() }
            .filter { it.snippet.channelId == artist.id }
            .mapNotNull(::toTrack)
            .distinctBy(Track::id)
            .toList()
        if (fromSearch.isNotEmpty()) return@withLock fromSearch

        artistTrackCache[artist.id]?.takeIf { it.expiresAt > now }?.let { cached ->
            lastRemoteError = cached.errorMessage
            return@withLock cached.items.mapNotNull(::toTrack)
        }

        val result = runCatching {
            requireKey { api.searchVideos(apiKey, query = "official music", channelId = artist.id).items }
        }
        val errorMessage = result.exceptionOrNull()?.let(::remoteErrorMessage)
        val items = result.getOrDefault(emptyList())
        lastRemoteError = errorMessage
        artistTrackCache[artist.id] = CachedSearch(
            items = items,
            expiresAt = now + if (result.isSuccess) SUCCESS_CACHE_MS else ERROR_CACHE_MS,
            errorMessage = errorMessage
        )
        items.mapNotNull(::toTrack)
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

    fun consumeRemoteError(): String? = lastRemoteError.also { lastRemoteError = null }

    private fun toTrack(item: YouTubeItem): Track? {
        val videoId = item.id.videoId ?: return null
        return toTrack(videoId, item.snippet)
    }

    private fun toTrack(item: YouTubeVideoItem): Track? = toTrack(item.id, item.snippet)

    private fun toTrack(videoId: String, snippet: YouTubeSnippet): Track {
        return Track(
            id = "youtube:$videoId",
            title = HtmlCompat.fromHtml(snippet.title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            artist = snippet.channelTitle,
            thumbnailUrl = thumbnail(snippet),
            streamUrl = "",
            externalUrl = "https://www.youtube.com/watch?v=$videoId",
            tags = listOf("YouTube")
        )
    }

    private fun thumbnail(snippet: YouTubeSnippet): String = snippet.thumbnails.high?.url
        ?: snippet.thumbnails.medium?.url ?: snippet.thumbnails.default?.url.orEmpty()

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

    private suspend fun searchItems(query: String): List<YouTubeItem> = searchMutex.withLock {
        val normalized = query.trim().lowercase()
        val now = System.currentTimeMillis()
        searchCache[normalized]?.takeIf { it.expiresAt > now }?.let { cached ->
            lastRemoteError = cached.errorMessage
            return@withLock cached.items
        }

        val result = runCatching {
            requireKey { api.searchVideos(apiKey, query = query.trim()).items }
        }
        val errorMessage = result.exceptionOrNull()?.let(::remoteErrorMessage)
        val items = result.getOrDefault(emptyList())
        lastRemoteError = errorMessage
        searchCache[normalized] = CachedSearch(
            items = items,
            expiresAt = now + if (result.isSuccess) SUCCESS_CACHE_MS else ERROR_CACHE_MS,
            errorMessage = errorMessage
        )
        while (searchCache.size > MAX_SEARCH_CACHE_SIZE) {
            searchCache.remove(searchCache.keys.first())
        }
        items
    }

    private fun remoteErrorMessage(error: Throwable): String = when ((error as? HttpException)?.code()) {
        429 -> "오늘의 YouTube 검색 한도를 모두 사용했습니다. 미국 태평양 자정에 갱신됩니다. 그 전까지 내 기기 음악을 이용해 주세요."
        403 -> "YouTube 검색 사용량이 소진됐습니다. 한도가 갱신될 때까지 내 기기 음악을 이용해 주세요."
        else -> "YouTube 검색에 연결하지 못했습니다. 내 기기 음악 결과만 표시합니다."
    }

    private data class CachedSearch(
        val items: List<YouTubeItem>,
        val expiresAt: Long,
        val errorMessage: String?
    )

    private companion object {
        const val MAX_SEARCH_CACHE_SIZE = 20
        const val SUCCESS_CACHE_MS = 15 * 60 * 1000L
        const val ERROR_CACHE_MS = 5 * 60 * 1000L
    }
}
