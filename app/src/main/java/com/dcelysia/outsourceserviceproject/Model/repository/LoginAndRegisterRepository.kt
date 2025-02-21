package com.dcelysia.outsourceserviceproject.Model.repository

import com.dcelysia.outsourceserviceproject.Model.data.request.LoginAndRegisterRequest
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Network.RetrofitClient
import com.dcelysia.outsourceserviceproject.core.MainApplication

class LoginAndRegisterRepository {

    suspend fun login(account: String, password: String): Resource<LoginAndRegisterResponse> {
        return try {
            val response =
                RetrofitClient.loginAndRegisterApi.login(LoginAndRegisterRequest(account, password))
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> {
                            MainApplication.token = it.data?.token
                            Resource.Success(it)
                        }
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("Response body is null")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("Login failed: ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun register(account: String, password: String): Resource<LoginAndRegisterResponse> {
        return try {
            val response = RetrofitClient.loginAndRegisterApi.register(
                LoginAndRegisterRequest(
                    account,
                    password
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    when (it.code) {
                        "200" -> Resource.Success(it)
                        else -> Resource.Error(it.msg)
                    }
                } ?: Resource.Error("Response body is null")
            } else {
                val errorResponse = RetrofitClient.getErrorResponse(response.errorBody())
                Resource.Error("Login failed: ${errorResponse?.msg}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}