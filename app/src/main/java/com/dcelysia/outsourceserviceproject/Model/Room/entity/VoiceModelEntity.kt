package com.dcelysia.outsourceserviceproject.Model.Room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName = "voice_models")
data class VoiceModelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val voiceItemId: Int, // 关联的VoiceItem的ID
    val pthModelFile: String, // GPT_weights_v3/${用户名_用户模型名字}-e15.ckpt
    val ckptModelFile: String, // SoVITS_weights_v3/${用户名_用户模型名字}_e8_s40_l32.pth
    val referenceWavPath: String,  // 参考音频文件路径
    val referenceWavText: String   // 参考音频文本
)