package com.dcelysia.outsourceserviceproject.Utils.mmkv

import com.dcelysia.outsourceserviceproject.Model.data.response.BaseUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfile
import com.google.gson.Gson
import com.tencent.mmkv.MMKV

object UserInfoManager {

    private val mmkv by lazy { MMKV.mmkvWithID("user_info") }

    private const val KEY_MY_USER = "my_user_profile"
    private const val KEY_MY_BASE_USER = "my_user_base_profile"
    fun updateUserProfile(userProfile: UserProfile) {
        mmkv.encode(KEY_MY_USER, Gson().toJson(userProfile))
        val baseUserProfile = BaseUserProfile(userProfile.account, userProfile.avatarUrl)
        mmkv.encode(KEY_MY_BASE_USER, Gson().toJson(baseUserProfile))
    }
    var cacheUserProfile: UserProfile?
        get() {
            val userJson = mmkv.decodeString(KEY_MY_USER, null)
            return userJson?.let { Gson().fromJson(it, UserProfile::class.java) }
        }
        set(value) {
            val userJson = Gson().toJson(value)
            mmkv.encode(KEY_MY_USER, userJson)
        }

    var cacheBaseUserProfile: BaseUserProfile?
        get() {
            val userJson = mmkv.decodeString(KEY_MY_BASE_USER, null)
            return userJson?.let { Gson().fromJson(it, BaseUserProfile::class.java) }
        }
        set(value) {
            val userJson = Gson().toJson(value)
            mmkv.encode(KEY_MY_BASE_USER, userJson)
        }

    fun clearAll() {
        mmkv.clearAll()
    }
}