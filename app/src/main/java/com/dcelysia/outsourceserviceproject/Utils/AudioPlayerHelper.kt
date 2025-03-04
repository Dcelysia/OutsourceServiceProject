package com.dcelysia.outsourceserviceproject.Utils

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.adapter.RecommendedVoiceAdapter
import java.io.File
import java.util.concurrent.TimeUnit

class AudioPlayerHelper(private val context: Context) {

    private var playbackSpeed: Float = 1.0f

    private var tempProgressBar: ProgressBar? = null
    private var tempTextView: TextView? = null

    private lateinit var voiceAdapter: RecommendedVoiceAdapter
    private val voiceItems = mutableListOf<VoiceItem>()

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressUpdateRunnable: Runnable? = null
    private var currentPlayingPosition: Int = -1

    // 播放状态
    enum class PlaybackState {
        PLAYING, PAUSED, STOPPED
    }

    private var currentState = PlaybackState.STOPPED

    /**
     * 播放音频并更新进度条
     * @param audioFile 音频文件或Uri
     * @param progressBar 进度条
     */
    fun playAudio(audioFile: Any, progressBar: ProgressBar) {
        releaseMediaPlayer()
        tempProgressBar = progressBar
        tempTextView = null

        mediaPlayer = MediaPlayer().apply {
            when (audioFile) {
                is File -> setDataSource(audioFile.absolutePath)
                is Uri -> setDataSource(context, audioFile)
                is String -> setDataSource(audioFile)
                is Int -> {
                    val afd = context.resources.openRawResourceFd(audioFile)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }

                else -> throw IllegalArgumentException("不支持的音频源类型")
            }

            prepare()
            start()

            // 设置播放速度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = PlaybackParams()
                params.speed = playbackSpeed
                playbackParams = params
            }
        }

        currentState = PlaybackState.PLAYING

        // 设置进度条最大值
        progressBar.max = mediaPlayer?.duration ?: 0

        // 更新进度条
        updateProgressBar(progressBar)
    }

    /**
     * 播放音频并更新进度条和时间文本
     * @param audioFile 音频文件或Uri
     * @param progressBar 进度条
     * @param timeTextView 显示时间的TextView
     */
    fun playAudio(audioFile: Any, progressBar: ProgressBar, timeTextView: TextView) {
        releaseMediaPlayer()

        tempProgressBar = progressBar
        tempTextView = timeTextView

        mediaPlayer = MediaPlayer().apply {
            when (audioFile) {
                is File -> setDataSource(audioFile.absolutePath)
                is Uri -> setDataSource(context, audioFile)
                is String -> setDataSource(audioFile)
                is Int -> {
                    val afd = context.resources.openRawResourceFd(audioFile)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }

                else -> throw IllegalArgumentException("不支持的音频源类型")
            }

            prepare()
            start()
            // 设置播放速度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = PlaybackParams()
                params.speed = playbackSpeed
                playbackParams = params
            }
        }

        currentState = PlaybackState.PLAYING

        // 设置进度条最大值
        progressBar.max = mediaPlayer?.duration ?: 0
        updateProgressBarAndTime(progressBar, timeTextView)
    }

    /**
     * 更新进度条
     */
    private fun updateProgressBar(progressBar: ProgressBar) {
        progressUpdateRunnable?.let { handler.removeCallbacks(it) }
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        progressBar.progress = it.currentPosition
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }
        handler.post(progressUpdateRunnable!!)
    }

    /**
     * 更新进度条和时间文本
     */
    private fun updateProgressBarAndTime(progressBar: ProgressBar, timeTextView: TextView) {
        progressUpdateRunnable?.let { handler.removeCallbacks(it) }
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val currentPosition = it.currentPosition
                    val duration = it.duration

                    // 更新进度条
                    progressBar.progress = currentPosition
                    // 更新时间文本 (格式: 当前时间 / 总时间)
                    val currentTimeStr = formatTime(currentPosition)
                    val totalTimeStr = formatTime(duration)
                    timeTextView.text = "$currentTimeStr / $totalTimeStr"

                    // 只有在播放状态下才继续更新
                    if (it.isPlaying) {
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }
        handler.post(progressUpdateRunnable!!)
    }

    /**
     * 格式化时间为 mm:ss 格式
     */
    private fun formatTime(timeMs: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * 暂停播放
     */
    fun pause() {
        if (currentState == PlaybackState.PLAYING && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            currentState = PlaybackState.PAUSED

            // 直接取消文本更新
            progressUpdateRunnable?.let {
                handler.removeCallbacks(it)
            }
        }
    }

    /**
     * 继续播放
     */
    fun resume() {
        if (currentState == PlaybackState.PAUSED) {
            mediaPlayer?.start()
            currentState = PlaybackState.PLAYING

            tempProgressBar?.let { progressBar ->
                tempTextView?.let { textView ->
                    updateProgressBarAndTime(progressBar, textView)
                } ?: updateProgressBar(progressBar)
            }
        }
    }

    /**
     * 停止播放并重置
     */
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        currentState = PlaybackState.STOPPED

        // 移除进度更新回调
        progressUpdateRunnable?.let {
            handler.removeCallbacks(it)
        }

        tempProgressBar?.progress = 0
        tempTextView?.text = "00:00 / 00:00"
    }

    /**
     * 设置播放速度
     * @param speed 播放速度 (0.5f-2.0f)
     */
    fun setPlaybackSpeed(speed: Float) {
        if (speed < 0.5f || speed > 2.0f) {
            throw IllegalArgumentException("播放速度必须在0.5到2.0之间")
        }

        playbackSpeed = speed

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
            val params = PlaybackParams()
            params.speed = speed
            mediaPlayer?.playbackParams = params
        }
    }

    /**
     * 获取当前播放速度
     */
    fun getPlaybackSpeed(): Float = playbackSpeed

    /**
     * 获取当前播放状态
     */
    fun getPlaybackState(): PlaybackState = currentState

    /**
     * 获取音频总时长(毫秒)
     */
    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    /**
     * 获取当前播放位置(毫秒)
     */
    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    /**
     * 跳转到指定位置
     * @param position 位置(毫秒)
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    /**
     * 释放MediaPlayer资源
     */
    fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        currentState = PlaybackState.STOPPED

        // 移除进度更新回调
        progressUpdateRunnable?.let {
            handler.removeCallbacks(it)
        }
        tempProgressBar = null
        tempTextView = null
    }

    /**
     * 设置播放完成监听器
     */
    fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            currentState = PlaybackState.STOPPED
            listener()
        }
    }
}