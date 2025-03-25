package com.dcelysia.outsourceserviceproject.Model.Room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag

@Entity("voice_items")
data class VoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val avatarResId: Int,
    val wavFile: Int
)