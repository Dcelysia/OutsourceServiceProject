package com.dcelysia.outsourceserviceproject.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.core.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

