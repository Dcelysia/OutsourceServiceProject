package com.dcelysia.outsourceserviceproject.Network.api

import com.dcelysia.outsourceserviceproject.Model.data.request.AccountSecurityRequest
import com.dcelysia.outsourceserviceproject.Model.data.request.UploadAvatarUriRequest
import com.dcelysia.outsourceserviceproject.Model.data.response.FileAvatarResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.MyResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.UpdateUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import java.io.File

interface UserProfileApi {
    @PUT("/user/me/token")
    suspend fun refreshToken() : Response<LoginAndRegisterResponse>

    @GET("/user/me/profile")
    suspend fun getCurrentUserProfile() : Response<UserProfileResponse>

    @Multipart
    @POST("/user/upload-avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part) : Response<FileAvatarResponse>

    @PUT("/user/me/profile")
    suspend fun updateUserProfile(@Body updateUserProfile: UpdateUserProfile) : Response<UserProfileResponse>

    @PUT("/user/change-password")
    suspend fun updatePassword(@Body accountSecurityRequest: AccountSecurityRequest) : Response<MyResponse>

}