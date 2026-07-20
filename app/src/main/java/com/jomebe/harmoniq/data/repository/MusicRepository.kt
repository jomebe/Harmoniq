package com.jomebe.harmoniq.data.repository

import com.jomebe.harmoniq.data.local.HarmoniqDao
import com.jomebe.harmoniq.data.local.toHistoryEntity
import com.jomebe.harmoniq.data.local.toSavedEntity
import com.jomebe.harmoniq.data.remote.AudiusApi
import com.jomebe.harmoniq.data.remote.AudiusTrack
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val api: AudiusApi,
    private val dao: HarmoniqDao,
    private val recommendationEngine: RecommendationEngine
) {
    fun observeHistory(): Flow<List<Track>> =
        dao.observeHistory().map { rows -> rows.distinctBy { it.trackId }.map { it.toTrack() } }

    fun observeSaved(): Flow<List<Track>> =
        dao.observeSavedTracks().map { rows -> rows.map { it.toTrack() } }

    fun observeIsSaved(trackId: String): Flow<Boolean> = dao.observeIsSaved(trackId)

    suspend fun popular(): List<Track> = api.trending().data.map(::toTrack)

    suspend fun search(query: String): List<Track> =
        api.search(query.trim()).data.map(::toTrack)

    suspend fun personalized(): List<Track> = coroutineScope {
        val recent = dao.recentHistory().map { it.toTrack() }
        if (recent.isEmpty()) return@coroutineScope popular().shuffled()

        val queries = recommendationEngine.seedQueries(recent)
        val candidates = queries.take(6)
            .map { query -> async { runCatching { search(query) }.getOrDefault(emptyList()) } }
            .flatMap { it.await() }
        recommendationEngine.rank(candidates, recent).take(40)
    }

    suspend fun related(track: Track): List<Track> {
        val query = track.tags.firstOrNull() ?: track.artist
        return search(query).filterNot { it.id == track.id }
    }

    suspend fun markStarted(track: Track) = dao.insertHistory(track.toHistoryEntity(completed = false))
    suspend fun markCompleted(track: Track) = dao.insertHistory(track.toHistoryEntity(completed = true))
    suspend fun save(track: Track) = dao.save(track.toSavedEntity())
    suspend fun unsave(trackId: String) = dao.unsave(trackId)
    suspend fun clearHistory() = dao.clearHistory()

    private fun toTrack(item: AudiusTrack): Track {
        val tags = buildList {
            item.genre?.takeIf(String::isNotBlank)?.let(::add)
            item.mood?.takeIf(String::isNotBlank)?.let(::add)
            item.tags.orEmpty().split(',').map(String::trim).filter(String::isNotBlank).take(10).forEach(::add)
        }.distinct()
        return Track(
            id = item.id,
            title = item.title,
            artist = item.user.name,
            thumbnailUrl = item.artwork?.bestUrl().orEmpty(),
            streamUrl = "https://api.audius.co/v1/tracks/${item.id}/stream",
            durationText = formatDuration(item.duration),
            tags = tags
        )
    }

    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return ""
        val hours = seconds / 3600
        val minutes = seconds / 60 % 60
        val remaining = seconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, remaining)
        else "%d:%02d".format(minutes, remaining)
    }
}
