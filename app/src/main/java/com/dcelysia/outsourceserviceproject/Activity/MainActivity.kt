package com.dcelysia.outsourceserviceproject.Activity

import com.dcelysia.outsourceserviceproject.Fragment.AudioConverterFragment
import com.dcelysia.outsourceserviceproject.Fragment.HomeFragment
import com.dcelysia.outsourceserviceproject.Fragment.MineFragment
import com.dcelysia.outsourceserviceproject.R
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.dcelysia.outsourceserviceproject.Fragment.StoreFragment
import com.dcelysia.outsourceserviceproject.Fragment.SynthesisFragment
import com.dcelysia.outsourceserviceproject.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

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
    }
}