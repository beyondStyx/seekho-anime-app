package com.seekho.animeapp.data.model

import com.squareup.moshi.Json

data class AnimeDetailsResponse(
    val data: AnimeDetail
)

data class AnimeDetail(
    @Json(name = "mal_id") val malId: Int,
    val title: String?,
    val synopsis: String?,
    val episodes: Int?,
    val score: Double?,
    val images: Images?,
    val genres: List<Genre>?,
    val trailer: Trailer?
)

data class Genre(
    val name: String?
)

data class Trailer(
    @Json(name = "youtube_id") val youtubeId: String?,   // optional but handy
    val url: String?,
    @Json(name = "embed_url") val embedUrl: String?,
    val images: TrailerImages?
)

data class TrailerImages(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "small_image_url") val smallImageUrl: String?,
    @Json(name = "medium_image_url") val mediumImageUrl: String?,
    @Json(name = "large_image_url") val largeImageUrl: String?,
    @Json(name = "maximum_image_url") val maximumImageUrl: String?
)