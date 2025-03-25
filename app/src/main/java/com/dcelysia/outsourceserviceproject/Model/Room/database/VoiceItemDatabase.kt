package com.dcelysia.outsourceserviceproject.Model.Room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dcelysia.outsourceserviceproject.Model.Room.dao.VoiceItemDao
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceItemEntity

@Database(entities = [VoiceItemEntity::class], version = 1)
abstract class VoiceItemDatabase : RoomDatabase() {
    abstract fun viewItemDao(): VoiceItemDao

    companion object {
        @Volatile
        private var INSTANCE: VoiceItemDatabase? = null

        fun getInstance(context: Context): VoiceItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceItemDatabase::class.java,
                    "voice_item_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}