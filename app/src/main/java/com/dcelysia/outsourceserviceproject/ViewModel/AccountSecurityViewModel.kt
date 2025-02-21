package com.dcelysia.outsourceserviceproject.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.outsourceserviceproject.Model.data.request.AccountSecurityRequest
import com.dcelysia.outsourceserviceproject.Model.data.response.MyResponse
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountSecurityViewModel : ViewModel() {

    val _oldPassword = MutableStateFlow("")
    val oldPassword = _oldPassword.asStateFlow()

    val _firstPassword = MutableStateFlow("")
    val firstPassword = _firstPassword.asStateFlow()

    val _secondPassword = MutableStateFlow("")
    val secondPassword = _secondPassword.asStateFlow()

    private val _submitResponse = MutableStateFlow<Resource<MyResponse>?>(null)
    val submitResponse = _submitResponse.asStateFlow()

    private val repository by lazy { UserProfileRepository() }

    fun updateOldPassword(text: String) {
        _oldPassword.value = text
    }

    fun updateFirstPassword(text: String) {
        _firstPassword.value = text
    }

    fun updateSecondPassword(text: String) {
        _secondPassword.value = text
    }

    fun updatePassword() {
        if (!_firstPassword.value.equals(_secondPassword.value)) {
            _submitResponse.value = Resource.Error("两次密码不一样，请重试")
            return
        }

        viewModelScope.launch {
            _submitResponse.value = Resource.Loading()
            _submitResponse.value = repository.updatePassword(AccountSecurityRequest(_oldPassword.value, _firstPassword.value))
        }
    }
}