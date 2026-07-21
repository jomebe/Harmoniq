package com.jomebe.harmoniq.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {
    @GET("tracks/")
    suspend fun tracks(
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String? = null,
        @Query("search") search: String? = null,
        @Query("artist_id") artistId: String? = null,
        @Query("include") include: String = "musicinfo"
    ): JamendoTracksResponse

    @GET("artists/")
    suspend fun artists(
        @Query("client_id") clientId: String,
        @Query("namesearch") nameSearch: String,
        @Query("limit") limit: Int = 20
    ): JamendoArtistsResponse
}

data class JamendoTracksResponse(val results: List<JamendoTrack> = emptyList())
data class JamendoArtistsResponse(val results: List<JamendoArtist> = emptyList())
data class JamendoTrack(
    val id: String,
    val name: String,
    val artist_name: String,
    val audio: String = "",
    val image: String = "",
    val duration: Int = 0,
    val musicinfo: JamendoMusicInfo? = null
)
data class JamendoArtist(val id: String, val name: String, val image: String = "")
data class JamendoMusicInfo(val tags: JamendoTags? = null)
data class JamendoTags(val genres: List<String> = emptyList(), val moods: List<String> = emptyList())
