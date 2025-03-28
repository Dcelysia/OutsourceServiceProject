package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.Model.Room.database.VoiceModelDataBase
import com.dcelysia.outsourceserviceproject.Model.Room.database.VoiceItemDatabase
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceModelEntity
import com.dcelysia.outsourceserviceproject.Model.Room.entity.VoiceItemEntity
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.FragmentModelsBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelsFragment : Fragment() {
    private lateinit var binding: FragmentModelsBinding

    private val voiceItems = mutableListOf<Any>()
    private val recyclerView by lazy { binding.modelsRecycler }
    private val page by lazy { binding.page }

    private val voiceModelDatabase by lazy { VoiceModelDataBase.getInstance(requireContext()) }
    private val voiceItemDatabase by lazy { VoiceItemDatabase.getInstance(requireContext()) }

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
        loadDataFromDatabase()
        setRecyclerView()
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadDataFromDatabase() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // 获取所有语音项目
                val voiceItems = voiceItemDatabase.viewItemDao().getAllModels()
                
                // 清空现有数据
                this@ModelsFragment.voiceItems.clear()
                
                // 添加"推荐"标题
                this@ModelsFragment.voiceItems.add("推荐")
                
                // 添加推荐语音项目
                voiceItems.forEach { voiceItem ->
                    this@ModelsFragment.voiceItems.add(
                        VoiceItem(
                            id = voiceItem.id,
                            title = voiceItem.title,
                            description = voiceItem.description,
                            duration = "0:05", // 这个值可能需要从其他地方获取
                            avatarResId = voiceItem.avatarResId,
                            wavFile = voiceItem.wavFile,
                            isPlaying = false
                        )
                    )
                }
                
                // 添加"我的"标题
                this@ModelsFragment.voiceItems.add("我的")
                
                // 获取用户自定义的语音模型
                val userModels = voiceModelDatabase.voiceModelDao().getAllModels()
                userModels.forEach { model ->
                    // 找到对应的语音项目
                    val associatedVoiceItem = voiceItems.find { it.id == model.voiceItemId }
                    if (associatedVoiceItem != null) {
                        this@ModelsFragment.voiceItems.add(
                            VoiceItem(
                                id = associatedVoiceItem.id,
                                title = associatedVoiceItem.title,
                                description = associatedVoiceItem.description,
                                duration = "0:05", // 这个值可能需要从其他地方获取
                                avatarResId = associatedVoiceItem.avatarResId,
                                wavFile = associatedVoiceItem.wavFile,
                                isPlaying = false
                            )
                        )
                    }
                }
            }
        }
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
            loadDataFromDatabase()
            postDelayed({ finishRefresh() }, 400)
        }.autoRefresh()

        page.onLoadMore {
            postDelayed({ finishLoadMore() }, 400)
        }
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