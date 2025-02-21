package com.dcelysia.outsourceserviceproject.Utils.mmkv

import com.tencent.mmkv.MMKV

object LoginInfoManager {
    private val mmkv by lazy { MMKV.mmkvWithID("login_info") }
    private const val KEY_ACCOUNT = "login_account"
    private const val KEY_PASSWORD = "login_password"
    private const val KEY_REMEMBER_FLAG = "login_isRemember"
    var cacheUsername: String
        get() = mmkv.getString(KEY_ACCOUNT, "") ?: ""
        set(value) {
            mmkv.putString(KEY_ACCOUNT, value)
        }

    var cachePassword: String
        get() = mmkv.getString(KEY_PASSWORD, "") ?: ""
        set(value) {
            mmkv.putString(KEY_PASSWORD, value)
        }

    var cacheRememberFlag: Boolean
        get() = mmkv.getBoolean(KEY_REMEMBER_FLAG, false)
        set(value) {
            mmkv.putBoolean(KEY_REMEMBER_FLAG, value)
        }

    fun clear() {
        mmkv.clearAll()
    }
}