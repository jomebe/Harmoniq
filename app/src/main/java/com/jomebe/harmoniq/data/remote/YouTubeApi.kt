package com.jomebe.harmoniq.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("key") key: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") category: String = "10",
        @Query("maxResults") limit: Int = 50,
        @Query("q") query: String,
        @Query("order") order: String = "relevance"
    ): YouTubeSearchResponse

    @GET("youtube/v3/search")
    suspend fun searchChannels(
        @Query("key") key: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "channel",
        @Query("maxResults") limit: Int = 20,
        @Query("q") query: String
    ): YouTubeSearchResponse

    @GET("youtube/v3/videos")
    suspend fun popularMusic(
        @Query("key") key: String,
        @Query("part") part: String = "snippet",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") category: String = "10",
        @Query("regionCode") region: String = "KR",
        @Query("maxResults") limit: Int = 50
    ): YouTubeSearchResponse
}

data class YouTubeSearchResponse(val items: List<YouTubeItem> = emptyList())
data class YouTubeItem(val id: YouTubeId, val snippet: YouTubeSnippet)
data class YouTubeId(val videoId: String? = null, val channelId: String? = null)
data class YouTubeSnippet(val title: String, val channelTitle: String, val thumbnails: YouTubeThumbnails = YouTubeThumbnails())
data class YouTubeThumbnails(val high: YouTubeThumbnail? = null, val medium: YouTubeThumbnail? = null, val default: YouTubeThumbnail? = null)
data class YouTubeThumbnail(val url: String)
