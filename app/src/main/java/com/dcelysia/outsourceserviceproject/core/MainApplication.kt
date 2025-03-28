package com.dcelysia.outsourceserviceproject.core

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Looper
import com.dcelysia.outsourceserviceproject.Utils.mmkv.LoginInfoManager
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.header.MaterialHeader

import com.scwang.smart.refresh.layout.SmartRefreshLayout

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

                val ip = "http://124.71.12.148:8080" // 正式服务器ip
//        val ip = "http://10.0.2.2:8080" // 本地测试ip
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MMKV.initialize(this@MainApplication)
        Looper.myQueue().addIdleHandler {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                ClassicsHeader(
                    this
                )
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
                ClassicsFooter(
                    this
                )
            }
            false
        }

        val resource = appContext.resources
        val configuration = resource.configuration
        configuration.fontScale = 1.0f
        resource.updateConfiguration(configuration, resource.displayMetrics)

        CoroutineScope(Dispatchers.IO).launch {
            val tasks = listOf(
                async { }
            )
            tasks.awaitAll()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // 当配置变化时保持字体大小不变
        if (newConfig.fontScale != 1.0f) {
            val resources = appContext.resources
            val configuration = Configuration(newConfig)
            configuration.fontScale = 1.0f
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    override fun attachBaseContext(base: Context) {
        // 创建配置以忽略系统字体大小设置
        val configuration = Configuration(base.resources.configuration)
        configuration.fontScale = 1.0f // 1.0是默认字体大小

        // 使用新配置创建上下文
        val context = base.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

}