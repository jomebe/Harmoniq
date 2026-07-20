package com.jomebe.harmoniq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class, SavedTrackEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HarmoniqDatabase : RoomDatabase() {
    abstract fun dao(): HarmoniqDao
}
