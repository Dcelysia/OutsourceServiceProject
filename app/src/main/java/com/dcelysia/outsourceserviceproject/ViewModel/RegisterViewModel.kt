package com.dcelysia.outsourceserviceproject.ViewModel

import android.util.Log
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

class RegisterViewModel : ViewModel() {

    private val TAG = "RegisterViewModel"

    private val repository = LoginAndRegisterRepository()
    private val _account = MutableStateFlow("")
    private val _password1 = MutableStateFlow("")
    private val _password2 = MutableStateFlow("")

    private val _isAgreed = MutableStateFlow(false)
    private val _isPassword1Visible = MutableStateFlow(false)
    private val _isPassword2Visible = MutableStateFlow(false)
    private val _isRegisterEnabled = MutableStateFlow(false)

    val isRegisterEnabled = _isRegisterEnabled.asStateFlow()
    val isPassword1Visible = _isPassword1Visible.asStateFlow()
    val isPassword2Visible = _isPassword2Visible.asStateFlow()

    private val _registerState = MutableSharedFlow<Resource<LoginAndRegisterResponse>?>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val registerState = _registerState.asSharedFlow()

    fun updatePassword1(message: String) {
        _password1.value = message
        checkIsEnabled()
    }

    fun updatePassword2(message: String) {
        _password2.value = message
        checkIsEnabled()
    }

    fun updateAccount(message: String) {
        _account.value = message
        checkIsEnabled()
    }

    fun updateAgree(isAgree: Boolean) {
        _isAgreed.value = isAgree
        checkIsEnabled()
    }

    fun updatePassword1Visible(isVisible: Boolean) {
        _isPassword1Visible.value = isVisible
    }

    fun updatePassword2Visible(isVisible: Boolean) {
        _isPassword2Visible.value = isVisible
    }

    fun checkIsEnabled() {
        _isRegisterEnabled.value =
            _account.value.isNotEmpty() && _password1.value.isNotEmpty() && _password2.value.isNotEmpty()
                    && _isAgreed.value
    }

    fun register() {
        viewModelScope.launch {
            Log.d(TAG, "进入")
            _registerState.emit(Resource.Loading())
            Log.d(TAG, "更改状态")
            if(!_password1.value.equals(_password2.value)) {
                _registerState.emit(Resource.Error("两次输入的密码不相同"))
                return@launch
            }
            Log.d(TAG, "第二阶段")
            Thread.sleep(300)
           // _registerState.value =
//            _registerState.emit(Resource.Success(LoginAndRegisterResponse("200", "ok", null)))
            _registerState.emit(repository.register(_account.value, _password1.value))
        }
    }
}