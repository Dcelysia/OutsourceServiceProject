package com.dcelysia.outsourceserviceproject.Model.repository

import android.net.Uri
import android.util.Log
import com.dcelysia.outsourceserviceproject.Model.data.request.AccountSecurityRequest
import com.dcelysia.outsourceserviceproject.Model.data.request.UploadAvatarUriRequest
import com.dcelysia.outsourceserviceproject.Model.data.response.ErrorResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.FileAvatarResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.MyResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.UpdateUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfileResponse
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserProfileRepository {

    fun refreshToken(): Resource<LoginAndRegisterResponse> {
        return try {
            runBlocking {
                val response = RetrofitClient.userProfileApi.refreshToken()
                if (response.isSuccessful) {
                    response.body()?.let {
                        when (it.code) {
                            "200" -> Resource.Success(it)
                            else -> Resource.Error(it.msg)
                        }
                    } ?: Resource.Error("请求异常，无返回值")
                } else {
                    val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                    Resource.Error("请求异常, ${errorResponse?.msg}")
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getCurrentUserProfile(): Resource<UserProfileResponse> {
        return try {
            val response = RetrofitClient.userProfileApi.getCurrentUserProfile()
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> Resource.Success(it)
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("请求异常，无返回值")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("请求异常, ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun uploadAvatar(file: File): Resource<FileAvatarResponse> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )
            val response = RetrofitClient.userProfileApi.uploadAvatar(filePart)
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> Resource.Success(it)
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("请求异常，无返回值")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("请求异常, ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateUserProfile(userProfile: UpdateUserProfile): Resource<UserProfileResponse> {
        return try {
            val response = RetrofitClient.userProfileApi.updateUserProfile(userProfile)
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> Resource.Success(it)
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("请求异常，无返回值")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("请求异常, ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updatePassword(accountSecurityRequest: AccountSecurityRequest): Resource<MyResponse> {
        return try {
            val response = RetrofitClient.userProfileApi.updatePassword(accountSecurityRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> Resource.Success(it)
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("请求异常，无返回值")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("请求异常, ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}