package com.dcelysia.outsourceserviceproject.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.outsourceserviceproject.Model.data.response.BaseUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.FileAvatarResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.UpdateUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfileResponse
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Network.RetrofitClient
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class PersonProfileViewModel : ViewModel() {
    private val repository = UserProfileRepository()

    private val _personProfileAvatar =
        MutableStateFlow("http://sql7h4hw3.hn-bkt.clouddn.com/upload/images/6a89817e-3f7b-445b-8231-2eb7e8f3b291.jpg")
    val personProfileAvatar = _personProfileAvatar.asStateFlow()

    private val _userProfile = MutableStateFlow<Resource<UserProfile>?>(null)
    val userProfile = _userProfile.asStateFlow()

    // 头像上传相关状态
    private val _uploadResult = MutableStateFlow<Resource<FileAvatarResponse>?>(null)
    val uploadResult = _uploadResult.asStateFlow()

    // 用户更新相关状态
    private val _updateResponse = MutableStateFlow<Resource<UserProfileResponse>?>(null)
    val updateResponse = _updateResponse.asStateFlow()

    init {
        loadUserProfile()
    }

    fun updateUserProfile(userProfile: UpdateUserProfile) {
        viewModelScope.launch {
            _updateResponse.value = Resource.Loading()
            _updateResponse.value = repository.updateUserProfile(userProfile)
        }
    }

    fun updateUserProfile(userProfile: UserProfile) {
        _userProfile.value = Resource.Success(userProfile)
    }

    fun updateAvatar(uri: String) {
        _personProfileAvatar.value = uri
        UserInfoManager.cacheUserProfile = UserInfoManager.cacheUserProfile?.copy(avatarUrl = uri)
        UserInfoManager.cacheBaseUserProfile =
            UserInfoManager.cacheBaseUserProfile?.copy(avatarUrl = uri)
    }

    fun uploadAvatar(file: File) {
        viewModelScope.launch {
            _uploadResult.value = repository.uploadAvatar(file)
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val cacheBaseUserProfile = UserInfoManager.cacheUserProfile
            if (cacheBaseUserProfile == null) {
                when (val response = repository.getCurrentUserProfile()) {
                    is Resource.Success -> {
                        response.data.data?.let {
                            UserInfoManager.updateUserProfile(it)
                            _userProfile.value = Resource.Success(it)
                        }
                    }

                    is Resource.Error -> {
                        _userProfile.value = Resource.Error(response.message)
                    }

                    else -> {
                    }
                }
            } else {
                Log.d("PersonProfileVM", "使用缓存的用户信息")
                _userProfile.value = Resource.Success(cacheBaseUserProfile)
            }
        }
    }
}