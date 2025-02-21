package com.dcelysia.outsourceserviceproject.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.outsourceserviceproject.Model.data.response.FileAvatarResponse
import com.dcelysia.outsourceserviceproject.Model.data.response.LoginAndRegisterResponse
import com.dcelysia.outsourceserviceproject.Model.repository.UserProfileRepository
import com.dcelysia.outsourceserviceproject.Network.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

class UserProfileViewModel : ViewModel(){

    private val _avatarUrl = MutableLiveData<Uri>(Uri.parse("http://localhost"))
    private val repository = UserProfileRepository()
    val avatarUri : LiveData<Uri> = _avatarUrl

    private val _personProfileState = MutableSharedFlow<Resource<LoginAndRegisterResponse>?>(
        replay = 0,
        extraBufferCapacity = 1
    )
    private val _uploadAvatarState = MutableSharedFlow<Resource<FileAvatarResponse>?>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uploadAvatarState = _uploadAvatarState.asSharedFlow()

    fun setAvatarUri(uri: Uri) {
        _avatarUrl.value = uri
    }

    fun uploadAvatar(file: File) = viewModelScope.launch {
        _uploadAvatarState.emit(Resource.Loading())

        _uploadAvatarState.emit(repository.uploadAvatar(file))
    }
}