package com.dcelysia.outsourceserviceproject.Model.data.response

data class UserProfileResponse(
    val code: String,
    val msg: String,
    val data: UserProfile?
)

data class UserProfile(
    val userId: Int,
    val account: String,
    val avatarUrl: String,
    val bio: String,
    val userLevel: Int,
    val gender: Int,
    val grade: String,
    val birthDate: String?, // 可为空
    val location: String?,
    val website: String?, // 可为空
    val createTime: String,
    val updateTime: String,
    val isDeleted: Int,
    val description: String,
    val existingModelsCount: Int,
    val customModelsCount: Int
)

data class BaseUserProfile(
    val account: String,
    val avatarUrl: String,
)

data class UpdateUserProfile(
    val account: String,
    var avatarUrl: String,
    val bio: String,
    val gender: Int,
    val grade: Int,
    val birthdate: String?, // 可为空
    val location: String?,
    val website: String?,
    val description: String,
)