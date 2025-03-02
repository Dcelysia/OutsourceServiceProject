package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.adapter.RecommendedVoiceAdapter
import com.dcelysia.outsourceserviceproject.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var voiceAdapter: RecommendedVoiceAdapter
    private val voiceItems = mutableListOf<VoiceItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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
            Toast.makeText(context, "AI 智能变声", Toast.LENGTH_SHORT).show()
        }

        // Setup AI Text-to-Speech card
        binding.cardTextToSpeech.setOnClickListener {
            Toast.makeText(context, "AI 智能文本转换", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupPremiumPlan() {
        binding.cardPremiumPlan.setOnClickListener {
            Toast.makeText(context, "Audio AI 高级计划详情", Toast.LENGTH_SHORT).show()
        }

        binding.btnUpgradePlan.setOnClickListener {
            Toast.makeText(context, "升级到高级计划", Toast.LENGTH_SHORT).show()
            // Navigate to premium plan details
            // navigateToPremiumPlan()
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
                title = "AI声音描述",
                description = "声音详细描述文本",
                duration = "0:05",
                avatarResId = R.drawable.notification_bell,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 2,
                title = "AI声音描述",
                description = "声音详细描述文本",
                duration = "0:05",
                avatarResId = R.drawable.notification_bell,
                isPlaying = false
            )
        )
    }

    private fun onVoiceItemPlayPauseClick(position: Int) {
        val voiceItem = voiceItems[position]

        // Toggle play state
        if (voiceItem.isPlaying) {
            // Pause this item
            pauseAudio(position)
        } else {
            // Pause any currently playing item first
            for (i in voiceItems.indices) {
                if (voiceItems[i].isPlaying && i != position) {
                    pauseAudio(i)
                }
            }

            // Play the selected item
            playAudio(position)
        }
    }

    private fun playAudio(position: Int) {
        // Update model
        voiceItems[position].isPlaying = true

        // Update UI
        voiceAdapter.updatePlayState(position)

        // Here you would actually start audio playback
        Toast.makeText(context, "播放: ${voiceItems[position].title}", Toast.LENGTH_SHORT).show()
    }

    private fun pauseAudio(position: Int) {
        // Update model
        voiceItems[position].isPlaying = false

        // Update UI
        voiceAdapter.updatePlayState(position)

        // Here you would actually pause audio playback
        Toast.makeText(context, "暂停: ${voiceItems[position].title}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}