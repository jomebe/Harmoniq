package com.jomebe.harmoniq.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AudiusApi {
    @GET("v1/tracks/trending")
    suspend fun trending(
        @Query("limit") limit: Int = 40,
        @Query("time") time: String = "week"
    ): AudiusTracksResponse

    @GET("v1/tracks/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("limit") limit: Int = 40
    ): AudiusTracksResponse
}

data class AudiusTracksResponse(val data: List<AudiusTrack> = emptyList())

data class AudiusTrack(
    val id: String,
    val title: String,
    val duration: Int = 0,
    val genre: String? = null,
    val mood: String? = null,
    val tags: String? = null,
    val user: AudiusUser,
    val artwork: AudiusArtwork? = null
)

data class AudiusUser(val name: String)

data class AudiusArtwork(
    @com.google.gson.annotations.SerializedName("150x150") val small: String? = null,
    @com.google.gson.annotations.SerializedName("480x480") val medium: String? = null,
    @com.google.gson.annotations.SerializedName("1000x1000") val large: String? = null,
    val mirrors: List<String> = emptyList()
) {
    fun bestUrl(): String = large ?: medium ?: small.orEmpty()
}
