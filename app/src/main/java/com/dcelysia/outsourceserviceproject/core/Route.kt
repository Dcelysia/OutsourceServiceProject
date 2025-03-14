package com.dcelysia.outsourceserviceproject.core

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dcelysia.outsourceserviceproject.Activity.AccountSecurityActivity
import com.dcelysia.outsourceserviceproject.Activity.AlbumActivity
import com.dcelysia.outsourceserviceproject.Activity.LoginActivity
import com.dcelysia.outsourceserviceproject.Activity.MainActivity
import com.dcelysia.outsourceserviceproject.Activity.PersonProfileActivity
import com.dcelysia.outsourceserviceproject.Activity.RegisterActivity
import com.dcelysia.outsourceserviceproject.Activity.SubscriptionActivity
import com.dcelysia.outsourceserviceproject.Activity.VerificationCodeActivity
import com.dcelysia.outsourceserviceproject.R


object Route {

    fun goAccountSecurity(context: Context) {
        val intent = Intent(context, AccountSecurityActivity::class.java)
        context.startActivity(intent)
    }

    fun goVerificationCodeActivity(context: Context) {
        val intent = Intent(context, VerificationCodeActivity::class.java)
        context.startActivity(intent)
    }

    fun goSubscriptionActivity(context: Context) {
        val intent = Intent(context, SubscriptionActivity::class.java)
        context.startActivity(intent)
    }

    fun goLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }

    fun goRegister(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    fun goLoginFromRegister(context: Context, account: String, password: String) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra("register_account", account)
        intent.putExtra("register_password", password)
        context.startActivity(intent)
    }

    fun goLoginForcibly(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun goHome(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    fun goPersonProfile(context: Context) {
        val intent = Intent(context, PersonProfileActivity::class.java)
        context.startActivity(intent)
    }

    fun goAlbum(context: Context) {
        val intent = Intent(context, AlbumActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_up, R.anim.slide_out_down)
        context.startActivity(intent, options.toBundle())
    }

    fun loadFragment(fragment: Fragment,activity: AppCompatActivity) {
        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        // 可以选择添加动画效果
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        // 替换 Fragment
        fragmentTransaction.add(R.id.main_activity, fragment,"")
        fragmentTransaction.addToBackStack(null) // 可选，允许用户返回到上一个 Fragment
        fragmentTransaction.commit()
    }
}