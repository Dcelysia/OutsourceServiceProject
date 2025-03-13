package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import eightbitlab.com.blurview.BlurView
import android.widget.SeekBar
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.FragmentAlbumBinding

class AlbumFragment : Fragment() {
    // ViewBinding
    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    // State variables
    private var isPlaying = false
    private var isLiked = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val decorView = requireActivity().window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurEnabled(true)
            .setBlurRadius(radius)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

//        // Like button
//        binding.btnHeart.setOnClickListener {
//            isLiked = !isLiked
//            updateLikeButton()
//
//            // Add like/unlike logic here
//            // songRepository.setLiked(songId, isLiked)
//        }

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

        // Setup other button listeners
        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            // Navigate back or minimize player
            requireActivity().onBackPressed()
        }

        // Next button
        binding.btnNext.setOnClickListener {
            // Play next song
            playNextSong()
        }

        // Previous button
        binding.btnPrevious.setOnClickListener {
            // Play previous song
            playPreviousSong()
        }

//        // Playlist button
//        binding.btnPlaylist.setOnClickListener {
//            // Show playlist
//            showPlaylist()
//        }
//
//        // Shuffle button
//        binding.btnShuffle.setOnClickListener {
//            // Toggle shuffle mode
//            toggleShuffle()
//        }
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

//        // Update like status
//        isLiked = song.isLiked
//        updateLikeButton()
    }

    private fun updatePlayPauseButton() {
        // Update play/pause button icon
        binding.imgPlayIcon.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

//    private fun updateLikeButton() {
//        // Update heart icon color
//        with(binding.imgHeart) {
//            setImageResource(
//                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
//            )
//            setColorFilter(
//                resources.getColor(
//                    if (isLiked) R.color.red_heart else R.color.white_70
//                )
//            )
//        }
//    }

    private fun updateCurrentTimeFromProgress(progress: Int) {
        // Calculate time from progress percentage
        // This is a simplified example - you'd need to calculate based on duration
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

    private fun showPlaylist() {
        // Logic to show the playlist
    }

    private fun toggleShuffle() {
        // Logic to toggle shuffle mode
    }

    // Song data class - much cleaner in Kotlin with a proper data class
    data class Song(
        val title: String,
        val artist: String,
        val currentLyric: String,
        val duration: String,
        val albumArtResId: Int,
        val isLiked: Boolean
    )
}