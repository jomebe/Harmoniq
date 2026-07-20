package com.jomebe.harmoniq.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    private val one = Track("1", "One", "Artist", "")
    private val two = Track("2", "Two", "Artist", "")

    @Test
    fun `start puts selected track first without duplicates`() {
        val queue = PlaybackQueue().start(two, listOf(one, two))
        assertEquals(listOf("2", "1"), queue.tracks.map(Track::id))
        assertEquals("2", queue.current?.id)
        assertTrue(queue.hasNext)
    }

    @Test
    fun `next stops at end`() {
        val queue = PlaybackQueue().start(one, listOf(one))
        assertFalse(queue.hasNext)
        assertEquals("1", queue.next().current?.id)
    }
}
