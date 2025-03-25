package com.dcelysia.outsourceserviceproject.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.Model.Room.database.VoiceModelDataBase
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.Utils.GPTSoVITSWebSocket
import com.dcelysia.outsourceserviceproject.databinding.FragmentAudioTrainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import androidx.core.net.toUri

class AudioTrainingFragment : Fragment() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentAudioTrainBinding.inflate(layoutInflater)
    }

    private var originalMediaPlayer: MediaPlayer? = null
    private var referenceMediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var originalAudioUri: Uri? = null
    private var referenceAudioUri: Uri? = null
    private var isTraining = false

    private val webSocket = GPTSoVITSWebSocket.getInstance()
    private val RECORD_AUDIO_REQUEST = 2

    private var isOriginalPrepared = false
    private var isReferencePrepared = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取原始音频URI
        val args = AudioTrainingFragmentArgs.fromBundle(requireArguments())
        if (args.audioUri.isNotEmpty()) {
            val audioUri = args.audioUri.toUri()
            originalAudioUri = audioUri
            binding.fileNameTextView.text = "已选择文件：${getFileName(audioUri)}"
            initializeMediaPlayer(audioUri, isReference = false)
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 原始音频播放控制
        binding.originalPlayPauseButton.setOnClickListener {
            togglePlayPause(isReference = false)
        }

        // 参考音频播放控制
        binding.referencePlayPauseButton.setOnClickListener {
            togglePlayPause(isReference = true)
        }

        // 原始音频进度条
        binding.originalSeekBar.setOnSeekBarChangeListener(createSeekBarListener(false))

        // 参考音频进度条
        binding.referenceSeekBar.setOnSeekBarChangeListener(createSeekBarListener(true))

        // 上传参考音频按钮
        binding.uploadReferenceButton.setOnClickListener {
            openAudioFilePicker()
        }
        // 开始训练按钮
        binding.startTrainingButton.setOnClickListener {
            validateAndStartTraining()
        }
    }

    private fun openAudioFilePicker() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "选择音频文件"), RECORD_AUDIO_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "未找到文件管理器应用", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RECORD_AUDIO_REQUEST) {
            data?.data?.let { uri ->
                validateAndSetReferenceAudio(uri)
            }
        }
    }

    private fun createSeekBarListener(isReference: Boolean): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val player = if (isReference) referenceMediaPlayer else originalMediaPlayer
                    player?.let {
                        val duration = it.duration
                        val newPosition = duration * progress / 100
                        it.seekTo(newPosition)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    private fun openSystemRecorder() {
        try {
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            startActivityForResult(intent, RECORD_AUDIO_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "未找到系统录音应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSetReferenceAudio(uri: Uri) {
        val player = MediaPlayer()
        try {
            player.setDataSource(requireContext(), uri)
            player.prepare()

            val durationInSeconds = player.duration / 1000

            if (durationInSeconds < 3 || durationInSeconds > 10) {
                Toast.makeText(context, "参考音频长度必须在3-10秒之间", Toast.LENGTH_SHORT).show()
                player.release()
                return
            }

            player.release()

            referenceAudioUri = uri
            binding.referencePlayerCardView.visibility = View.VISIBLE
            initializeMediaPlayer(uri, isReference = true)

        } catch (e: Exception) {
            player.release()
            Toast.makeText(context, "音频验证失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeMediaPlayer(uri: Uri, isReference: Boolean) {
        try {
            // 1. 先完全释放旧的 MediaPlayer
            if (isReference) {
                releaseMediaPlayer(referenceMediaPlayer)
                referenceMediaPlayer = null
                isReferencePrepared = false
            } else {
                releaseMediaPlayer(originalMediaPlayer)
                originalMediaPlayer = null
                isOriginalPrepared = false
            }

            // 2. 创建新的 MediaPlayer 实例
            val player = MediaPlayer().apply {
                setOnErrorListener { mp, what, extra ->
                    requireActivity().runOnUiThread {
                        val message = when (what) {
                            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "未知错误"
                            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "服务异常"
                            else -> "播放错误 ($what, $extra)"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        if (isReference) {
                            isReferencePrepared = false
                        } else {
                            isOriginalPrepared = false
                        }
                    }
                    true
                }

                setOnCompletionListener {
                    val button = if (isReference) {
                        binding.referencePlayPauseButton
                    } else {
                        binding.originalPlayPauseButton
                    }
                    button.setImageResource(R.drawable.ic_play_black)
                    handler.removeCallbacksAndMessages(if (isReference) "reference" else "original")
                }
            }

            try {
                player.setDataSource(requireContext(), uri)
                player.setOnPreparedListener { mp ->
                    val duration = mp.duration
                    if (isReference) {
                        binding.referenceDurationTextView.text = formatDuration(duration)
                        binding.referenceSeekBar.progress = 0
                        referenceMediaPlayer = player
                        isReferencePrepared = true
                    } else {
                        binding.originalDurationTextView.text = formatDuration(duration)
                        binding.originalSeekBar.progress = 0
                        originalMediaPlayer = player
                        isOriginalPrepared = true
                    }
                }
                player.prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
                releaseMediaPlayer(player)
                if (isReference) {
                    isReferencePrepared = false
                } else {
                    isOriginalPrepared = false
                }
                Toast.makeText(context, "初始化播放器失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (isReference) {
                isReferencePrepared = false
            } else {
                isOriginalPrepared = false
            }
            Toast.makeText(context, "创建播放器失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // 在 releaseMediaPlayer 中也要更新状态
    private fun releaseMediaPlayer(player: MediaPlayer?) {
        try {
            player?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (player == originalMediaPlayer) {
                originalMediaPlayer = null
                isOriginalPrepared = false
            } else if (player == referenceMediaPlayer) {
                referenceMediaPlayer = null
                isReferencePrepared = false
            }
        }
    }

    private fun togglePlayPause(isReference: Boolean) {
        try {
            // 检查播放器是否准备好
            if (isReference && !isReferencePrepared) {
                Toast.makeText(context, "参考音频播放器正在准备中，请稍候", Toast.LENGTH_SHORT).show()
                return
            }
            if (!isReference && !isOriginalPrepared) {
                Toast.makeText(context, "原始音频播放器正在准备中，请稍候", Toast.LENGTH_SHORT).show()
                return
            }

            val player = if (isReference) referenceMediaPlayer else originalMediaPlayer
            val button = if (isReference) binding.referencePlayPauseButton else binding.originalPlayPauseButton
            val tag = if (isReference) "reference" else "original"

            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    button.setImageResource(R.drawable.ic_play_black)
                    handler.removeCallbacksAndMessages(tag)
                } else {
                    // 停止另一个播放器
                    if (isReference && originalMediaPlayer?.isPlaying == true) {
                        originalMediaPlayer?.pause()
                        binding.originalPlayPauseButton.setImageResource(R.drawable.ic_play_black)
                        handler.removeCallbacksAndMessages("original")
                    } else if (!isReference && referenceMediaPlayer?.isPlaying == true) {
                        referenceMediaPlayer?.pause()
                        binding.referencePlayPauseButton.setImageResource(R.drawable.ic_play_black)
                        handler.removeCallbacksAndMessages("reference")
                    }

                    it.start()
                    button.setImageResource(R.drawable.ic_pause_black)
                    updateSeekBar(isReference)
                }
            }
        } catch (e: Exception) {
            val message = "播放器操作失败: ${e.message}"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateSeekBar(isReference: Boolean) {
        val player = if (isReference) referenceMediaPlayer else originalMediaPlayer
        val seekBar = if (isReference) binding.referenceSeekBar else binding.originalSeekBar
        val button = if (isReference) binding.referencePlayPauseButton else binding.originalPlayPauseButton

        player?.let {
            if (it.isPlaying) {
                val currentPosition = it.currentPosition
                val duration = it.duration
                val progress = (currentPosition.toFloat() / duration * 100).toInt()
                seekBar.progress = progress
                handler.postDelayed({ updateSeekBar(isReference) }, 100)
            } else {
                button.setImageResource(R.drawable.ic_play_black)
            }
        }
    }

    private fun validateAndStartTraining() {
        val trainingText = binding.trainingTextEditText.text.toString().trim()
        val modelName = binding.modelName.text.toString().trim()

        when {
            originalAudioUri == null -> {
                Toast.makeText(context, "请先上传原始音频", Toast.LENGTH_SHORT).show()
            }
            referenceAudioUri == null -> {
                Toast.makeText(context, "请上传参考音频", Toast.LENGTH_SHORT).show()
            }
            trainingText.isEmpty() -> {
                Toast.makeText(context, "请输入训练文本内容", Toast.LENGTH_SHORT).show()
            }
            modelName.isEmpty() -> {
                Toast.makeText(context, "请输入模型名称", Toast.LENGTH_SHORT).show()
            }
            isTraining -> {
                Toast.makeText(context, "训练正在进行中...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                startTraining(modelName, trainingText)
            }
        }
    }

    private fun startTraining(modelName: String, trainingText: String) {
        isTraining = true
        showLoadingDialog()
        updateLoadingProgress("准备开始训练...")

        webSocket.startProcess(
            username = "dcelysia",
            modelName = modelName,
            audioUri = originalAudioUri!!,
            context = requireContext(),
            callback = object : GPTSoVITSWebSocket.ProcessCallback {
                override fun onStepComplete(step: GPTSoVITSWebSocket.ProcessStep, result: String) {
                    requireActivity().runOnUiThread {
                        val message = when (step) {
                            GPTSoVITSWebSocket.ProcessStep.UVR_CONVERT -> "音频转换完成"
                            GPTSoVITSWebSocket.ProcessStep.SLICE -> "音频切片完成"
                            GPTSoVITSWebSocket.ProcessStep.ASR -> "语音识别完成"
                            GPTSoVITSWebSocket.ProcessStep.TRIPLE_PROCESS -> "训练集格式化完成"
                            GPTSoVITSWebSocket.ProcessStep.SOVITS_TRAINING -> "SoVITS训练完成"
                            GPTSoVITSWebSocket.ProcessStep.GPT_TRAINING -> "GPT训练完成"
                        }
                        updateLoadingProgress(message)
                    }
                }

                override fun onError(error: String) {
                    requireActivity().runOnUiThread {
                        hideLoadingDialog()
                        isTraining = false
                        Toast.makeText(context, "训练失败: $error", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onComplete(username: String, modelName: String) {
                    requireActivity().runOnUiThread {
                        updateLoadingProgress("准备上传参考音频...")
                    }

                    // 使用IO线程来处理文件上传，避免UI线程阻塞
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            // 确保引用是安全的
                            val referenceUri = referenceAudioUri ?: run {
                                withContext(Dispatchers.Main) {
                                    hideLoadingDialog()
                                    isTraining = false
                                    Toast.makeText(context, "参考音频文件丢失", Toast.LENGTH_LONG).show()
                                }
                                return@launch
                            }

                        // 使用协程挂起函数包装callback式API
                        val result = suspendCancellableCoroutine<Result<String>> { continuation ->
                            webSocket.uploadReferenceFile(
                                username = username,
                                modelName = modelName,
                                audioUri = referenceUri,
                                context = requireContext()
                            ) { result ->
                                // 回调结果传递给协程
                                continuation.resume(result)
                            }

                            // 如果协程被取消，我们可以在这里添加额外的清理逻辑
                            continuation.invokeOnCancellation {
                                // 可以添加取消上传的逻辑，如果WebSocket API支持的话
                            }
                        }
                            Log.d("Result",result.isSuccess.toString());

                            // 切换回主线程更新UI
                            withContext(Dispatchers.Main) {
                                if (result.isSuccess) {
                                    val referenceWavPath = result.getOrNull()
                                    // 保存到数据库，让 id 自增
                                    val voiceModel = VoiceModelEntity(
                                        voiceItemId = 0, // 或者直接使用默认值
                                        pthModelFile = "GPT_weights_v3/${username}_${modelName}-e15.ckpt",
                                        ckptModelFile = "SoVITS_weights_v3/${username}_${modelName}_e8_s40_l32.pth",
                                        referenceWavPath = referenceWavPath ?: "",
                                        referenceWavText = trainingText
                                    )

                                    // 在后台保存
                                    launch(Dispatchers.IO) {
                                        try {
                                            VoiceModelDataBase.getInstance(requireContext())
                                                .voiceModelDao()
                                                .insert(voiceModel)

                                            withContext(Dispatchers.Main) {
                                                hideLoadingDialog()
                                                isTraining = false
                                                Toast.makeText(context, "训练完成！", Toast.LENGTH_SHORT).show()
                                                findNavController().popBackStack()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                hideLoadingDialog()
                                                isTraining = false
                                                Toast.makeText(
                                                    context,
                                                    "数据库保存失败: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    hideLoadingDialog()
                                    isTraining = false
                                    Toast.makeText(
                                        context,
                                        "参考音频上传失败: ${result.exceptionOrNull()?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                hideLoadingDialog()
                                isTraining = false
                                Toast.makeText(
                                    context,
                                    "上传处理过程出错: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        )
    }


    private var loadingDialog: AlertDialog? = null

    // 修改 showLoadingDialog 方法，添加取消按钮
    private fun showLoadingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val messageText = dialogView.findViewById<TextView>(R.id.messageText)

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)  // 允许取消
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                webSocket.cancel()
                isTraining = false
            }
            .create()

        loadingDialog?.show()
    }
    private fun updateLoadingProgress(message: String) {
        loadingDialog?.findViewById<TextView>(R.id.messageText)?.text = message
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun formatDuration(duration: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex("_display_name")
        cursor?.moveToFirst()
        val name = nameIndex?.let { cursor.getString(it) } ?: "unknown_file.mp3"
        cursor?.close()
        return name
    }

    override fun onPause() {
        super.onPause()
        try {
            if (originalMediaPlayer?.isPlaying == true) {
                originalMediaPlayer?.pause()
            }
            if (referenceMediaPlayer?.isPlaying == true) {
                referenceMediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.originalPlayPauseButton.setImageResource(R.drawable.ic_play_black)
        binding.referencePlayPauseButton.setImageResource(R.drawable.ic_play_black)
        handler.removeCallbacksAndMessages("original")
        handler.removeCallbacksAndMessages("reference")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        webSocket.cancel()
        hideLoadingDialog()

        // 清理播放器资源
        releaseMediaPlayer(originalMediaPlayer)
        releaseMediaPlayer(referenceMediaPlayer)
        originalMediaPlayer = null
        referenceMediaPlayer = null

        // 清理所有回调
        handler.removeCallbacksAndMessages(null)
    }
}