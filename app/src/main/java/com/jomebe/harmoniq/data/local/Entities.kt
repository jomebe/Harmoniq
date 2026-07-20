package com.jomebe.harmoniq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jomebe.harmoniq.domain.Track

@Entity(tableName = "play_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val tags: String,
    val playedAt: Long,
    val completed: Boolean
) {
    fun toTrack() = Track(videoId, title, artist, thumbnailUrl, tags = tags.split('|').filter(String::isNotBlank))
}

@Entity(tableName = "saved_tracks")
data class SavedTrackEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val durationText: String,
    val tags: String,
    val savedAt: Long
) {
    fun toTrack() = Track(videoId, title, artist, thumbnailUrl, durationText, tags = tags.split('|').filter(String::isNotBlank))
}

fun Track.toSavedEntity() = SavedTrackEntity(
    videoId = id,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    durationText = durationText,
    tags = tags.joinToString("|"),
    savedAt = System.currentTimeMillis()
)

fun Track.toHistoryEntity(completed: Boolean) = HistoryEntity(
    videoId = id,
    title = title,
    artist = artist,
    thumbnailUrl = thumbnailUrl,
    tags = tags.joinToString("|"),
    playedAt = System.currentTimeMillis(),
    completed = completed
)
