package com.dcelysia.outsourceserviceproject.Fragment

import com.dcelysia.outsourceserviceproject.R

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout

class AudioConverterFragment : Fragment() {

    private val PICK_AUDIO_REQUEST = 1
    private lateinit var tabLayout: TabLayout
    private lateinit var btnUploadAudio: TextView
    private lateinit var btnBack: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_audio_converter, container, false)

        tabLayout = view.findViewById(R.id.tab_layout)
        btnUploadAudio = view.findViewById(R.id.btn_upload_audio)
        btnBack = view.findViewById(R.id.btn_back)

        setupListeners()

        return view
    }

    private fun setupListeners() {
        // 返回按钮点击
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 上传按钮点击
        btnUploadAudio.setOnClickListener {
            openAudioFilePicker()
        }

        // 标签选择
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateUIForTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // 不需要处理
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 不需要处理
            }
        })
    }

    private fun updateUIForTab(position: Int) {
        btnUploadAudio.text = if (position == 0) {
            getString(R.string.upload_audio_file) // 上传文件标签
        } else {
            getString(R.string.record) // 录音标签
        }
    }

    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "选择音频文件"), PICK_AUDIO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { selectedAudioUri ->
                // 处理选中的音频文件
                handleSelectedAudioFile(selectedAudioUri)
            }
        }
    }

    private fun handleSelectedAudioFile(audioUri: Uri) {
        // 在真实应用中，您会在这里处理音频文件
        // 在这个示例中，我们只显示一个 Toast
        Toast.makeText(context, "选中的音频文件: ${audioUri.lastPathSegment}", Toast.LENGTH_SHORT)
            .show()

        // 导航到处理屏幕或开始转换
        // 这将在真实应用中实现
    }
}