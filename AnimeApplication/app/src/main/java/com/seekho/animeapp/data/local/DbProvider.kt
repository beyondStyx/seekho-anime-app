package com.seekho.animeapp.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object DbProvider {
    private const val TAG = "DbProvider"

    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        INSTANCE?.let { return it }

        // build DB once
        return synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "anime.db"
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "onCreate: anime.db created")
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(TAG, "onOpen: anime.db opened")
                    }
                })
                .build()
                .also {
                    Log.d(TAG, "get: created AppDatabase instance")
                    INSTANCE = it
                }
        }
    }
}