package com.jomebe.harmoniq.data.repository

import android.text.Html
import com.jomebe.harmoniq.BuildConfig
import com.jomebe.harmoniq.data.local.HarmoniqDao
import com.jomebe.harmoniq.data.local.toHistoryEntity
import com.jomebe.harmoniq.data.local.toSavedEntity
import com.jomebe.harmoniq.data.remote.SubscriptionsResponse
import com.jomebe.harmoniq.data.remote.VideoItem
import com.jomebe.harmoniq.data.remote.YouTubeApi
import com.jomebe.harmoniq.domain.ArtistSeed
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration

class MissingApiKeyException : IllegalStateException("local.properties에 YOUTUBE_API_KEY를 설정해주세요.")

class MusicRepository(
    private val api: YouTubeApi,
    private val dao: HarmoniqDao,
    private val recommendationEngine: RecommendationEngine
) {
    private val apiKey get() = BuildConfig.YOUTUBE_API_KEY.ifBlank { throw MissingApiKeyException() }

    fun observeHistory(): Flow<List<Track>> = dao.observeHistory().map { rows -> rows.distinctBy { it.videoId }.map { it.toTrack() } }
    fun observeSaved(): Flow<List<Track>> = dao.observeSavedTracks().map { rows -> rows.map { it.toTrack() } }
    fun observeIsSaved(videoId: String): Flow<Boolean> = dao.observeIsSaved(videoId)

    suspend fun popular(): List<Track> = api.popularMusic(apiKey = apiKey).items.map(::toTrack)

    suspend fun search(query: String): List<Track> {
        val result = api.search(query = query.trim(), apiKey = apiKey)
        val ids = result.items.mapNotNull { it.id.videoId }
        if (ids.isEmpty()) return emptyList()
        val details = api.videosByIds(ids = ids.joinToString(","), apiKey = apiKey).items.associateBy(VideoItem::id)
        return ids.mapNotNull { id -> details[id]?.let(::toTrack) }
    }

    suspend fun liked(): List<Track> = api.likedVideos(apiKey = apiKey).items.map(::toTrack)

    suspend fun subscriptions(): List<ArtistSeed> = api.subscriptions(apiKey = apiKey).toSeeds()

    suspend fun personalized(): List<Track> = coroutineScope {
        val recent = dao.recentHistory().map { it.toTrack() }
        val likedDeferred = async { runCatching { liked() }.getOrDefault(emptyList()) }
        val subscriptionsDeferred = async { runCatching { subscriptions() }.getOrDefault(emptyList()) }
        val liked = likedDeferred.await()
        val subscriptions = subscriptionsDeferred.await()
        val queries = recommendationEngine.seedQueries(recent, liked, subscriptions)
        val candidates = queries.take(4).map { query -> async { runCatching { search(query) }.getOrDefault(emptyList()) } }
            .flatMap { it.await() }
        recommendationEngine.rank(candidates, recent, liked, subscriptions.map(ArtistSeed::name).toSet()).take(30)
    }

    suspend fun related(track: Track): List<Track> {
        val query = "${track.artist} ${cleanTitle(track.title)} music"
        return search(query).filterNot { it.id == track.id }
    }

    suspend fun markStarted(track: Track) = dao.insertHistory(track.toHistoryEntity(completed = false))
    suspend fun markCompleted(track: Track) = dao.insertHistory(track.toHistoryEntity(completed = true))
    suspend fun save(track: Track) = dao.save(track.toSavedEntity())
    suspend fun unsave(videoId: String) = dao.unsave(videoId)
    suspend fun clearHistory() = dao.clearHistory()

    private fun SubscriptionsResponse.toSeeds() = items.map {
        ArtistSeed(it.snippet.resourceId.channelId, decode(it.snippet.title), it.snippet.thumbnails.bestUrl())
    }

    private fun toTrack(item: VideoItem) = Track(
        id = item.id,
        title = decode(item.snippet.title),
        artist = decode(item.snippet.channelTitle),
        thumbnailUrl = item.snippet.thumbnails.bestUrl(),
        durationText = formatDuration(item.contentDetails?.duration.orEmpty()),
        publishedAt = item.snippet.publishedAt,
        tags = item.snippet.tags.take(12)
    )

    private fun decode(value: String): String = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString()
    private fun cleanTitle(value: String) = value.replace(Regex("[\\[(].*?[\\])]"), "").take(80)
    private fun formatDuration(value: String): String = runCatching {
        val duration = Duration.parse(value)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
    }.getOrDefault("")
}
