package com.dcelysia.outsourceserviceproject.Model.data.response

data class LoginAndRegisterResponse (
    val code: String,
    val msg: String,
    val data: UserInfo?
)

data class UserInfo (
    val token: String
)