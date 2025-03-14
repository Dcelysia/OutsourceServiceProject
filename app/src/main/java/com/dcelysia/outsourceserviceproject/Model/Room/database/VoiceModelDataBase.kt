package com.dcelysia.outsourceserviceproject.Model.Room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dcelysia.outsourceserviceproject.Model.Room.dao.VoiceModelDao
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity


@Database(entities = [VoiceModelEntity::class], version = 1)
abstract class VoiceModelDataBase : RoomDatabase() {
    abstract fun voiceModelDao(): VoiceModelDao

    companion object {
        @Volatile
        private var INSTANCE: VoiceModelDataBase? = null

        fun getInstance(context: Context): VoiceModelDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceModelDataBase::class.java,
                    "voice_model_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}