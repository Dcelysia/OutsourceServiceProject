package com.dcelysia.outsourceserviceproject.Model.Room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dcelysia.outsourceserviceproject.Model.Room.dao.AudioDetailDao
import com.dcelysia.outsourceserviceproject.Model.Room.entity.AudioDetailEntity

@Database(entities = [AudioDetailEntity::class], version = 3, exportSchema = false)
abstract class AudioDetailDatabase : RoomDatabase() {
    abstract fun viewItemDao(): AudioDetailDao

    companion object {
        private var INSTANCE: AudioDetailDatabase? = null
        fun getInstance(context: Context): AudioDetailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioDetailDatabase::class.java,
                    "audio_detail_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}