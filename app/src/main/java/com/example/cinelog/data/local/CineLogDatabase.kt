package com.example.cinelog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cinelog.data.local.dao.MovieDao
import com.example.cinelog.data.local.entity.MovieEntity

@Database(entities = [MovieEntity::class], version = 7, exportSchema = false)
abstract class CineLogDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: CineLogDatabase? = null

        fun getDatabase(context: Context): CineLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CineLogDatabase::class.java,
                    "cinelog_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
