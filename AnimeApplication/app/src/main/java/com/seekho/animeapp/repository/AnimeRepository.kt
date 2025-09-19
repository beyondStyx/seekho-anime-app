package com.seekho.animeapp.repository

import android.content.Context
import android.util.Log
import com.seekho.animeapp.data.local.*
import com.seekho.animeapp.data.model.*
import com.seekho.animeapp.data.remote.JikanService
import com.seekho.animeapp.data.remote.NetworkModule
import java.io.IOException
import kotlinx.coroutines.delay

class AnimeRepository(
    context: Context,
    private val api: JikanService = NetworkModule.api,
    private val db: AppDatabase = DbProvider.get(context.applicationContext)
) {
    private val dao = db.animeDao()

    companion object {
        private const val TAG = "AnimeRepository"
    }


    // Top list with caching
    suspend fun getTopAnime(page: Int, limit: Int, sfw: Boolean): Result<TopAnimeResponse> {
        val now = System.currentTimeMillis()
        Log.d(TAG, "getTopAnime: page=$page limit=$limit sfw=$sfw")
        return try {
            val net = api.getTopAnime(page = page, limit = limit, sfw = sfw)
            val entities = net.data.map { it.toEntity(page, now) }
            Log.d(TAG, "getTopAnime: net ok items=${net.data.size}")
            dao.upsertAnime(entities) // write-through
            Log.d(TAG, "getTopAnime: cached page=$page rows=${entities.size}")

            Result.success(net)
        } catch (e: IOException) {
            // offline try cache
            Log.e(TAG, "getTopAnime: network/io failed, try cache. ${e.message}")
            val cached = dao.getAnimeByPage(page)
            if (cached.isNotEmpty()) {
                Result.success(
                    TopAnimeResponse(
                        data = cached.map { it.toItem() },
                        pagination = Pagination(
                            hasNextPage = true, // approximate for cache
                            currentPage = page,
                            items = PaginationItems(
                                count = cached.size,
                                total = null,
                                perPage = limit
                            )
                        )
                    )
                )
            } else {
                Log.e(TAG, "getTopAnime: cache miss page=$page")
                Result.failure(IllegalStateException("You're offline and no cached data for this page."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopAnime: error ${e.message}", e)
            Result.failure(e)
        }
    }

    // Details with caching
    suspend fun getAnimeDetails(id: Int): Result<AnimeDetailsResponse> {
        val now = System.currentTimeMillis()
        Log.d(TAG, "getAnimeDetails: id=$id")
        return try {
            val net = api.getAnimeDetails(id)
            dao.upsertDetail(net.data.toEntity(now))
            Log.d(TAG, "getAnimeDetails: cached id=$id")
            Result.success(net)
        } catch (e: IOException) {
            Log.e(TAG, "getAnimeDetails: network/io failed, try cache. ${e.message}")
            val cached = dao.getDetail(id)
            if (cached != null) {
                Result.success(AnimeDetailsResponse(data = cached.toDetail()))
            } else {
                Result.failure(IllegalStateException("You're offline and this anime isn't cached yet."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAnimeDetails: error ${e.message}", e)
            Result.failure(e)
        }
    }

    // Characters (main cast) with caching
    suspend fun getAnimeCharacters(id: Int): Result<List<AnimeCharacter>> {
        Log.d(TAG, "getAnimeCharacters: id=$id")
        return try {
            delay(120) // be gentle with the API
            val net = api.getAnimeCharacters(id)
            val main = net.data.filter { it.role.equals("Main", true) }
            dao.replaceCharactersForAnime(id, main.map { it.toEntity(id) })
            Result.success(main)
        } catch (e: IOException) {
            val cached = dao.getMainCast(id)
            if (cached.isNotEmpty()) {
                Result.success(
                    cached.map {
                        AnimeCharacter(
                            role = if (it.isMain) "Main" else "Supporting",
                            character = CharacterData(
                                malId = it.characterId,
                                name = it.name,
                                images = CharacterImages(jpg = CharacterJpg(imageUrl = it.imageUrl))
                            )
                        )
                    }
                )
            } else {
                Result.failure(IllegalStateException("You're offline and cast isn't cached."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAnimeCharacters: error ${e.message}", e)
            Result.failure(e)
        }
    }
}