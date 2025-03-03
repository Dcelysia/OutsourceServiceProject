package com.dcelysia.outsourceserviceproject.UI

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.dcelysia.outsourceserviceproject.R

class CustomToast private constructor(context: Context) {
    private val toast: Toast

    init {
        val appContext = context.applicationContext
        val layout = LayoutInflater.from(appContext).inflate(R.layout.custom_toast_layout, null)
        toast = Toast(appContext).apply {
            view = layout
            duration = Toast.LENGTH_SHORT
        }
    }

    companion object {
        fun showMessage(context: Context, message: String) {
            CustomToast(context.applicationContext).show(message)
        }
    }

    fun show(message: String) {
        toast.view?.findViewById<TextView>(R.id.toast_text)?.text = message
        toast.show()
    }
}