package com.dcelysia.outsourceserviceproject.Fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.adapter.RecommendedVoiceAdapter
import com.dcelysia.outsourceserviceproject.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var voiceAdapter: RecommendedVoiceAdapter
    private val voiceItems = mutableListOf<VoiceItem>()
    private val handler = Handler(Looper.getMainLooper())
    private var progressUpdateRunnable: Runnable? = null
    private var currentPlayingPosition: Int = -1


    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupFeatureCards()
        setupPremiumPlan()
        setupRecommendedVoices()
    }

    private fun setupUserInfo() {
        // Set user name and greeting based on time of day
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            currentHour < 12 -> "早上好"
            currentHour < 18 -> "下午好"
            else -> "晚上好"
        }

        binding.tvGreeting.text = "$greeting Aric"

        // Setup notification icon click listener
        binding.ivNotification.setOnClickListener {
            Toast.makeText(context, "查看通知", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFeatureCards() {
        // Setup AI Voice Changer card
        binding.cardVoiceChanger.setOnClickListener {
            val controller = Navigation.findNavController(it)
            controller.navigate(R.id.action_homeFragment_to_audioConverterFragment)
        }

        // Setup AI Text-to-Speech card
        binding.cardTextToSpeech.setOnClickListener {
            val controller = Navigation.findNavController(it)
            controller.navigate(R.id.action_homeFragment_to_voiceSynthesisFragment)
        }

    }

    private fun setupPremiumPlan() {
        binding.cardPremiumPlan.setOnClickListener {
            Toast.makeText(context, "Audio AI 高级计划详情", Toast.LENGTH_SHORT).show()
        }

        binding.btnUpgradePlan.setOnClickListener {
            Toast.makeText(context, "升级到高级计划", Toast.LENGTH_SHORT).show()
            // Navigate to premium plan details
//             navigateToPremiumPlan()
        }
    }

    private fun setupRecommendedVoices() {
        // Initialize voice items
        initVoiceItems()

        // Setup RecyclerView
        voiceAdapter = RecommendedVoiceAdapter(voiceItems) { position ->
            onVoiceItemPlayPauseClick(position)
        }

        binding.recyclerVoices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voiceAdapter
            setHasFixedSize(true)
        }
    }

    private fun initVoiceItems() {
        // Clear existing items if any
        voiceItems.clear()

        // Add sample voice items
        voiceItems.add(
            VoiceItem(
                id = 1,
                title = "胡桃",
                description = "本堂主就是第七十七代往生堂堂主",
                duration = "0:05",
                avatarResId = R.drawable.hutao,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 2,
                title = "爱莉希雅",
                description = "此后，将有群星闪耀，因为我如今来过。",
                duration = "0:05",
                avatarResId = R.drawable.elysia,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "芙宁娜",
                description = "希望你喜欢这五百年属于你的戏份",
                duration = "0:05",
                avatarResId = R.drawable.funingna,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "派蒙",
                description = "最好的伙伴",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )
    }

    private fun onVoiceItemPlayPauseClick(
        position: Int
    ) {
        if (currentPlayingPosition == position && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.stop()
            mediaPlayer.reset()
            if (currentPlayingPosition != -1) {
                voiceItems[currentPlayingPosition].isPlaying = false
                voiceAdapter.updatePlayState(currentPlayingPosition)
            }
            currentPlayingPosition = position
            voiceItems[currentPlayingPosition].isPlaying = true
            voiceAdapter.updatePlayState(currentPlayingPosition)

            mediaPlayer.setOnCompletionListener {
                voiceItems[currentPlayingPosition].isPlaying = false
                voiceAdapter.updatePlayState(currentPlayingPosition)
            }

            val afd = requireContext().resources.openRawResourceFd(voiceItems[position].wavFile)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        progressUpdateRunnable?.let { handler.removeCallbacks(it) }
    }
}