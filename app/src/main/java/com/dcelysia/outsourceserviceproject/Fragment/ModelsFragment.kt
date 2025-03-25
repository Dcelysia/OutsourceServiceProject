package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dcelysia.outsourceserviceproject.Model.Room.database.VoiceModelDataBase
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.adapter.ModelsAdapter
import com.dcelysia.outsourceserviceproject.databinding.FragmentModelsBinding
import com.dcelysia.outsourceserviceproject.databinding.ItemRecommendedVoiceBinding
import com.dcelysia.outsourceserviceproject.databinding.RecyclerTextTitleBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setDifferModels
import com.drake.brv.utils.setup
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelsFragment : Fragment() {
    private lateinit var binding: FragmentModelsBinding

    private val voiceItems = mutableListOf<Any>()
    private val recyclerView by lazy { binding.modelsRecycler }
    private val page by lazy { binding.page }
    private val mmkv by lazy { MMKV.mmkvWithID("models_fragment") }
    private val firstInit = "is_first_init"

    private val database by lazy { VoiceModelDataBase.getInstance(requireContext()) }

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
//        if (!mmkv.getBoolean(firstInit, false)) {
            lifecycleScope.launch {
                launch(Dispatchers.IO) {
                    database.voiceModelDao().update(
                        VoiceModelEntity(
                            id = 1,
                            voiceItemId = 1,
                            pthModelFile = "SoVITS_weights_v3/hutao_e2_s456_l32.pth",
                            ckptModelFile = "GPT_weights_v3/hutao-e15.ckpt",
                            referenceWavPath = "/home/top/hdd/qs/TEMP/official/hutao_reference.wav",
                            referenceWavText = "我说白术，你不会看不出来吧？难不成你师父，忘了教你这门功夫？"
                        )
                    )
                }
//            }
//            mmkv.putBoolean(firstInit, true)
        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initVoiceItems() {
        // Clear existing items if any
        voiceItems.clear()

        voiceItems.add("推荐")
        // Add sample voice items
        voiceItems.add(
            VoiceItem(
                id = 1,
                title = "胡桃",
                description = "本堂主就是第七十七代往生堂堂主",
                duration = "0:05",
                avatarResId = R.drawable.hutao,
                wavFile = R.raw.hutao_1,
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

        voiceItems.add("我的")

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
        recyclerView.linear().setup {
            addType<VoiceItem>(R.layout.item_recommended_voice)
            addType<String>(R.layout.recycler_text_title)

            onBind {
                when (val item = getModel<Any>()) {
                    is String -> {
                        findView<TextView>(R.id.text_title).text = item
                    }

                    is VoiceItem -> {
                        // 绑定模型视图
                        findView<ImageView>(R.id.iv_avatar).setImageResource(item.avatarResId)
                        findView<TextView>(R.id.tv_title).text = item.title
                        findView<TextView>(R.id.tv_description).text = item.description
                        findView<TextView>(R.id.tv_duration).text = item.duration

                        // 更新播放/暂停按钮状态
                        val btnPlayPause = findView<ImageView>(R.id.btn_play_pause)
                        val playImageView = findView<ImageView>(R.id.view_audio_progress)

                        if (item.isPlaying) {
                            btnPlayPause.setImageResource(R.drawable.pause)
                            btnPlayPause.setBackgroundResource(R.drawable.pause)
                            playImageView.setImageResource(R.drawable.yinbo_blue)
                        } else {
                            btnPlayPause.setImageResource(R.drawable.play)
                            btnPlayPause.setBackgroundResource(R.drawable.play)
                            playImageView.setImageResource(R.drawable.yinbo_grey)
                        }

                        // 设置点击事件
                        findView<CardView>(R.id.all_model).setOnClickListener {
                            findNavController().navigate(
                                ModelsFragmentDirections.actionModelsFragmentToVoiceSynthesisFragment(
                                    modelId = item.id,
                                    modelName = item.title
                                )
                            )
                        }
                    }
                }
            }
            models = voiceItems
        }
        page.onRefresh {
            postDelayed({ finishRefresh() }, 800)
        }.autoRefresh()

        page.onLoadMore {
            postDelayed({ finishLoadMore() }, 800)
        }

//        recyclerView.adapter = ModelsAdapter(voiceItems) { msg ->
//            findNavController().navigate(
//                ModelsFragmentDirections.actionModelsFragmentToVoiceSynthesisFragment(
//                    modelName = msg
//                )
//            )
//        }
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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