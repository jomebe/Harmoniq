package com.jomebe.harmoniq.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object YouTubeClient {
    fun create(): YouTubeApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YouTubeApi::class.java)
}
