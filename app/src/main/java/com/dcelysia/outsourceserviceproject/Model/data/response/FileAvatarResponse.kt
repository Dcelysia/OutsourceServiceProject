package com.dcelysia.outsourceserviceproject.Model.data.response

import com.google.gson.annotations.SerializedName

data class FileAvatarResponse(
    val code: String,
    val msg: String,
    val data: FileAvatarUrl
)

data class FileAvatarUrl(
    @SerializedName("avatarUrl")
    val avatarUrl: String
)