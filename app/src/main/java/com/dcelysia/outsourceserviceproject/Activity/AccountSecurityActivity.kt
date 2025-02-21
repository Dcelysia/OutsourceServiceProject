package com.dcelysia.outsourceserviceproject.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CustomToast
import com.dcelysia.outsourceserviceproject.ViewModel.AccountSecurityViewModel
import com.dcelysia.outsourceserviceproject.databinding.ActivityAccountSecurityBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AccountSecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSecurityBinding
    private val oldPassword by lazy { binding.accountSecurityOldPassword }
    private val newPassword by lazy { binding.accountSecurityFrPassword }
    private val confirmPassword by lazy { binding.accountSecuritySePassword }
    private val viewModel by lazy { AccountSecurityViewModel() }
    private val overlay by lazy { binding.accountSecurityLoadingOverlay }
    private val submit by lazy { binding.accountSecurityButton }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPasswordEdit()
        observeViewModel()
        submit.setOnClickListener { submit() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.submitResponse.collect { response ->
                        when (response) {
                            is Resource.Success -> {
                                hideOverlay()
                                CustomToast.showMessage(
                                    this@AccountSecurityActivity,
                                    "密码更改成功"
                                )
                                finish()
                            }

                            is Resource.Error -> {
                                hideOverlay()
                                CustomToast.showMessage(
                                    this@AccountSecurityActivity,
                                    response.message
                                )
                            }

                            is Resource.Loading -> {
                                showOverlay()
                            }

                            null -> {
                                hideOverlay()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun submit() {
        viewModel.updatePassword()
    }


    private fun setPasswordEdit() {
        oldPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateOldPassword(s?.toString() ?: "")
            }
        })
        inputFilterSeverity(oldPassword)


        newPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateFirstPassword(s?.toString() ?: "")
            }
        })
        inputFilterSeverity(newPassword)

        confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSecondPassword(s?.toString() ?: "")
            }
        })
        inputFilterSeverity(confirmPassword)
    }

    private fun showOverlay() {
        overlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(200).setListener(null)
        }
    }

    private fun hideOverlay() {
        overlay.animate()
            .alpha(0f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    overlay.visibility = View.GONE
                }
            })
    }

    private fun inputFilterSeverity(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("^[a-zA-Z0-9!@#\$%^&*(),.?\":{}|<>]+$")
            source.toString().filter { char ->
                regex.matches(char.toString())
            }
        }
        editText.filters = arrayOf(inputFilter)
    }

}