package com.jomebe.harmoniq.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private val engine = RecommendationEngine()

    @Test
    fun `liked artist is ranked above unknown artist`() {
        val liked = listOf(track("liked", "Artist A", listOf("indie")))
        val candidates = listOf(
            track("unknown", "Artist B", listOf("pop")),
            track("match", "Artist A", listOf("indie"))
        )

        val ranked = engine.rank(candidates, recent = emptyList(), liked = liked)

        assertEquals("match", ranked.first().id)
    }

    @Test
    fun `previously unplayed track receives novelty boost`() {
        val played = track("played", "Same Artist")
        val fresh = track("fresh", "Same Artist")

        val ranked = engine.rank(listOf(played, fresh), recent = listOf(played))

        assertEquals("fresh", ranked.first().id)
    }

    @Test
    fun `seed queries include recent artists and defaults`() {
        val queries = engine.seedQueries(listOf(track("1", "Artist Topic")), emptyList(), emptyList())
        assertTrue(queries.any { it.contains("Artist") })
        assertTrue(queries.any { it == "최신 음악" })
    }

    private fun track(id: String, artist: String, tags: List<String> = emptyList()) =
        Track(id, "Title $id", artist, "", tags = tags)
}
