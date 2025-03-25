package com.dcelysia.outsourceserviceproject.Fragment

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.Model.Room.database.VoiceModelDataBase
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CustomToast
import com.dcelysia.outsourceserviceproject.Utils.VoiceSynthesisWebsocketUtil
import com.dcelysia.outsourceserviceproject.databinding.FragmentVoiceSynthesisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class VoiceSynthesisFragment : Fragment() {

    private lateinit var binding: FragmentVoiceSynthesisBinding

    private val speechRates = arrayOf(
        0.5f,
        0.6f,
        0.7f,
        0.8f,
        0.9f,
        1.0f,
        1.1f,
        1.2f,
        1.3f,
        1.4f,
        1.5f,
        1.6f,
        1.7f,
        1.8f,
        1.9f,
        2.0f
    )
    private var currentSpeechRateIndex = 5 // 1.0f is the default

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private val playLayout by lazy { binding.voicePlayLayout }
    private var modelId = -1

    private val webSocket = VoiceSynthesisWebsocketUtil.getInstance()
    private var audioFilePath: String? = null
    private var loadingDialog: AlertDialog? = null

    private val voiceModelDatabase by lazy {
        VoiceModelDataBase.getInstance(requireContext())
    }
    private val updateProgressAction = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoiceSynthesisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVoiceModelSpinner()
        setupSpeechRateSeekBar()
        setupPlayerControls()
        setupSynthesizeButton()
        setupSettingsButton()
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun setupVoiceModelSpinner() {
        binding.voiceModelChosen.setOnClickListener {
            findNavController().navigate(R.id.action_voiceSynthesisFragment_to_modelsFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        val args = VoiceSynthesisFragmentArgs.fromBundle(requireArguments())
        binding.voiceModelChosen.text = args.modelName
        modelId = args.modelId
    }

    private fun setupSpeechRateSeekBar() {
        // Set initial progress to 1.0x (middle of the range)
        binding.speechRateSeekBar.progress = 50
        binding.speechRateValueText.text = "1.0x"

        binding.speechRateSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Map progress (0-100) to our speechRates array index
                val index = (progress * (speechRates.size - 1) / 100.0f).toInt()
                currentSpeechRateIndex = index
                binding.speechRateValueText.text = "${speechRates[index]}x"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupPlayerControls() {
        binding.playButton.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }

        binding.stopButton.setOnClickListener {
            stopPlayback()
        }
    }

    private fun setupSynthesizeButton() {
        binding.synthesizeButton.setOnClickListener {
            val text = binding.textInputEditText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "请输入要合成的文本内容", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            stopPlayback()

            // Show loading state
//            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.synthesizeButton.isEnabled = false
            playLayout.visibility = View.GONE

            // 开始语音合成
            synthesizeVoice(text)
        }
    }

    private fun setupSettingsButton() {
        binding.settingsButton.setOnClickListener {
            Toast.makeText(requireContext(), "设置功能暂未实现", Toast.LENGTH_SHORT).show()
        }
    }

    private fun synthesizeVoice(text: String) {
        lifecycleScope.launch {
            try {
                val data: VoiceModelEntity? = withContext(Dispatchers.IO) {
                    voiceModelDatabase.voiceModelDao().getModelByVoiceItemId(modelId)
                }

                if (data == null) {
                    CustomToast.showMessage(requireContext(), "未找到模型数据，请稍后重试")
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.synthesizeButton.isEnabled = true
                    return@launch
                }

                showLoadingDialog()
                val speed = speechRates[currentSpeechRateIndex]

                webSocket.synthesisVoice(
                    requireContext(),
                    data,
                    speed,
                    text,
                    object : VoiceSynthesisWebsocketUtil.SynthesisCallBack {
                        override fun onStepStart(step: VoiceSynthesisWebsocketUtil.SynthesisStep) {
                            requireActivity().runOnUiThread {
                                val message = when (step) {
                                    VoiceSynthesisWebsocketUtil.SynthesisStep.LOADING_SOVITS -> "正在加载SoVITS模型..."
                                    VoiceSynthesisWebsocketUtil.SynthesisStep.LOADING_GPT -> "正在加载GPT模型..."
                                    VoiceSynthesisWebsocketUtil.SynthesisStep.GENERATE_AUDIO -> "正在生成语音..."
                                    else -> "正在处理..."
                                }
                                updateLoadingProgress(message)
                            }
                        }

                        override fun onStepComplete(step: VoiceSynthesisWebsocketUtil.SynthesisStep) {
                            // 步骤完成时的处理
                        }

                        override fun onProgress(step: VoiceSynthesisWebsocketUtil.SynthesisStep) {
                            // 进度更新时的处理
                        }

                        override fun onComplete(audioFilePath: String) {
                            requireActivity().runOnUiThread {
                                hideLoadingDialog()
                                this@VoiceSynthesisFragment.audioFilePath = audioFilePath

                                binding.loadingProgressBar.visibility = View.GONE
                                binding.synthesizeButton.isEnabled = true
                                playLayout.visibility = View.VISIBLE

                                // 设置并开始播放音频
                                setupMediaPlayer(audioFilePath)
                            }
                        }

                        override fun onError(error: String) {
                            requireActivity().runOnUiThread {
                                hideLoadingDialog()
                                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()

                                binding.loadingProgressBar.visibility = View.GONE
                                binding.synthesizeButton.isEnabled = true
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    Toast.makeText(requireContext(), "发生错误: ${e.message}", Toast.LENGTH_SHORT).show()

                    binding.loadingProgressBar.visibility = View.GONE
                    binding.synthesizeButton.isEnabled = true
                }
            }
        }
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun updateLoadingProgress(message: String) {
        loadingDialog?.findViewById<TextView>(R.id.messageText)?.text = message
    }

    private fun showLoadingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)  // 允许取消
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                webSocket.cancel()
            }
            .create()

        loadingDialog?.show()
    }

    private fun setupMediaPlayer(audioUrl: String) {
        // 清理现有的 MediaPlayer
        releaseMediaPlayer()

        try {
            // 创建新的 MediaPlayer 实例
            mediaPlayer = MediaPlayer().apply {
                // 设置音频属性
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // 设置错误监听器
                setOnErrorListener { _, what, extra ->
                    val errorMsg = when (what) {
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "未知错误"
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "服务器错误"
                        else -> "播放错误 ($what, $extra)"
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    true
                }

                // 设置完成监听器
                setOnCompletionListener {
                    stopPlayback()
                }

                // 设置准备完成监听器
                setOnPreparedListener {
                    // 准备完成后自动开始播放
                    startPlayback()

                    // 更新总时长显示
                    val duration = it.duration
                    binding.timeTextView.text = String.format(
                        "%s / %s",
                        "00:00",
                        formatTime(duration.toLong())
                    )
                }

                // 设置数据源并异步准备
                setDataSource(audioUrl)
                prepareAsync()
            }
        } catch (e: IOException) {
            Toast.makeText(
                requireContext(),
                "播放器初始化失败: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startPlayback() {
        if (mediaPlayer == null && audioFilePath != null) {
            // 如果MediaPlayer为空但有音频路径，重新初始化
            setupMediaPlayer(audioFilePath!!)
            return
        }

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
            binding.playButton.setIconResource(R.drawable.ic_pause)

            // 开始进度更新
            handler.removeCallbacks(updateProgressAction)
            handler.post(updateProgressAction)
        }
    }

    private fun pausePlayback() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            binding.playButton.setIconResource(R.drawable.ic_play)

            // 停止进度更新
            handler.removeCallbacks(updateProgressAction)
        }
    }

    private fun stopPlayback() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.seekTo(0)
                isPlaying = false
                binding.playButton.setIconResource(R.drawable.ic_play)

                // 重置进度
                binding.progressIndicator.progress = 0

                // 更新时间显示
                val duration = mediaPlayer?.duration ?: 0
                binding.timeTextView.text = String.format(
                    "%s / %s",
                    "00:00",
                    formatTime(duration.toLong())
                )
            } else {
                // 如果没有MediaPlayer，只重置UI
                binding.progressIndicator.progress = 0
                binding.timeTextView.text = "00:00 / 00:00"
            }

            // 停止进度更新
            handler.removeCallbacks(updateProgressAction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateProgressBar() {
        if (!isPlaying || mediaPlayer == null) return

        try {
            val currentPosition = mediaPlayer?.currentPosition ?: 0
            val duration = mediaPlayer?.duration ?: 0

            if (duration > 0) {
                // 计算进度百分比
                val progress = (currentPosition * 100 / duration)
                binding.progressIndicator.progress = progress

                // 更新时间文本
                binding.timeTextView.text = String.format(
                    "%s / %s",
                    formatTime(currentPosition.toLong()),
                    formatTime(duration.toLong())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatTime(timeMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            isPlaying = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        // 暂停播放
        if (mediaPlayer?.isPlaying == true) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressAction)
        releaseMediaPlayer()
        webSocket.cancel()
        hideLoadingDialog()
    }
}