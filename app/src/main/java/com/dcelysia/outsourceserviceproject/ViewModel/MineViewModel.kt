package com.dcelysia.outsourceserviceproject.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.outsourceserviceproject.Model.data.response.BaseUserProfile
import com.dcelysia.outsourceserviceproject.Model.data.response.UpdateUserProfile
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// 偷懒，这段MVVM写的不是很规范
class MineViewModel : ViewModel() {
    private val _baseUserProfile = MutableStateFlow<Resource<BaseUserProfile>?>(null)
    private val userProfileRepository = UserProfileRepository()

    val baseUserProfile = _baseUserProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val cacheBaseUserProfile = UserInfoManager.cacheBaseUserProfile
            if (cacheBaseUserProfile == null) {
                when (val response = userProfileRepository.getCurrentUserProfile()) {
                    is Resource.Success -> {
                        response.data.data?.let {
                            UserInfoManager.updateUserProfile(it)
                            _baseUserProfile.value = Resource.Success(BaseUserProfile(it.account, it.avatarUrl))
                        }
                    }
                    is Resource.Error -> {
                        _baseUserProfile.value = Resource.Error(response.message)
                    }
                    else -> {
                    }
                }
            } else {
                _baseUserProfile.value = Resource.Success(cacheBaseUserProfile)
            }
        }
    }

    private fun updateUserProfile(updateUserProfile: UpdateUserProfile) {
        viewModelScope.launch {

        }
    }

}