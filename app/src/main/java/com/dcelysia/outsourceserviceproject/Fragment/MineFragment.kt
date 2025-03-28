package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dcelysia.outsourceserviceproject.Model.data.response.UserProfile
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import com.dcelysia.outsourceserviceproject.ViewModel.PersonProfileViewModel
import com.dcelysia.outsourceserviceproject.core.Route
import com.dcelysia.outsourceserviceproject.databinding.FragmentMineBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [MineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MineFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var binding: FragmentMineBinding
//    private val mineAvatar by lazy { binding.mineAvatar }
//    private val mineAccount by lazy { binding.mineAccount }
//    private val topMineConfig by lazy { binding.topMineConfig }
//    private val topUserConfig by lazy { binding.topMineConfig }
    private val viewModel by lazy { PersonProfileViewModel() }

//    private val accountSecurity by lazy { binding.accountSecurity }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMineBinding.inflate(layoutInflater, container, false)
        
        // 设置点击事件
        setupClickListeners()
        
        // 观察用户信息
        observeUserInfo()
        
        return binding.root
    }

    private fun setupClickListeners() {
        // 编辑按钮点击事件
        binding.btnEdit.setOnClickListener {
            Route.goPersonProfile(requireActivity())
        }

        // 头像点击事件
        binding.ivProfile.setOnClickListener {
            // TODO: 实现头像选择功能
        }

        // 升级计划按钮点击事件
        binding.btnUpgrade.setOnClickListener {
            // TODO: 跳转到升级计划页面
        }

        // 人脸识别开关事件
        binding.switchFaceRecognition.setOnCheckedChangeListener { _, isChecked ->
            // TODO: 处理人脸识别开关状态
        }

        // 更改密码点击事件
        binding.layoutChangePassword.setOnClickListener {
            // TODO: 跳转到更改密码页面
        }

        // 法律和政策点击事件
        binding.layoutLegalPolicy.setOnClickListener {
            // TODO: 跳转到法律和政策页面
        }

        // 通知设置点击事件
        binding.layoutNotification.setOnClickListener {
            // TODO: 跳转到通知设置页面
        }

        // 语言设置点击事件
        binding.layoutLanguage.setOnClickListener {
            // TODO: 跳转到语言设置页面
        }
    }

    private fun observeUserInfo() {
        lifecycleScope.launch {
            viewModel.userProfile.collectLatest { response ->
                when (response) {
                    is Resource.Success -> {
                        updateUI(response.data)
                    }
                    is Resource.Error -> {
                        // TODO: 显示错误提示
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateUI(userProfile: UserProfile) {
        // 更新头像
        Glide.with(this)
            .load(userProfile.avatarUrl)
            .circleCrop()
            .into(binding.ivProfile)

        // 更新用户名
        binding.tvUserName.text = userProfile.account

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WodeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            MineFragment().apply {

            }
    }
}