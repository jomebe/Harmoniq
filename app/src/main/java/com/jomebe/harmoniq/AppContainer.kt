package com.jomebe.harmoniq

import android.content.Context
import androidx.room.Room
import com.jomebe.harmoniq.data.local.HarmoniqDatabase
import com.jomebe.harmoniq.data.remote.AudiusClient
import com.jomebe.harmoniq.data.repository.MusicRepository
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.player.PlaybackConnection

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        HarmoniqDatabase::class.java,
        "harmoniq.db"
    ).fallbackToDestructiveMigration().build()

    private val api = AudiusClient.create()

    val playback = PlaybackConnection(context)
    val repository = MusicRepository(api, database.dao(), RecommendationEngine())
}
