package com.dcelysia.outsourceserviceproject.Fragment

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.FragmentAudioTrainBinding

import java.util.concurrent.TimeUnit

class AudioTrainingFragment : Fragment() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentAudioTrainBinding.inflate(layoutInflater)
    }

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var selectedAudioFile: Uri? = null


    private val back by lazy { binding.btnBack }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = AudioTrainingFragmentArgs.fromBundle(requireArguments())
        if (args.audioUri.isNotEmpty()) {
            val audioUri = Uri.parse(args.audioUri)
            selectedAudioFile = audioUri
            binding.fileNameTextView.text = getFileName(audioUri)
            try {
                initializeMediaPlayer(audioUri)
            } catch (e: Exception) {
                Toast.makeText(context, "音频加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        setupListeners()
    }

    private fun setupListeners() {

        back.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        // SeekBar listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.let {
                        val duration = it.duration
                        val newPosition = duration * progress / 100
                        it.seekTo(newPosition)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Start training button
        binding.startTrainingButton.setOnClickListener {
            val trainingText = binding.trainingTextEditText.text.toString().trim()

            if (trainingText.isEmpty()) {
                Toast.makeText(requireContext(), "请输入训练文本内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAudioFile == null) {
                Toast.makeText(requireContext(), "请先上传音频文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // In a real app, start the training process here
            Toast.makeText(requireContext(), "开始训练...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeMediaPlayer(uri: Uri) {
        // Release existing player if needed
        releaseMediaPlayer()

        // Create new media player
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), uri)
                setOnPreparedListener { mp ->
                    val duration = mp.duration
                    binding.durationTextView.text = formatDuration(duration)
                    binding.seekBar.progress = 0
                    binding.playPauseButton.isEnabled = true
                }
                setOnErrorListener { _, what, extra ->
                    Toast.makeText(context, "播放器错误: $what, $extra", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "初始化播放器失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                it.start()
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                updateSeekBar()
            }
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                val currentPosition = it.currentPosition
                val duration = it.duration
                val progress = (currentPosition.toFloat() / duration * 100).toInt()

                binding.seekBar.progress = progress

                // Update every 100ms
                handler.postDelayed({ updateSeekBar() }, 100)
            }
        }
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

    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        binding.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseMediaPlayer()
        handler.removeCallbacksAndMessages(null)
    }
}