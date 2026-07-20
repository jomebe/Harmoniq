package com.jomebe.harmoniq.domain

import kotlin.math.ln

class RecommendationEngine {
    fun rank(
        candidates: List<Track>,
        recent: List<Track>,
        liked: List<Track> = emptyList(),
        subscribedArtists: Set<String> = emptySet()
    ): List<Track> {
        if (candidates.isEmpty()) return emptyList()

        val artistAffinity = mutableMapOf<String, Double>()
        recent.forEachIndexed { index, track ->
            val recency = 1.0 / (1.0 + index * 0.12)
            artistAffinity.merge(normalize(track.artist), recency, Double::plus)
        }
        liked.forEach { artistAffinity.merge(normalize(it.artist), 2.4, Double::plus) }
        subscribedArtists.forEach { artistAffinity.merge(normalize(it), 1.7, Double::plus) }

        val tagAffinity = (recent.take(40) + liked).flatMap(Track::tags)
            .groupingBy { normalize(it) }
            .eachCount()

        val playedIds = recent.map(Track::id).toSet()
        return candidates.distinctBy(Track::id).sortedByDescending { track ->
            val artistScore = artistAffinity[normalize(track.artist)] ?: 0.0
            val tagScore = track.tags.sumOf { ln(1.0 + (tagAffinity[normalize(it)] ?: 0)) }
            val novelty = if (track.id in playedIds) -1.5 else 0.8
            artistScore * 2.2 + tagScore * 0.55 + novelty
        }
    }

    fun seedQueries(recent: List<Track>, liked: List<Track>, subscriptions: List<ArtistSeed>): List<String> {
        val artistSeeds = (liked.map(Track::artist) + recent.map(Track::artist) + subscriptions.map(ArtistSeed::name))
            .map { normalizeDisplay(it) }
            .filter(String::isNotBlank)
            .distinct()
            .take(6)
        return (artistSeeds.map { "$it music" } + listOf("최신 음악", "인기 음악")).distinct().take(8)
    }

    private fun normalize(value: String) = value.lowercase().replace(Regex("[^a-z0-9가-힣]"), "")
    private fun normalizeDisplay(value: String) = value.replace(Regex("(?i)official|topic|vevo|records|music"), "").trim()
}
