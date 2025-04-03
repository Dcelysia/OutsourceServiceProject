package com.dcelysia.outsourceserviceproject.Model.Room.entity

import android.service.quicksettings.Tile
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat

@Entity("audio_detail")
data class AudioDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val audioPicture: String,
    val content: String,
    val audioUrl: String,
    val modelName: String = "",
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()).toString(),
)
