package com.seekho.animeapp.data.model

import com.squareup.moshi.Json

data class TopAnimeResponse(
    val data: List<AnimeItem>,
    val pagination: Pagination?
)

data class AnimeItem(
    @Json(name = "mal_id") val malId: Int,
    val title: String?,
    val episodes: Int?,
    val score: Double?,
    val images: Images?
)

data class Images(
    val webp: Webp?,
    val jpg: Jpg?
)

data class Webp(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "small_image_url") val smallImageUrl: String?,
    @Json(name = "large_image_url") val largeImageUrl: String?
)

data class Jpg(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "small_image_url") val smallImageUrl: String?,
    @Json(name = "large_image_url") val largeImageUrl: String?
)

data class Pagination(
    @Json(name = "has_next_page") val hasNextPage: Boolean?,
    @Json(name = "current_page") val currentPage: Int?,
    val items: PaginationItems?
)

data class PaginationItems(
    val count: Int?,
    val total: Int?,
    @Json(name = "per_page") val perPage: Int?
)