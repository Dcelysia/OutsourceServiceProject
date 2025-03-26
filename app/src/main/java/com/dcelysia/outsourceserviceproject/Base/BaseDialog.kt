package com.dcelysia.outsourceserviceproject.Base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.core.MainApplication

abstract class BaseDialog(context: Context?) : Dialog(
    context ?: MainApplication.appContext, R.style.CustomDialog
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        init()
    }

    protected abstract fun init()
    protected abstract fun layoutId(): Int
}