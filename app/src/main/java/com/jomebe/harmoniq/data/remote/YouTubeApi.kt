package com.jomebe.harmoniq.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {
    @GET("youtube/v3/search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("videoEmbeddable") videoEmbeddable: String = "true",
        @Query("safeSearch") safeSearch: String = "moderate",
        @Query("regionCode") regionCode: String = "KR",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): SearchResponse

    @GET("youtube/v3/videos")
    suspend fun videosByIds(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") ids: String,
        @Query("key") apiKey: String
    ): VideosResponse

    @GET("youtube/v3/videos")
    suspend fun popularMusic(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("regionCode") regionCode: String = "KR",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): VideosResponse

    @GET("youtube/v3/videos")
    suspend fun likedVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("myRating") myRating: String = "like",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): VideosResponse

    @GET("youtube/v3/subscriptions")
    suspend fun subscriptions(
        @Query("part") part: String = "snippet",
        @Query("mine") mine: Boolean = true,
        @Query("order") order: String = "relevance",
        @Query("maxResults") maxResults: Int = 25,
        @Query("key") apiKey: String
    ): SubscriptionsResponse
}
