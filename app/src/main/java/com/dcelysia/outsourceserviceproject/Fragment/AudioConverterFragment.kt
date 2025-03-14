package com.dcelysia.outsourceserviceproject.Fragment

import android.Manifest
import com.dcelysia.outsourceserviceproject.R

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dcelysia.outsourceserviceproject.databinding.FragmentAudioConverterBinding
import com.google.android.material.tabs.TabLayout

class AudioConverterFragment : Fragment() {

    private val PICK_AUDIO_REQUEST = 1
    private val RECORD_AUDIO_REQUEST = 2
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentAudioConverterBinding.inflate(layoutInflater)
    }

    private val tabLayout by lazy { binding.tabLayout }
    private val btnBack by lazy { binding.btnBack }
    private val btnUploadAudio by lazy { binding.btnUploadAudio }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnUploadAudio.setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                0 -> openAudioFilePicker()
                1 -> openSystemRecorder()
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateUIForTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun updateUIForTab(position: Int) {
        btnUploadAudio.text = when (position) {
            0 -> getString(R.string.upload_audio_file)
            else -> getString(R.string.record)
        }
    }

    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "audio/mpeg",  // MP3
                    "audio/mp4",   // M4A, AAC
                    "audio/x-wav", // WAV
                    "audio/ogg",   // OGG
                    "audio/aac",   // AAC
                    "audio/x-ms-wma" // WMA
                )
            )
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "选择音频文件"), PICK_AUDIO_REQUEST)
    }

    private fun openSystemRecorder() {
        try {
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            startActivityForResult(intent, RECORD_AUDIO_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "未找到系统录音应用", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_AUDIO_REQUEST, RECORD_AUDIO_REQUEST -> {
                    data?.data?.let { uri ->
                        handleSelectedAudioFile(uri)
                    }
                }
            }
        }
    }

    private fun handleSelectedAudioFile(audioUri: Uri) {
        // 处理选中的音频文件
        findNavController().navigate(
            AudioConverterFragmentDirections.actionAudioConverterFragmentToAudioTrainingFragment(
                audioUri = audioUri.toString()
            )
        )
    }
}