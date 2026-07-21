package com.jomebe.harmoniq

import android.content.Context
import androidx.room.Room
import com.jomebe.harmoniq.data.local.HarmoniqDatabase
import com.jomebe.harmoniq.data.local.LocalMusicDataSource
import com.jomebe.harmoniq.data.remote.JamendoClient
import com.jomebe.harmoniq.data.repository.MusicRepository
import com.jomebe.harmoniq.domain.RecommendationEngine
import com.jomebe.harmoniq.player.PlaybackConnection

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        HarmoniqDatabase::class.java,
        "harmoniq.db"
    ).fallbackToDestructiveMigration().build()

    private val api = JamendoClient.create()

    val playback = PlaybackConnection(context)
    val repository = MusicRepository(api, LocalMusicDataSource(context), database.dao(), RecommendationEngine())
}
