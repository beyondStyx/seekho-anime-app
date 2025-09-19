package com.seekho.animeapp.data.remote

import com.seekho.animeapp.data.model.AnimeCharactersResponse
import com.seekho.animeapp.data.model.AnimeDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.seekho.animeapp.data.model.TopAnimeResponse
import retrofit2.http.Path

interface JikanService {

    @GET("v4/top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 24,
        @Query("sfw") sfw: Boolean = true
    ): TopAnimeResponse

    @GET("v4/anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: Int
    ) : AnimeDetailsResponse

    @GET("v4/anime/{id}/characters")
    suspend fun getAnimeCharacters(
        @Path("id") id: Int
    ) : AnimeCharactersResponse
}