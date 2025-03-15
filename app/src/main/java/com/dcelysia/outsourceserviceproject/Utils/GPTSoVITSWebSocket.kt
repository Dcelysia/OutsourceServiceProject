// GPTSoVITSWebSocket.kt
package com.dcelysia.outsourceserviceproject.Utils

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class GPTSoVITSWebSocket private constructor() {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val processState = ProcessState()

    interface ProcessCallback {
        fun onStepComplete(step: ProcessStep, result: String)
        fun onError(error: String)
        fun onComplete(username: String, modelName: String) // 修改为返回用户名和模型名
    }

    enum class ProcessStep {
        UVR_CONVERT,
        SLICE,
        ASR,
        TRIPLE_PROCESS,
        SOVITS_TRAINING,
        GPT_TRAINING
    }

    private class ProcessState {
        var currentStep: ProcessStep? = null
        val completed = mutableSetOf<ProcessStep>()
    }

    fun startProcess(
        username: String,
        modelName: String,
        audioUri: Uri,
        context: Context,
        callback: ProcessCallback
    ) {
        // 重置状态
        processState.currentStep = null
        processState.completed.clear()

        // 首先上传文件
        uploadFile(username, modelName, audioUri, context) { uploadResult ->
            if (uploadResult.isSuccess) {
                val filePath = uploadResult.getOrNull()
                if (filePath != null) {
                    // 文件上传成功，建立WebSocket连接
                    connectWebSocket(username, modelName, filePath, callback)
                } else {
                    callback.onError("文件上传失败：未获取到文件路径")
                }
            } else {
                callback.onError("文件上传失败：${uploadResult.exceptionOrNull()?.message}")
            }
        }
    }

    private fun uploadFile(
        username: String,
        modelName: String,
        audioUri: Uri,
        context: Context,
        callback: (Result<String>) -> Unit
    ) {
        try {
            // 从Uri获取文件
            val file = getFileFromUri(audioUri, context) ?: throw Exception("无法读取音频文件")

            // 创建MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("model_name", modelName)
                .addFormDataPart(
                    "audio",
                    file.name,
                    file.asRequestBody("audio/*".toMediaTypeOrNull())
                )
                .build()

            // 创建上传请求
            val request = Request.Builder()
                .url("${SERVER_URL}/upload")
                .post(requestBody)
                .build()

            // 执行请求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            throw IOException("Unexpected code $response")
                        }

                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)

                        if (jsonObject.getString("status") == "success") {
                            val filePath = jsonObject.getString("file_path")
                            callback(Result.success(filePath))
                        } else {
                            throw Exception(jsonObject.getString("error"))
                        }
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            })

        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    private fun getFileFromUri(uri: Uri, context: Context): File? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.wav")
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun connectWebSocket(
        username: String,
        modelName: String,
        filePath: String,
        callback: ProcessCallback
    ) {
        val request = Request.Builder()
            .url("${SERVER_URL}/ws/process")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 发送初始化数据
                val initData = JSONObject().apply {
                    put("username", username)
                    put("model_name", modelName)
                    put("file_path", filePath)
                }
                webSocket.send(initData.toString())
            }

            // 修改 onMessage 方法中的处理逻辑
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    val json = JSONObject(text)
                    when (json.getString("status")) {
                        "processing" -> {
//                            val step = json.getString("step")
//                            Log.d("step", step)
//                            processState.currentStep = getStepFromString(step)
                        }

                        "success" -> {
                            val step = json.getString("step")
                            processState.currentStep = getStepFromString(step)
                            Log.d("step", step)
                            processState.currentStep?.let { step ->
                                processState.completed.add(step)
                                // 获取结果信息
                                val result = json.optString("result", "")

                                Handler(Looper.getMainLooper()).post {
                                    callback.onStepComplete(step, result)
                                    // 检查是否所有步骤都完成
                                    if (processState.completed.size == ProcessStep.values().size) {
                                        callback.onComplete(username, modelName)
                                    }
                                }
                            }
                        }

                        "error" -> {
                            val error = json.getString("error")
                            callback.onError(error)
                        }
                    }
                } catch (e: Exception) {
                    callback.onError("消息解析错误: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                callback.onError("WebSocket连接失败: ${t.message}")
            }
        })
    }

    private fun getStepFromString(step: String): ProcessStep? {
        return when (step) {
            "uvr_convert" -> ProcessStep.UVR_CONVERT
            "slice" -> ProcessStep.SLICE
            "asr" -> ProcessStep.ASR
            "triple_process" -> ProcessStep.TRIPLE_PROCESS
            "sovits_training" -> ProcessStep.SOVITS_TRAINING
            "gpt_training" -> ProcessStep.GPT_TRAINING
            else -> null
        }
    }

    fun cancel() {
        webSocket?.close(1000, "用户取消")
        webSocket = null
        processState.currentStep = null
        processState.completed.clear()
    }

    fun uploadReferenceFile(
        username: String,
        modelName: String,
        audioUri: Uri,
        context: Context,
        callback: (Result<String>) -> Unit
    ) {
        try {
            val file = getFileFromUri(audioUri, context) ?: throw Exception("无法读取音频文件")

            val request = Request.Builder()
                .url("${SERVER_URL}/ws/upload_reference")
                .build()

            val webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    try {
                        // 发送初始化数据
                        val initData = JSONObject().apply {
                            put("username", username)
                            put("model_name", modelName)
                        }
                        webSocket.send(initData.toString())

                        // 发送文件数据
                        file.inputStream().use { input ->
                            val buffer = ByteArray(1024 * 1024) // 1MB buffer
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                if (bytesRead > 0) {
                                    val bytes = buffer.copyOfRange(0, bytesRead)
                                    // 使用 ByteString.toByteString() 扩展函数
                                    webSocket.send(bytes.toByteString())
                                }
                            }
                        }

                        // 发送结束标记
                        webSocket.send("done")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending file", e)
                        Handler(Looper.getMainLooper()).post {
                            callback(Result.failure(e))
                        }
                        webSocket.close(1000, "发送失败")
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        Log.d(TAG, "Received message: $text")
                        val json = JSONObject(text)
                        when (json.getString("status")) {
                            "success" -> {
                                val filePath = json.getString("file_path")
                                Handler(Looper.getMainLooper()).post {
                                    callback(Result.success(filePath))
                                }
                                webSocket.close(1000, "上传完成")
                            }

                            "error" -> {
                                val error = json.getString("error")
                                Handler(Looper.getMainLooper()).post {
                                    callback(Result.failure(Exception(error)))
                                }
                                webSocket.close(1000, "上传失败")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing message", e)
                        Handler(Looper.getMainLooper()).post {
                            callback(Result.failure(e))
                        }
                        webSocket.close(1000, "处理响应失败")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    Handler(Looper.getMainLooper()).post {
                        callback(Result.failure(t))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closing: $code - $reason")
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: $code - $reason")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadReferenceFile", e)
            Handler(Looper.getMainLooper()).post {
                callback(Result.failure(e))
            }
        }
    }

    // ByteString 扩展函数
    private fun ByteString.Companion.toByteString(bytes: ByteArray): ByteString {
        return bytes.toByteString()
    }

    companion object {
        private const val TAG = "GPTSoVITSWebSocket"
        private const val SERVER_URL = "http://124.71.12.148:8000"  // 替换为实际的服务器地址

        @Volatile
        private var instance: GPTSoVITSWebSocket? = null

        fun getInstance(): GPTSoVITSWebSocket {
            return instance ?: synchronized(this) {
                instance ?: GPTSoVITSWebSocket().also { instance = it }
            }
        }
    }


}