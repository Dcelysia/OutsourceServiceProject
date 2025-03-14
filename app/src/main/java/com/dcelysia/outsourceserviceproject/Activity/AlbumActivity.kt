package com.dcelysia.outsourceserviceproject.Activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import eightbitlab.com.blurview.BlurView
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.ActivityAlbumBinding


class AlbumActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityAlbumBinding

    // State variables
    private var isPlaying = false
    private var isLiked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置毛玻璃效果
        setupBlurEffect()

        // Setup click listeners
        setupListeners()

        // Load initial song data
        loadSongData()
    }

    private fun setupBlurEffect() {
        // 为顶部横幅设置毛玻璃效果
        setupBlurView(binding.blurView, 16f)

        // 为底部控制区域设置毛玻璃效果
        setupBlurView(binding.blurViewBottom, 20f)
    }

    private fun setupBlurView(blurView: BlurView, radius: Float) {
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurEnabled(true)
            .setBlurRadius(radius)
    }

    private fun setupListeners() {
        // Play/Pause button
        binding.imgPlayIcon.setOnClickListener {
            isPlaying = !isPlaying
            updatePlayPauseButton()

            if (isPlaying) {
                // Start playback logic here
                // mediaPlayer.start()
            } else {
                // Pause playback logic here
                // mediaPlayer.pause()
            }
        }

        // SeekBar change listener
        binding.seekbarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Update current time based on progress
                    updateCurrentTimeFromProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // You can pause updates here if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Seek to the position in the media player
                seekToPosition(seekBar.progress)
            }
        })

        // Navigation listeners
        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finishWithSlideDownAnimation()
        }

        // Next button
        binding.btnNext.setOnClickListener {
            playNextSong()
        }

        // Previous button
        binding.btnPrevious.setOnClickListener {
            playPreviousSong()
        }
    }

    private fun finishWithSlideDownAnimation() {
        finish()
        // Apply custom slide down animation
        overridePendingTransition(0, R.anim.slide_down_exit)
    }

    // Override onBackPressed to also apply our custom animation
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.slide_down_exit)
    }

    private fun loadSongData() {
        // In a real app, you would load this data from a repository or service
        val currentSong = Song(
            title = "背对背拥抱",
            artist = "林俊杰",
            currentLyric = "我们背对背拥抱",
            duration = "03:54",
            albumArtResId = R.drawable.album_time, // Use your actual resource
            isLiked = true
        )

        // Update UI with song data
        updateSongUI(currentSong)
    }

    private fun updateSongUI(song: Song) {
        with(binding) {
            txtSongTitle.text = song.title
            txtArtist.text = song.artist
            txtLyrics.text = song.currentLyric
            txtTotalTime.text = song.duration
            imgAlbum.setImageResource(song.albumArtResId)
        }
    }

    private fun updatePlayPauseButton() {
        // Update play/pause button icon
        binding.imgPlayIcon.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun updateCurrentTimeFromProgress(progress: Int) {
        // Calculate time from progress percentage
        val totalSeconds = 234 // 03:54 in seconds
        val currentSeconds = (progress * totalSeconds) / 100

        val timeString = formatTime(currentSeconds)
        binding.txtCurrentTime.text = timeString
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun seekToPosition(progress: Int) {
        // In a real app, you would seek the media player to this position
        // mediaPlayer.seekTo((progress * mediaPlayer.getDuration()) / 100)
    }

    private fun playNextSong() {
        // Logic to play the next song
    }

    private fun playPreviousSong() {
        // Logic to play the previous song
    }

    // Song data class
    data class Song(
        val title: String,
        val artist: String,
        val currentLyric: String,
        val duration: String,
        val albumArtResId: Int,
        val isLiked: Boolean
    )
}