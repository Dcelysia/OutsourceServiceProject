package com.dcelysia.outsourceserviceproject.Model.data.response

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag

data class VoiceItemResponse(
    val code: String,
    val msg: String,
    val data: VoiceItem?
)

data class VoiceItem(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val avatarResId: Int,
    val wavFile: Int,
    var isPlaying: Boolean = false,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : ItemDrag