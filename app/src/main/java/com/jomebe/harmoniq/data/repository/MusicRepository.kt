package com.jomebe.harmoniq.data.repository

import com.jomebe.harmoniq.data.local.HarmoniqDao
import com.jomebe.harmoniq.data.local.LocalMusicDataSource
import com.jomebe.harmoniq.data.local.toHistoryEntity
import com.jomebe.harmoniq.data.local.toSavedEntity
import com.jomebe.harmoniq.data.remote.JamendoApi
import com.jomebe.harmoniq.data.remote.JamendoTrack
import com.jomebe.harmoniq.domain.Artist
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.domain.Track
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val api: JamendoApi,
    private val localMusic: LocalMusicDataSource,
    private val dao: HarmoniqDao,
    private val recommendationEngine: RecommendationEngine
) {
    private val clientId = "f2215756" // Jamendo public client identifier, not a secret.

    fun observeHistory(): Flow<List<Track>> = dao.observeHistory().map { it.distinctBy { row -> row.trackId }.map { row -> row.toTrack() } }
    fun observeSaved(): Flow<List<Track>> = dao.observeSavedTracks().map { it.map { row -> row.toTrack() } }

    // Jamendo permits up to 200 results per request. Several offsets keep the home feed varied.
    suspend fun popular(): List<Track> = coroutineScope {
        listOf(0, 50, 100, 150).map { offset -> async { api.tracks(clientId, limit = 50, offset = offset, order = "popularity_total").results } }
            .flatMap { it.await() }.map(::toTrack).distinctBy(Track::id).take(160)
    }

    suspend fun search(query: String): List<Track> = coroutineScope {
        val trimmed = query.trim()
        val local = async { localMusic.search(trimmed) }
        val remote = async { api.tracks(clientId, limit = 100, search = trimmed).results.map(::toTrack) }
        (local.await() + remote.await()).distinctBy(Track::id)
    }

    suspend fun artists(query: String): List<Artist> = api.artists(clientId, query.trim()).results
        .map { Artist(it.id, it.name, it.image) }
        .sortedByDescending { it.name.equals(query.trim(), ignoreCase = true) }

    suspend fun tracksForArtist(artist: Artist): List<Track> =
        api.tracks(clientId, limit = 100, artistId = artist.id, order = "popularity_total").results.map(::toTrack)

    suspend fun personalized(): List<Track> = coroutineScope {
        val recent = dao.recentHistory().map { it.toTrack() }
        if (recent.isEmpty()) return@coroutineScope popular().shuffled()
        val candidates = recommendationEngine.seedQueries(recent).take(6)
            .map { query -> async { runCatching { search(query) }.getOrDefault(emptyList()) } }.flatMap { it.await() }
        recommendationEngine.rank(candidates, recent).take(40)
    }

    suspend fun related(track: Track): List<Track> = search(track.tags.firstOrNull() ?: track.artist).filterNot { it.id == track.id }
    suspend fun markStarted(track: Track) = dao.insertHistory(track.toHistoryEntity(false))
    suspend fun markCompleted(track: Track) = dao.insertHistory(track.toHistoryEntity(true))
    suspend fun save(track: Track) = dao.save(track.toSavedEntity())
    suspend fun unsave(trackId: String) = dao.unsave(trackId)
    suspend fun clearHistory() = dao.clearHistory()

    private fun toTrack(item: JamendoTrack): Track = Track(
        id = "jamendo:${item.id}", title = item.name, artist = item.artist_name,
        thumbnailUrl = item.image, streamUrl = item.audio, durationText = formatDuration(item.duration),
        tags = (item.musicinfo?.tags?.genres.orEmpty() + item.musicinfo?.tags?.moods.orEmpty()).distinct()
    )

    private fun formatDuration(seconds: Int): String = if (seconds <= 0) "" else "%d:%02d".format(seconds / 60, seconds % 60)
}
