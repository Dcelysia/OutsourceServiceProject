package com.dcelysia.outsourceserviceproject.Model.Room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceItemEntity
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity

@Dao
interface VoiceItemDao {
    @Query("SELECT * FROM voice_items WHERE id = :id")
    fun getModelByVoiceItemId(id: Int): VoiceItemEntity?

    @Query("SELECT * FROM voice_items")
    fun getAllModels(): List<VoiceItemEntity>

    @Insert
    suspend fun insert(voiceItem: VoiceItemEntity): Long

    @Update
    fun update(voiceItem: VoiceItemEntity)

    @Delete
    fun delete(voiceItem: VoiceItemEntity)

    @Query("DELETE FROM voice_items WHERE id = :id")
    fun deleteById(id: Int)
}