package com.dcelysia.outsourceserviceproject.Activity

import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dcelysia.outsourceserviceproject.Fragment.SynthesisFragment
import com.dcelysia.outsourceserviceproject.Fragment.HomeFragment
import com.dcelysia.outsourceserviceproject.Fragment.StoreFragment
import com.dcelysia.outsourceserviceproject.Fragment.MineFragment
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import com.dcelysia.outsourceserviceproject.core.MainApplication
import com.dcelysia.outsourceserviceproject.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userProfileRepository by lazy { UserProfileRepository() }
    private val homeNavigation by lazy { binding.homeNavigation }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        homeNavigation.itemIconTintList = null
        initFragment(HomeFragment.newInstance())
        Looper.myQueue().addIdleHandler {
            lifecycleScope.launch {
               getCurrentUserProfile()
            }
            false
        }
        homeNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    initFragment(HomeFragment.newInstance())
                    true
                }
                R.id.nav_synthesis -> {
                    initFragment(SynthesisFragment.newInstance())
                    true
                }
                R.id.nav_mine -> {
                    initFragment(MineFragment.newInstance())
                    true
                }
                R.id.nav_store -> {
                    initFragment(StoreFragment.newInstance())
                    true
                }
                else -> false
            }
        }

    }

    private suspend fun getCurrentUserProfile() {
        val response = userProfileRepository.getCurrentUserProfile()
        when (response) {
            is Resource.Success -> {
                response.data.data?.let {
                    UserInfoManager.updateUserProfile(it)
                    Glide.with(MainApplication.appContext)
                        .load(it.avatarUrl)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .preload()
                }
            } else -> {
                UserInfoManager.clearAll()
            }
        }
    }

    private fun initFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.home_content, fragment).commit()
    }
}