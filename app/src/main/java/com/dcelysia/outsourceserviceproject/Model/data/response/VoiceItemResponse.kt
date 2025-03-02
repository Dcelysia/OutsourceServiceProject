package com.dcelysia.outsourceserviceproject.Model.data.response

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
    var isPlaying: Boolean = false
)