package com.jomebe.harmoniq

import android.content.Context
import androidx.room.Room
import com.jomebe.harmoniq.auth.YouTubeAuthManager
import com.jomebe.harmoniq.data.local.HarmoniqDatabase
import com.jomebe.harmoniq.data.remote.AuthTokenStore
import com.jomebe.harmoniq.data.remote.YouTubeClient
import com.jomebe.harmoniq.data.repository.MusicRepository
import com.jomebe.harmoniq.domain.RecommendationEngine

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        HarmoniqDatabase::class.java,
        "harmoniq.db"
    ).fallbackToDestructiveMigration().build()

    private val tokenStore = AuthTokenStore()
    private val api = YouTubeClient.create(tokenStore)

    val authManager = YouTubeAuthManager(context, tokenStore)
    val repository = MusicRepository(api, database.dao(), RecommendationEngine())
}
