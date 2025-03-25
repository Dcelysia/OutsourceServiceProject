package com.dcelysia.outsourceserviceproject.Utils

import android.content.Context
import android.util.Log
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VoiceSynthesisWebsocketUtil private constructor() {
    private val TAG = "VoiceSynthesisUtil"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentCallback: SynthesisCallBack? = null
    private var currentStep: SynthesisStep = SynthesisStep.IDLE

    companion object {
        private const val BASE_URL = "ws://124.71.12.148:8000"
        private const val GET_TTS_URL = "$BASE_URL/ws/get_tts_wav"
        private const val CHANGE_SOVITS_WEIGHTS_URL = "$BASE_URL/ws/change_sovits_weights"
        private const val CHANGE_GPT_WEIGHTS_URL = "$BASE_URL/ws/change_gpt_weights"
        private const val AUDIO_PATH_PRE = "http://124.71.12.148:8000/play/audio?file_path="

        @Volatile
        private var instance: VoiceSynthesisWebsocketUtil? = null

        fun getInstance(): VoiceSynthesisWebsocketUtil {
            return instance ?: synchronized(this) {
                instance ?: VoiceSynthesisWebsocketUtil().also { instance = it }
            }
        }
    }

    fun cancel() {
        webSocket?.cancel()
        webSocket = null
        currentCallback = null
        currentStep = SynthesisStep.IDLE
    }


    enum class SynthesisStep {
        IDLE,
        LOADING_GPT,
        LOADING_SOVITS,
        GENERATE_AUDIO,
        DOWNLOAD_AUDIO
    }

    interface SynthesisCallBack {
        fun onStepStart(step: SynthesisStep)
        fun onStepComplete(step: SynthesisStep)
        fun onProgress(step: SynthesisStep)
        fun onComplete(audioFilePath: String)
        fun onError(error: String)
    }

    fun synthesisVoice(
        context: Context,
        voiceModelEntity: VoiceModelEntity,
        speed: Float,
        heText: String,
        callBack: SynthesisCallBack
    ) {
        currentCallback = callBack
        currentStep = SynthesisStep.LOADING_SOVITS
        connectWebsocket(CHANGE_SOVITS_WEIGHTS_URL, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val message = JSONObject().apply {
                    put("sovits_path", voiceModelEntity.pthModelFile)
                    put("prompt_language", "中文")
                    put("text_language", "中文")
                }.toString()
                webSocket.send(message)
                callBack.onStepStart(SynthesisStep.LOADING_SOVITS)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val status = json.optString("status")

                    if (status == "success") {
                        // 第一步完成，进行第二步
                        webSocket.cancel()
                        callBack.onStepComplete(SynthesisStep.LOADING_SOVITS)
                        loadGPTModel(
                            voiceModelEntity.ckptModelFile,
                            voiceModelEntity.referenceWavText,
                            heText,
                            voiceModelEntity.referenceWavPath,
                            speed,
                            context,
                            callBack
                        )
                    } else if (status == "processing") {
                        val stage = json.optString("stage", "")
                        callBack.onProgress(SynthesisStep.LOADING_SOVITS)
                    } else if (status == "error") {
                        val error = json.optString("message", "未知错误")
                        callBack.onError("加载SoVITS模型失败: $error")
                        webSocket.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析SoVITS响应失败", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "SoVITS WebSocket连接失败", t)
                callBack.onError("连接失败: ${t.message}")
                this@VoiceSynthesisWebsocketUtil.webSocket = null
            }
        })
    }

    private fun loadGPTModel(
        gptPath: String,
        promptText: String,
        heText: String,
        refWavPath: String,
        speed: Float,
        context: Context,
        callback: SynthesisCallBack
    ) {
        currentStep = SynthesisStep.LOADING_GPT

        connectWebsocket(CHANGE_GPT_WEIGHTS_URL, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val message = JSONObject().apply {
                    put("gpt_path", gptPath)
                }.toString()

                webSocket.send(message)
                callback.onStepStart(SynthesisStep.LOADING_GPT)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val status = json.optString("status")

                    if (status == "success") {
                        webSocket.cancel()
                        callback.onStepComplete(SynthesisStep.LOADING_GPT)
                        generateTTS(promptText, heText, refWavPath, speed, context, callback)
                    } else if (status == "processing") {
                        val stage = json.optString("stage", "")
                        callback.onProgress(SynthesisStep.LOADING_GPT)
                    } else if (status == "error") {
                        val error = json.optString("message", "未知错误")
                        callback.onError("加载GPT模型失败: $error")
                        webSocket.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析GPT响应失败", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "GPT WebSocket连接失败", t)
                callback.onError("连接失败: ${t.message}")
                this@VoiceSynthesisWebsocketUtil.webSocket = null
            }
        })
    }

    private fun generateTTS(
        promptText: String,
        text: String,
        refWavPath: String,
        speed: Float,
        context: Context,
        callback: SynthesisCallBack
    ) {
        currentStep = SynthesisStep.GENERATE_AUDIO

        connectWebsocket(GET_TTS_URL, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val message = JSONObject().apply {
                    put("prompt_text", promptText)
                    put("text", text)
                    put("ref_wav_path", refWavPath)
                    put("speed", speed)
                }.toString()

                webSocket.send(message)
                callback.onStepStart(SynthesisStep.GENERATE_AUDIO)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val status = json.optString("status")

                    if (status == "success" && json.has("audio_path")) {
                        val serverPath = json.getString("audio_path")
                        webSocket.cancel()
                        callback.onComplete(AUDIO_PATH_PRE + serverPath)
                    } else if (status == "processing") {
                        callback.onProgress(SynthesisStep.GENERATE_AUDIO)
                    } else if (status == "error") {
                        val error = json.optString("message", "未知错误")
                        callback.onError("生成语音失败: $error")
                        webSocket.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析TTS响应失败", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "TTS WebSocket连接失败", t)
                callback.onError("连接失败: ${t.message}")
                this@VoiceSynthesisWebsocketUtil.webSocket = null
            }
        })
    }

    private fun connectWebsocket(url: String, listener: WebSocketListener) {
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, listener)
    }

}