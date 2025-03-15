package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dcelysia.outsourceserviceproject.ViewModel.MineViewModel
import com.dcelysia.outsourceserviceproject.databinding.FragmentMineBinding

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
    private val viewModel by lazy { MineViewModel() }

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
//        topUserConfig.setOnClickListener {
//            Route.goPersonProfile(requireContext())
//        }
//        mineAvatar.setOnClickListener { Route.goPersonProfile(requireContext()) }
//        accountSecurity.setOnClickListener { Route.goAccountSecurity(requireContext()) }
//        observeUserInfo()
        return binding.root
    }

//    private fun observeUserInfo() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.baseUserProfile.collect { response ->
//                        when (response) {
//                            is Resource.Success -> {
//                                val baseUserProfile = response.data
//                                mineAccount.text = baseUserProfile.account
//                                Glide.with(this@MineFragment)
//                                    .load(baseUserProfile.avatarUrl)
//                                    .into(mineAvatar)
//                            }
//
//                            is Resource.Error -> {
//                                CustomToast.showMessage(
//                                    requireContext(),
//                                    "出错啦, ${response.message} + ${MainApplication.token}"
//                                )
//                            }
//
//                            else -> {}
//                        }
//                    }
//                }
//            }
//        }
//    }

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