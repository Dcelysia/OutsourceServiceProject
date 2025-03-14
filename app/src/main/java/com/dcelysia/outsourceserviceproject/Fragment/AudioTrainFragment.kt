package com.dcelysia.outsourceserviceproject.Fragment

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.Utils.GPTSoVITSWebSocket
import com.dcelysia.outsourceserviceproject.databinding.FragmentAudioTrainBinding
import java.util.concurrent.TimeUnit

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
            val audioUri = Uri.parse(args.audioUri)
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
            openSystemRecorder()
        }

        // 开始训练按钮
        binding.startTrainingButton.setOnClickListener {
            validateAndStartTraining()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RECORD_AUDIO_REQUEST) {
            data?.data?.let { uri ->
                validateAndSetReferenceAudio(uri)
            }
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
        val player = MediaPlayer()
        try {
            player.setDataSource(requireContext(), uri)
            player.setOnPreparedListener { mp ->
                val duration = mp.duration
                if (isReference) {
                    binding.referenceDurationTextView.text = formatDuration(duration)
                    binding.referenceSeekBar.progress = 0
                    referenceMediaPlayer = player
                } else {
                    binding.originalDurationTextView.text = formatDuration(duration)
                    binding.originalSeekBar.progress = 0
                    originalMediaPlayer = player
                }
            }
            player.setOnCompletionListener {
                val button = if (isReference) {
                    binding.referencePlayPauseButton
                } else {
                    binding.originalPlayPauseButton
                }
                button.setImageResource(R.drawable.ic_play_black)
            }
            player.setOnErrorListener { _, what, extra ->
                Toast.makeText(context, "播放器错误: $what, $extra", Toast.LENGTH_SHORT).show()
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            player.release()
            Toast.makeText(context, "初始化播放器失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayPause(isReference: Boolean) {
        val player = if (isReference) referenceMediaPlayer else originalMediaPlayer
        val button =
            if (isReference) binding.referencePlayPauseButton else binding.originalPlayPauseButton

        player?.let {
            if (it.isPlaying) {
                it.pause()
                button.setImageResource(R.drawable.ic_play_black)
            } else {
                // 停止另一个播放器
                if (isReference) {
                    originalMediaPlayer?.pause()
                    binding.originalPlayPauseButton.setImageResource(R.drawable.ic_play_black)
                } else {
                    referenceMediaPlayer?.pause()
                    binding.referencePlayPauseButton.setImageResource(R.drawable.ic_play_black)
                }

                it.start()
                button.setImageResource(R.drawable.ic_pause_black)
                updateSeekBar(isReference)
            }
        }
    }

    private fun updateSeekBar(isReference: Boolean) {
        val player = if (isReference) referenceMediaPlayer else originalMediaPlayer
        val seekBar = if (isReference) binding.referenceSeekBar else binding.originalSeekBar
        val button =
            if (isReference) binding.referencePlayPauseButton else binding.originalPlayPauseButton

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

        webSocket.startProcess(
            serverUrl = "124.71.12.148:8000",  // 替换为实际的服务器地址
            username = "dcelysia",
            modelName = modelName,
            originalAudioUri = originalAudioUri!!,
            referenceAudioUri = referenceAudioUri!!,
            trainingText = trainingText,
            context = requireContext(),
            callback = object : GPTSoVITSWebSocket.ProcessCallback {
                override fun onFileUploadProgress(progress: Float) {
                    requireActivity().runOnUiThread {
                        updateLoadingProgress("文件上传中: ${progress.toInt()}%")
                    }
                }

                override fun onStepStart(step: GPTSoVITSWebSocket.ProcessStep) {
                    requireActivity().runOnUiThread {
                        val stepName = when (step) {
                            GPTSoVITSWebSocket.ProcessStep.UVR_CONVERT -> "音频转换"
                            GPTSoVITSWebSocket.ProcessStep.SLICE -> "音频切片"
                            GPTSoVITSWebSocket.ProcessStep.ASR -> "语音识别"
                            GPTSoVITSWebSocket.ProcessStep.TRIPLE_PROCESS -> "训练集格式化"
                            GPTSoVITSWebSocket.ProcessStep.SOVITS_TRAINING -> "SoVITS训练"
                            GPTSoVITSWebSocket.ProcessStep.GPT_TRAINING -> "GPT训练"
                        }
                        updateLoadingProgress("正在进行$stepName...")
                    }
                }

                override fun onStepComplete(step: GPTSoVITSWebSocket.ProcessStep, result: String) {
                    requireActivity().runOnUiThread {
                        val stepName = when (step) {
                            GPTSoVITSWebSocket.ProcessStep.UVR_CONVERT -> "音频转换"
                            GPTSoVITSWebSocket.ProcessStep.SLICE -> "音频切片"
                            GPTSoVITSWebSocket.ProcessStep.ASR -> "语音识别"
                            GPTSoVITSWebSocket.ProcessStep.TRIPLE_PROCESS -> "训练集格式化"
                            GPTSoVITSWebSocket.ProcessStep.SOVITS_TRAINING -> "SoVITS训练"
                            GPTSoVITSWebSocket.ProcessStep.GPT_TRAINING -> "GPT训练"
                        }
                        updateLoadingProgress("$stepName 完成")
                    }
                }

                override fun onError(error: String) {
                    requireActivity().runOnUiThread {
                        hideLoadingDialog()
                        isTraining = false
                        Toast.makeText(context, "训练失败: $error", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onComplete() {
                    requireActivity().runOnUiThread {
                        hideLoadingDialog()
                        isTraining = false
                        Toast.makeText(context, "训练完成！", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private var loadingDialog: AlertDialog? = null

    private fun showLoadingDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val messageText = dialogView.findViewById<TextView>(R.id.messageText)

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
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

    private fun releaseMediaPlayers() {
        originalMediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        originalMediaPlayer = null

        referenceMediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        referenceMediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        originalMediaPlayer?.pause()
        referenceMediaPlayer?.pause()
        binding.originalPlayPauseButton.setImageResource(R.drawable.ic_play_black)
        binding.referencePlayPauseButton.setImageResource(R.drawable.ic_play_black)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket.cancel()
        hideLoadingDialog()
        releaseMediaPlayers()
        handler.removeCallbacksAndMessages(null)
    }
}