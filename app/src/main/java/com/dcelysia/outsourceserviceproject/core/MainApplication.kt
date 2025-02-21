package com.dcelysia.outsourceserviceproject.core

import android.app.Application
import android.content.Context
import com.dcelysia.outsourceserviceproject.Utils.mmkv.LoginInfoManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainApplication : Application() {
    companion object {
        fun clearCacheAll() {
            token = null
            LoginInfoManager.clear()
        }

        var token: String?
            get() = MMKV.defaultMMKV()?.getString("token", null)
            set(value) {
                MMKV.defaultMMKV()?.putString("token", value)
            }

        lateinit var appContext: Context
//        val ip = "http://124.71.12.148:8080" // 正式服务器ip
        val ip = "http://10.0.2.2:8080" // 本地测试ip
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MMKV.initialize(this@MainApplication)
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = listOf(
                async { }
            )
            tasks.awaitAll()
        }
    }

}