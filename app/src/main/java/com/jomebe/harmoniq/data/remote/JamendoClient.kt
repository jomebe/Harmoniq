package com.jomebe.harmoniq.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object JamendoClient {
    fun create(): JamendoApi = Retrofit.Builder()
        .baseUrl("https://api.jamendo.com/v3.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(JamendoApi::class.java)
}
