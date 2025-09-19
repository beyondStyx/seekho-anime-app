package com.seekho.animeapp.data.model

import com.squareup.moshi.Json


data class AnimeCharactersResponse(
    val data: List<AnimeCharacter>
)

data class AnimeCharacter(
    val character: CharacterData,
    val role: String?
)

data class CharacterData(
    @Json(name = "mal_id") val malId: Int,
    val name: String?,
    val images: CharacterImages?
)

data class CharacterImages(
    val jpg: CharacterJpg?
)


data class CharacterJpg(
    @Json(name = "image_url") val imageUrl: String?
)