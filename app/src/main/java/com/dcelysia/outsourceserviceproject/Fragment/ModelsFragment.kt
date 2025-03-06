package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.adapter.ModelsAdapter
import com.dcelysia.outsourceserviceproject.databinding.FragmentHomeBinding
import com.dcelysia.outsourceserviceproject.databinding.FragmentModelsBinding

class ModelsFragment : Fragment() {
    private lateinit var binding: FragmentModelsBinding

    private val voiceItems = mutableListOf<VoiceItem>()
    private val recyclerView by lazy { binding.modelsRecycler }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentModelsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVoiceItems()
        setRecyclerView()
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

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "柏神",
                description = "宁启睿的主人",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "柏神",
                description = "宁启睿的主人",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "柏神",
                description = "宁启睿的主人",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "柏神",
                description = "宁启睿的主人",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )

        voiceItems.add(
            VoiceItem(
                id = 3,
                title = "柏神",
                description = "宁启睿的主人",
                duration = "0:05",
                avatarResId = R.drawable.paimeng,
                wavFile = R.raw.test,
                isPlaying = false
            )
        )
    }

    private fun setRecyclerView() {
        recyclerView.adapter = ModelsAdapter(voiceItems) { msg ->
            findNavController().navigate(
                ModelsFragmentDirections.actionModelsFragmentToVoiceSynthesisFragment(
                    modelName = msg
                )
            )
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ModelsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}