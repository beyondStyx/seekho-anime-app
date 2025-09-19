package com.seekho.animeapp.data.local

import com.seekho.animeapp.data.model.*

private const val TAG = "Mappers"

private fun AnimeItem.posterUrl(): String? =
    images?.webp?.largeImageUrl
        ?: images?.jpg?.largeImageUrl
        ?: images?.webp?.imageUrl
        ?: images?.jpg?.imageUrl

private fun AnimeDetail.posterUrl(): String? =
    images?.webp?.largeImageUrl
        ?: images?.jpg?.largeImageUrl
        ?: images?.webp?.imageUrl
        ?: images?.jpg?.imageUrl

// Network -> DB
fun AnimeItem.toEntity(page: Int, now: Long) = AnimeEntity(
    malId = malId,
    title = title,
    episodes = episodes,
    score = score,
    posterUrl = posterUrl(),
    pageIndex = page,
    updatedAt = now
)

fun AnimeDetail.toEntity(now: Long) = AnimeDetailEntity(
    malId = malId,
    title = title,
    synopsis = synopsis,
    episodes = episodes,
    score = score,
    posterUrl = posterUrl(),
    trailerEmbedUrl = trailer?.embedUrl,
    trailerUrl = trailer?.url,
    youtubeId = trailer?.youtubeId,
    genresCsv = genres?.mapNotNull { it.name }?.joinToString(","),
    updatedAt = now
)

fun AnimeCharacter.toEntity(animeId: Int) = CharacterEntity(
    animeId = animeId,
    characterId = character.malId,                // <-- needs CharacterData.malId
    name = character.name,
    imageUrl = character.images?.jpg?.imageUrl,
    isMain = (role?.equals("Main", true) == true)
)

// DB -> UI-shaped models (reuse your existing UI models)
fun AnimeEntity.toItem(): AnimeItem = AnimeItem(
    malId = malId,
    title = title,
    episodes = episodes,
    score = score,
    images = Images(
        webp = Webp(imageUrl = posterUrl, smallImageUrl = posterUrl, largeImageUrl = posterUrl),
        jpg  = Jpg (imageUrl = posterUrl, smallImageUrl = posterUrl, largeImageUrl = posterUrl)
    )
)

fun AnimeDetailEntity.toDetail(): AnimeDetail = AnimeDetail(
    malId = malId,
    title = title,
    synopsis = synopsis,
    episodes = episodes,
    score = score,
    images = Images(
        webp = Webp(imageUrl = posterUrl, smallImageUrl = posterUrl, largeImageUrl = posterUrl),
        jpg  = Jpg (imageUrl = posterUrl, smallImageUrl = posterUrl, largeImageUrl = posterUrl)
    ),
    trailer = Trailer(
        youtubeId = youtubeId,
        url = trailerUrl,
        embedUrl = trailerEmbedUrl,
        images = null
    ),
    genres = genresCsv?.split(",")?.map { Genre(name = it) }
)