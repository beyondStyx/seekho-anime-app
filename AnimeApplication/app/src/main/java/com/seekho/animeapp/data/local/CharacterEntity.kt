package com.seekho.animeapp.data.local

import androidx.room.Entity

@Entity(tableName = "characters", primaryKeys = ["animeId", "characterId"])
data class CharacterEntity(
    val animeId: Int,
    val characterId: Int,
    val name: String?,
    val imageUrl: String?,
    val isMain: Boolean
)