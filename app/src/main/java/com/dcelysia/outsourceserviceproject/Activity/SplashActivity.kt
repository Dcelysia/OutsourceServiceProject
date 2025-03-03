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
            delay(500)
//            if (MainApplication.token == null) {
//                Route.goLogin(this@SplashActivity)
//            } else {
//                Route.goHome(this@SplashActivity)
//            }
//            Route.goVerificationCodeActivity(this@SplashActivity)
//            Route.goLogin(this@SplashActivity)
//            Route.goSubscriptionActivity(this@SplashActivity)
            Route.goHome(this@SplashActivity)
            finish()
        }
    }
}

