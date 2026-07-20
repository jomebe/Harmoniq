package com.jomebe.harmoniq.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HarmoniqDao {
    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun observeHistory(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun recentHistory(limit: Int = 100): List<HistoryEntity>

    @Insert
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM play_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM saved_tracks ORDER BY savedAt DESC")
    fun observeSavedTracks(): Flow<List<SavedTrackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_tracks WHERE videoId = :videoId)")
    fun observeIsSaved(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: SavedTrackEntity)

    @Query("DELETE FROM saved_tracks WHERE videoId = :videoId")
    suspend fun unsave(videoId: String)
}
