package com.dcelysia.outsourceserviceproject.Model.Room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity


@Dao
interface VoiceModelDao {
    @Query("SELECT * FROM voice_models WHERE voiceItemId = :voiceItemId")
    fun getModelByVoiceItemId(voiceItemId: Int): VoiceModelEntity?

    @Query("SELECT * FROM voice_models")
    fun getAllModels(): List<VoiceModelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voiceModel: VoiceModelEntity)

    @Update
    fun update(voiceModel: VoiceModelEntity)

    @Delete
    fun delete(voiceModel: VoiceModelEntity)

    @Query("DELETE FROM voice_models WHERE voiceItemId = :voiceItemId")
    fun deleteById(voiceItemId: Int)

}