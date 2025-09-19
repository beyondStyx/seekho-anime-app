package com.seekho.animeapp.data.local

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AnimeDao {

    companion object {
        private const val TAG = "AnimeDao"
    }

    @Query("""
    SELECT * FROM anime
    WHERE pageIndex = :page
    ORDER BY 
        (score IS NULL) ASC,   
        score DESC,            
        title ASC
""")
    suspend fun getAnimeByPage(page: Int): List<AnimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnime(list: List<AnimeEntity>)

    //Detail
    @Query("SELECT * FROM anime_detail WHERE malId = :id")
    suspend fun getDetail(id: Int): AnimeDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDetail(detail: AnimeDetailEntity)

    //Characters
    @Query("SELECT * FROM characters WHERE animeId = :animeId AND isMain = 1")
    suspend fun getMainCast(animeId: Int): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(list: List<CharacterEntity>)

    @Query("DELETE FROM characters WHERE animeId = :animeId")
    suspend fun deleteCharactersForAnime(animeId: Int)

    @Transaction
    suspend fun replaceCharactersForAnime(animeId: Int, list: List<CharacterEntity>) {
        Log.d(TAG, "replaceCharactersForAnime: id=$animeId, new=${list.size}")
        deleteCharactersForAnime(animeId)
        insertCharacters(list)
        Log.d(TAG, "replaceCharactersForAnime: done id=$animeId")
    }
}