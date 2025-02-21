package com.dcelysia.outsourceserviceproject.Network

import okhttp3.Response

interface RequestCallback {
    fun onSuccess(response: Response)
    fun onFailure(error: String)
}
