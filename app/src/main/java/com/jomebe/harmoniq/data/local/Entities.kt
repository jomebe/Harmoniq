package com.jomebe.harmoniq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jomebe.harmoniq.domain.Track

@Entity(tableName = "play_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val trackId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val tags: String,
    val playedAt: Long,
    val completed: Boolean
) {
    fun toTrack() = Track(trackId, title, artist, thumbnailUrl, streamUrl, tags = tags.split('|').filter(String::isNotBlank))
}

@Entity(tableName = "saved_tracks")
data class SavedTrackEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val durationText: String,
    val tags: String,
    val savedAt: Long
) {
    fun toTrack() = Track(trackId, title, artist, thumbnailUrl, streamUrl, durationText, tags = tags.split('|').filter(String::isNotBlank))
}

fun Track.toSavedEntity() = SavedTrackEntity(
    trackId = id,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    streamUrl = streamUrl,
    durationText = durationText,
    tags = tags.joinToString("|"),
    savedAt = System.currentTimeMillis()
)

fun Track.toHistoryEntity(completed: Boolean) = HistoryEntity(
    trackId = id,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    streamUrl = streamUrl,
    tags = tags.joinToString("|"),
    playedAt = System.currentTimeMillis(),
    completed = completed
)
