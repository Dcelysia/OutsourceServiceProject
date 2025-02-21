package com.dcelysia.outsourceserviceproject.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dcelysia.outsourceserviceproject.Model.repository.LoginAndRegisterRepository
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.Utils.mmkv.LoginInfoManager
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import com.dcelysia.outsourceserviceproject.core.MainApplication
import com.dcelysia.outsourceserviceproject.core.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

class SplashActivity : AppCompatActivity() {
    private val username by lazy { LoginInfoManager.cacheUsername }
    private val password by lazy { LoginInfoManager.cachePassword }
    private val loginRepository by lazy { LoginAndRegisterRepository() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch {
            delay(400)
            Route.goHome(this@SplashActivity)
//            autoLogin()
        }
    }
    private suspend fun autoLogin() {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                Route.goLogin(this@SplashActivity)
                finish()
                return
            }
            withContext(Dispatchers.IO) {
                val response = loginRepository.login(
                    username, password
                )
                when (response) {
                    is Resource.Success -> {
                        Route.goHome(this@SplashActivity)
                    }
                    else -> {
                        Route.goLogin(this@SplashActivity)
                    }
                }
            }
        } catch (e: Exception) {
            Route.goLogin(this@SplashActivity)
            finish()
        }
    }
}

