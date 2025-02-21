package com.dcelysia.outsourceserviceproject.Network.api

import com.dcelysia.outsourceserviceproject.Model.data.request.LoginAndRegisterRequest
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAndRegisterApi {

    @POST("/user/login")
    suspend fun login(@Body request: LoginAndRegisterRequest) : Response<LoginAndRegisterResponse>

    @POST("/user/register")
    suspend fun register(@Body request: LoginAndRegisterRequest) : Response<LoginAndRegisterResponse>


}