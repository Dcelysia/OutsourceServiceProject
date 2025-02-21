package com.dcelysia.outsourceserviceproject.core

import android.content.Context
import android.content.Intent
import com.dcelysia.outsourceserviceproject.Activity.AccountSecurityActivity
import com.dcelysia.outsourceserviceproject.Activity.LoginActivity
import com.dcelysia.outsourceserviceproject.Activity.MainActivity
import com.dcelysia.outsourceserviceproject.Activity.PersonProfileActivity
import com.dcelysia.outsourceserviceproject.Activity.RegisterActivity

object Route {

    fun goAccountSecurity(context: Context) {
        val intent = Intent(context, AccountSecurityActivity::class.java)
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
}