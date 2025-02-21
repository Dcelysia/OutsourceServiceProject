package com.dcelysia.outsourceserviceproject.Network

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dcelysia.outsourceserviceproject.Activity.LoginActivity
import com.dcelysia.outsourceserviceproject.Model.data.response.ErrorResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Network.api.LoginAndRegisterApi
import com.dcelysia.outsourceserviceproject.Network.api.UserProfileApi
import com.dcelysia.outsourceserviceproject.Network.interceptor.AuthInterceptor
import com.dcelysia.outsourceserviceproject.Network.interceptor.NetworkLogger
import com.dcelysia.outsourceserviceproject.core.MainApplication
import com.google.gson.Gson
import com.tencent.msdk.dns.MSDKDnsResolver
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = NetworkLogger.getLoggingInterceptor()

    private val client = OkHttpClient.Builder()
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                require(hostname.isNotBlank()) { "hostname can not be null or blank" }
                return try {
                    // 尝试使用 HTTPDNS 解析
                    val ips = MSDKDnsResolver.getInstance().getAddrByName(hostname)
                    val ipArr = ips.split(";")
                    // 如果没有返回有效的 IP 地址，尝试降级使用 LocalDNS
                    if (ipArr.isEmpty() || ipArr.all { it == "0" }) {
                        fallbackToLocalDns(hostname)
                    } else {
                        val inetAddressList = mutableListOf<InetAddress>()
                        for (ip in ipArr) {
                            if (ip != "0") {
                                try {
                                    Log.d("MyIp", ip)
                                    inetAddressList.add(InetAddress.getByName(ip))
                                } catch (ignored: UnknownHostException) {
                                    // 忽略无效的 IP
                                }
                            }
                        }
                        // 如果 HTTPDNS 返回的 IP 列表为空，则降级使用 LocalDNS
                        if (inetAddressList.isEmpty()) {
                            fallbackToLocalDns(hostname)
                        } else {
                            inetAddressList
                        }
                    }
                } catch (e: Exception) {
                    // 在发生异常时降级使用 LocalDNS
                    fallbackToLocalDns(hostname)
                }
            }

            // 降级到 LocalDNS 的方法
            private fun fallbackToLocalDns(hostname: String): List<InetAddress> {
                return try {
                    InetAddress.getAllByName(hostname).toList()
                } catch (e: UnknownHostException) {
                    emptyList()
                }
            }
        })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(object : AuthInterceptor.TokenExpiredHandler {
            override fun onTokenExpired() {
                Handler(Looper.getMainLooper()).post {
                    val intent = Intent(MainApplication.appContext, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("from_token_expired", true)
                    MainApplication.appContext.startActivity(intent)
                }
            }
        }))
        .build()

    fun refreshTokenSync(): String? {
        try {
            val request = Request.Builder()
                .url("${MainApplication.ip}/user/me/token")
                .addHeader("token", MainApplication.token ?: "")
                .put("".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()

            return client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val newUserProfile = Gson().fromJson(
                            response.body!!.toString(),
                            LoginAndRegisterResponse::class.java
                        )
                        newUserProfile.data?.also {
                            MainApplication.token = it.token
                            Log.d("Token Refresh", "Token refreshed successfully")
                        }
                    }

                    else -> {
                        Log.w("Token Refresh", "Failed to refresh token: ${response.code}")
                        val errorBody = response.body?.string()
                        Log.w("Token Refresh", "Error response: $errorBody")
                    }
                }
                null
            }
        } catch (e: Exception) {
            Log.e("Token Refresh", "Network error during token refresh", e)
            return null
        }
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(MainApplication.ip)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getErrorResponse(errorBody: ResponseBody?): ErrorResponse? {
        return try {
            errorBody?.let {
                val converter = retrofit.responseBodyConverter<ErrorResponse>(
                    ErrorResponse::class.java,
                    ErrorResponse::class.java.annotations
                )
                converter.convert(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    val loginAndRegisterApi: LoginAndRegisterApi = retrofit.create(LoginAndRegisterApi::class.java)
    val userProfileApi: UserProfileApi = retrofit.create(UserProfileApi::class.java)

}