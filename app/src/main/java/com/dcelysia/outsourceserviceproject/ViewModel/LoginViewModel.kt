package com.dcelysia.outsourceserviceproject.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Model.repository.LoginAndRegisterRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = LoginAndRegisterRepository()
    private val _loginState = MutableSharedFlow<Resource<LoginAndRegisterResponse>?>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val loginState = _loginState.asSharedFlow()

    private val _account = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _isAgreed = MutableStateFlow(false)
    private val _isPasswordVisible = MutableStateFlow(false)
    private val _isLoginEnabled = MutableStateFlow(false)

    val isLoginEnabled = _isLoginEnabled.asStateFlow()
    val isPasswordVisible = _isPasswordVisible.asStateFlow()

    fun updateAccount(account: String) {
        _account.value = account
        checkIsEnabled()
    }

    fun updatePassword(password: String) {
        _password.value = password
        checkIsEnabled()
    }

    fun updateIsAgree(isAgree: Boolean) {
        _isAgreed.value = isAgree
        checkIsEnabled()
    }

    fun updatePasswordVisible(isVisible: Boolean) {
        _isPasswordVisible.value = isVisible
    }

    private fun checkIsEnabled() {
        _isLoginEnabled.value =
            _account.value.isNotEmpty() && _password.value.isNotEmpty() && _isAgreed.value
    }

    fun login() {
        viewModelScope.launch {
            _loginState.emit(Resource.Loading())
            _loginState.emit(repository.login(_account.value, _password.value))
            // 测试
//            _loginState.emit(Resource.Success(LoginAndRegisterResponse("200", "ok", null)))
        }
    }

    fun  getAccount(): MutableStateFlow<String> {
        return _account
    }
}