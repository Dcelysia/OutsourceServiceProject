package com.dcelysia.outsourceserviceproject.Activity

import com.dcelysia.outsourceserviceproject.R
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.dcelysia.outsourceserviceproject.core.Route
import com.dcelysia.outsourceserviceproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 设置底部导航与NavController的关联
        binding.bottomNavigation.setupWithNavController(navController)

        // 确保底部导航栏可以正确响应点击事件
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }

        binding.miniPlayerLayout.setOnClickListener {
            Route.goAlbum(this@MainActivity)
        }

        // 默认隐藏播放栏
        binding.miniPlayerLayout.visibility = View.GONE

        // 添加导航监听器，根据目的地决定是否显示播放栏
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateMiniPlayerVisibility(destination)
        }
    }

    /**
     * 根据当前导航目的地更新播放栏的可见性
     */
    private fun updateMiniPlayerVisibility(destination: NavDestination) {
        // 判断当前是否是AlbumFragment
        val isAlbumFragment = destination.id == R.id.albumFragment

        // 如果是AlbumFragment，显示播放栏，否则隐藏
        binding.miniPlayerLayout.visibility = if (isAlbumFragment) View.VISIBLE else View.GONE
    }
}