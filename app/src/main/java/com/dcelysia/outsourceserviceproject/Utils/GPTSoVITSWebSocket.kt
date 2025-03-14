package com.dcelysia.outsourceserviceproject.Utils
// GPTSoVITSWebSocket.kt
import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

class GPTSoVITSWebSocket private constructor() {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS) // 10分钟超时
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val CHUNK_SIZE = 1024 * 1024 // 1MB
        private const val TAG = "GPTSoVITSWebSocket"

        @Volatile
        private var instance: GPTSoVITSWebSocket? = null

        fun getInstance(): GPTSoVITSWebSocket {
            return instance ?: synchronized(this) {
                instance ?: GPTSoVITSWebSocket().also { instance = it }
            }
        }
    }

    // 定义处理步骤的枚举
    enum class ProcessStep {
        UVR_CONVERT,
        SLICE,
        ASR,
        TRIPLE_PROCESS,
        SOVITS_TRAINING,
        GPT_TRAINING
    }

    // 定义回调接口
    interface ProcessCallback {
        fun onFileUploadProgress(progress: Float)
        fun onStepStart(step: ProcessStep)
        fun onStepComplete(step: ProcessStep, result: String)
        fun onError(error: String)
        fun onComplete()
    }

    // 处理状态类
    data class ProcessState(
        var currentStep: ProcessStep? = null,
        var completed: MutableSet<ProcessStep> = mutableSetOf()
    )

    private val processState = ProcessState()

    fun startProcess(
        serverUrl: String,
        username: String,
        modelName: String,
        originalAudioUri: Uri,
        referenceAudioUri: Uri,
        trainingText: String,
        context: Context,
        callback: ProcessCallback
    ) {
        val request = Request.Builder()
            .url("ws://$serverUrl/ws/process")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 发送初始信息
                val initialData = JSONObject().apply {
                    put("username", username)
                    put("model_name", modelName)
                    put("text", trainingText)
                    put("prompt_text", trainingText)
                }
                webSocket.send(initialData.toString())

                // 开始上传原始音频
                uploadAudioFile(context, originalAudioUri, callback)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text, callback)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                callback.onError("连接失败: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }
        })
    }

    private fun uploadAudioFile(context: Context, uri: Uri, callback: ProcessCallback) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { inputStream ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    val fileSize = parcelFileDescriptor.statSize

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (bytesRead > 0) {
                            val chunk = if (bytesRead < CHUNK_SIZE) {
                                buffer.copyOf(bytesRead)
                            } else {
                                buffer
                            }

                            webSocket?.send(ByteString.of(*chunk))
                            totalBytesRead += bytesRead

                            // 更新上传进度
                            val progress = (totalBytesRead.toFloat() / fileSize * 100)
                            callback.onFileUploadProgress(progress)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError("文件上传失败: ${e.message}")
            webSocket?.close(1000, "文件上传失败")
        }
    }

    private fun handleMessage(text: String, callback: ProcessCallback) {
        try {
            val response = JSONObject(text)
            Log.d(TAG, "Received message: $text")

            when (response.getString("status")) {
                "success" -> {
                    val step = response.getString("step")
                    val result = when {
                        response.has("result") -> {
                            when (val resultObj = response.get("result")) {
                                is String -> resultObj
                                else -> resultObj.toString()
                            }
                        }

                        else -> "完成"
                    }

                    when (step) {
                        "uvr_convert" -> updateStep(ProcessStep.UVR_CONVERT, result, callback)
                        "slice" -> updateStep(ProcessStep.SLICE, result, callback)
                        "asr" -> updateStep(ProcessStep.ASR, result, callback)
                        "triple_process" -> updateStep(ProcessStep.TRIPLE_PROCESS, result, callback)
                        "sovits_training" -> updateStep(
                            ProcessStep.SOVITS_TRAINING,
                            result,
                            callback
                        )

                        "gpt_training" -> {
                            updateStep(ProcessStep.GPT_TRAINING, result, callback)
                            callback.onComplete()
                        }
                    }
                }

                "error" -> {
                    callback.onError(response.getString("error"))
                }

                "processing" -> {
                    // 处理进度信息
                    if (response.has("progress")) {
                        val progress = response.getDouble("progress").toFloat()
                        callback.onFileUploadProgress(progress)
                    }
                }
            }
        } catch (e: Exception) {
            callback.onError("解析响应失败: ${e.message}")
        }
    }

    private fun updateStep(step: ProcessStep, result: String, callback: ProcessCallback) {
        processState.currentStep = step
        processState.completed.add(step)
        callback.onStepComplete(step, result)
    }

    fun cancel() {
        webSocket?.close(1000, "用户取消")
        webSocket = null
        processState.currentStep = null
        processState.completed.clear()
    }
}