package com.jomebe.harmoniq.data.remote

data class SearchResponse(
    val items: List<SearchItem> = emptyList(),
    val nextPageToken: String? = null
)

data class SearchItem(
    val id: SearchId,
    val snippet: Snippet
)

data class SearchId(val videoId: String? = null)

data class VideosResponse(val items: List<VideoItem> = emptyList())

data class VideoItem(
    val id: String,
    val snippet: Snippet,
    val contentDetails: ContentDetails? = null
)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val channelId: String = "",
    val publishedAt: String = "",
    val thumbnails: Thumbnails,
    val tags: List<String> = emptyList()
)

data class Thumbnails(
    val maxres: Thumbnail? = null,
    val standard: Thumbnail? = null,
    val high: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val default: Thumbnail? = null
) {
    fun bestUrl(): String = maxres?.url ?: standard?.url ?: high?.url ?: medium?.url ?: default?.url.orEmpty()
}

data class Thumbnail(val url: String)
data class ContentDetails(val duration: String = "")

data class SubscriptionsResponse(val items: List<SubscriptionItem> = emptyList())
data class SubscriptionItem(val snippet: SubscriptionSnippet)
data class SubscriptionSnippet(
    val title: String,
    val resourceId: ResourceId,
    val thumbnails: Thumbnails
)
data class ResourceId(val channelId: String)

data class ChannelsResponse(val items: List<ChannelItem> = emptyList())
data class ChannelItem(val id: String, val snippet: Snippet)
