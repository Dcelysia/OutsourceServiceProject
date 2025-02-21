package com.dcelysia.outsourceserviceproject.Model.data.request

import com.google.gson.annotations.SerializedName

data class AccountSecurityRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)