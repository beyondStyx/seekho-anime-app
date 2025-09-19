package com.seekho.animeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_detail")
data class AnimeDetailEntity(
    @PrimaryKey val malId: Int,
    val title: String?,
    val synopsis: String?,
    val episodes: Int?,
    val score: Double?,
    val posterUrl: String?,
    val trailerEmbedUrl: String?,
    val trailerUrl: String?,
    val youtubeId: String?,
    val genresCsv: String?,
    val updatedAt: Long
)