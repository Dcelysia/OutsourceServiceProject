package com.dcelysia.outsourceserviceproject.Fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.FragmentVoiceSynthesisBinding
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class VoiceSynthesisFragment : Fragment() {

    private lateinit var binding: FragmentVoiceSynthesisBinding

    private val voiceModels = arrayOf(
        "女声 - 自然温柔",
        "女声 - 清晰明亮",
        "男声 - 沉稳有力",
        "男声 - 亲切自然",
        "儿童声音"
    )

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
    private val updateProgressAction = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 100)
        }
    }

    private val maxDuration = 90000L

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
            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.synthesizeButton.isEnabled = false

            // Simulate voice synthesis (in a real app, this would call an API)
            synthesizeVoice(text)
        }
    }

    private fun setupSettingsButton() {
        binding.settingsButton.setOnClickListener {
            Toast.makeText(requireContext(), "设置功能暂未实现", Toast.LENGTH_SHORT).show()
        }
    }

    private fun synthesizeVoice(text: String) {
        // In a real app, you would call your voice synthesis API here
        // For this example, we'll simulate a delay and then play a sample audio

        Handler(Looper.getMainLooper()).postDelayed({
            // Hide loading
            binding.loadingProgressBar.visibility = View.GONE
            binding.synthesizeButton.isEnabled = true

            // In a real app, you would play the synthesized audio
            // For this example, we'll just simulate a successful synthesis
            setupMediaPlayer()
            startPlayback()

        }, 2000) // Simulate 2 second delay for synthesis
    }

    private fun setupMediaPlayer() {
        // Clean up any existing MediaPlayer
        releaseMediaPlayer()

        // In a real app, you would use the synthesized audio file
        // For this example, we'll simulate with a placeholder
        mediaPlayer = MediaPlayer().apply {
            try {
                // This would be replaced with the actual synthesized audio file
                // setDataSource(context, Uri.parse("your_audio_file_uri"))
                // prepare()

                // For this example, we'll just set a duration without actual audio
                // This allows us to simulate playback
                setOnPreparedListener {
                    // The progress updating will be handled by our handler
                }

                setOnCompletionListener {
                    stopPlayback()
                }
            } catch (e: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Failed to prepare media player",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startPlayback() {
        if (mediaPlayer == null) {
            setupMediaPlayer()
        }

        // In a real app, this would actually play audio
        // mediaPlayer?.start()

        isPlaying = true
        binding.playButton.setIconResource(R.drawable.ic_pause)

        // Start progress updates
        handler.removeCallbacks(updateProgressAction)
        handler.post(updateProgressAction)
    }

    private fun pausePlayback() {
        // mediaPlayer?.pause()
        isPlaying = false
        binding.playButton.setIconResource(R.drawable.ic_play)

        // Stop progress updates
        handler.removeCallbacks(updateProgressAction)
    }

    private fun stopPlayback() {
        // mediaPlayer?.stop()
        // mediaPlayer?.seekTo(0)
        isPlaying = false
        binding.playButton.setIconResource(R.drawable.ic_play)

        // Reset progress
        binding.progressIndicator.progress = 0
        binding.timeTextView.text = "00:00 / 01:30"

        // Stop progress updates
        handler.removeCallbacks(updateProgressAction)
    }

    private fun updateProgressBar() {
        if (!isPlaying) return

        // In a real app, this would be the actual position of the MediaPlayer
        // val currentPosition = mediaPlayer?.currentPosition ?: 0

        // For this example, we'll simulate progress based on time
        val elapsedTime = System.currentTimeMillis() % maxDuration
        val progress = (elapsedTime * 100 / maxDuration).toInt()

        binding.progressIndicator.progress = progress

        // Update time text
        binding.timeTextView.text = String.format(
            "%s / %s",
            formatTime(elapsedTime),
            formatTime(maxDuration)
        )

        // If we reach the end, stop playback
        if (elapsedTime >= maxDuration) {
            stopPlayback()
        }
    }

    private fun formatTime(timeMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressAction)
        releaseMediaPlayer()
    }
}