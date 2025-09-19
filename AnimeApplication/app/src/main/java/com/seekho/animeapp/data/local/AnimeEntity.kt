package com.seekho.animeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey val malId: Int,
    val title: String?,
    val episodes: Int?,
    val score: Double?,
    val posterUrl: String?,
    val pageIndex: Int,
    val updatedAt: Long
)