package com.dcelysia.outsourceserviceproject.Model.Room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dcelysia.outsourceserviceproject.Model.Room.entity.AudioDetailEntity

@Dao
interface AudioDetailDao {
    @Insert
    fun insertModelItem(modelDetailEntity: AudioDetailEntity): Long
    @Query("SELECT * FROM audio_detail ORDER BY timestamp DESC")
    fun getAllAudio(): List<AudioDetailEntity>
}