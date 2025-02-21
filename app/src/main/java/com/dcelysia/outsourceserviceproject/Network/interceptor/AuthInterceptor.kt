package com.dcelysia.outsourceserviceproject.Network.interceptor

import android.util.Log
import com.dcelysia.outsourceserviceproject.Model.data.response.ErrorResponse
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Utils.CommonConst
import com.dcelysia.outsourceserviceproject.core.MainApplication
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenExpiredHandler: TokenExpiredHandler? = null) :
    Interceptor {
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 2
        private const val MAX_BACKOFF_DELAY = 200L // 最大延迟0.2秒
    }

    interface TokenExpiredHandler {
        fun onTokenExpired()
    }

    private val userProfileRepository by lazy { UserProfileRepository() }
    private var retryCount = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 添加 Authorization 头
        val requestWithToken = originalRequest.newBuilder()
            .addHeader("token", MainApplication.token ?: "")
            .build()

        if (originalRequest.url.encodedPath == "/user/me/token" && originalRequest.method == "PUT") {
            return chain.proceed(requestWithToken)
        }

        val response = chain.proceed(requestWithToken)

        val responseBody = response.peekBody(Long.MAX_VALUE).string()
        val realResponse = Gson().fromJson(responseBody, ErrorResponse::class.java)
        // 检查是否需要重试
        if (realResponse?.code == "401" &&
            realResponse.msg == CommonConst.UNAUTHORIZATION &&
            retryCount < MAX_RETRY_ATTEMPTS
        ) {
            retryCount++
            synchronized(this) {
                try {
                    // 计算当前重试的延迟时间（指数退避）
                    val delayMs = (1000L * (1 shl (retryCount - 1))).coerceAtMost(MAX_BACKOFF_DELAY)
                    Thread.sleep(delayMs)

                    // 刷新 Token
                    when (val refreshResponse = userProfileRepository.refreshToken()) {
                        is Resource.Success -> {
                            val newToken = refreshResponse.data.data?.token ?: ""
                            MainApplication.token = newToken // 更新全局 Token
                            val newRequest = originalRequest.newBuilder()
                                .addHeader("token", newToken)
                                .build()
                            response.close() // 关闭旧响应
                            return chain.proceed(newRequest) // 发送新请求
                        }
                        else -> {
                            // 刷新 Token 失败，继续重试
                            Log.w("Token Refresh", "Refresh token failed, retrying...")
                            return intercept(chain) // 递归调用 intercept 方法
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Token Refresh", "Retry attempt $retryCount failed", e)
                    // 重试失败，继续重试
                    return intercept(chain) // 递归调用 intercept 方法
                }
            }
        }

        // 如果重试次数达到上限，可以触发重新登录
        if (retryCount >= MAX_RETRY_ATTEMPTS || realResponse?.msg.equals(CommonConst.TOKEN_TIME_INVALID)) {
            Log.w("Token Refresh", "Max retry attempts reached")
            // 清除 Token
            MainApplication.clearCacheAll()
            retryCount = 0 // 重置重试计数器
            // 处理 Token 过期操作
            tokenExpiredHandler?.onTokenExpired()
        }
        retryCount = 0 // 重置重试计数器
        return response
    }
}