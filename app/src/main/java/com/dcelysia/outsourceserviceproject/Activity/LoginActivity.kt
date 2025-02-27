package com.dcelysia.outsourceserviceproject.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dcelysia.outsourceserviceproject.core.Route
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CustomToast
import com.dcelysia.outsourceserviceproject.Utils.mmkv.LoginInfoManager
import com.dcelysia.outsourceserviceproject.ViewModel.LoginViewModel
import com.dcelysia.outsourceserviceproject.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val loginEditAccount by lazy { binding.loginEditAccount }
    private val loginEditPassword by lazy { binding.loginEditPassword }

    private val loginPasswordVisible by lazy { binding.loginPasswordVisible }
    private val loginRememberPassword by lazy { binding.loginRememberPassword }
    private val forgetPassword by lazy { binding.forgetPassword }
    private val register by lazy { binding.register }

    private val login by lazy { binding.loginButton }
    private val loginAgreePlan by lazy { binding.loginAgreePlan }
    private val viewModel: LoginViewModel by viewModels()
    private var isVisible = false

    private val overlay by lazy { binding.loginLoadingOverlay }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setAccountEdit()
        setPasswordEdit()
        setIsAgree()
        observeViewModel()
        setVisiblePassword()
        // 如果从重试获取token三次过来还是不行就弹窗提醒
        if(intent.getBooleanExtra("from_token_expired", false)) {

            CustomToast.showMessage(this@LoginActivity,"用户登陆超时，请重新登陆")
        }
        val registerAccountInfo = intent.getStringExtra("register_account")
        if (registerAccountInfo != null && registerAccountInfo.isNotEmpty()) {
            loginEditAccount.setText(registerAccountInfo)
            loginEditPassword.setText(intent.getStringExtra("register_password"))
        } else if (LoginInfoManager.cacheRememberFlag) {
            loginEditAccount.setText(LoginInfoManager.cacheUsername)
            loginEditPassword.setText(LoginInfoManager.cachePassword)
            loginRememberPassword.isChecked = true
        }

        login.setOnClickListener {
            login()
        }
        register.setOnClickListener {
            Route.goRegister(this@LoginActivity)
        }
    }

    private fun login() {
        val rememberFlag = loginRememberPassword.isChecked
        val account = loginEditAccount.text.toString()
        val password = loginEditPassword.text.toString()
        LoginInfoManager.cacheRememberFlag = rememberFlag

        if (rememberFlag) {
            LoginInfoManager.cacheUsername = account
            LoginInfoManager.cachePassword = password
        }
        viewModel.login()
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

    private fun setVisiblePassword() {
        loginPasswordVisible.setOnClickListener {
            isVisible = !isVisible
            viewModel.updatePasswordVisible(isVisible)
        }
    }

    private fun setAccountEdit() {
        loginEditAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateAccount(s?.toString() ?: "")
            }
        })
        inputFilterSimple(loginEditAccount)
    }

    private fun setPasswordEdit() {
        loginEditPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updatePassword(s?.toString() ?: "")
            }
        })
        inputFilterSeverity(loginEditPassword)
    }

    private fun setIsAgree() {
        loginAgreePlan.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateIsAgree(isChecked)
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.isLoginEnabled
                        .collect { isEnabled ->
                            login.setBackgroundResource(
                                if (isEnabled) R.drawable.gradient_enable_button
                                else R.drawable.gradient_disable_button
                            )
                            login.isEnabled = isEnabled
                        }
                }

                launch {
                    viewModel.isPasswordVisible.collect { isVisible ->
                        if (isVisible) {
                            loginEditPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            loginPasswordVisible.setImageResource(R.drawable.zhengyan)
                        } else {
                            loginEditPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            loginPasswordVisible.setImageResource(R.drawable.biyan)
                        }
                        loginEditPassword.setSelection(loginEditPassword.text.length)
                    }
                }

                launch {
                    viewModel.loginState.collect { state ->
                        when (state) {
                            is Resource.Error -> {
                                CustomToast.showMessage(this@LoginActivity, state.message)
                                hideOverlay()
                            }
                            is Resource.Loading -> {
                                showOverlay()
                            }
                            is Resource.Success -> {
                                CustomToast.showMessage(this@LoginActivity, "登陆成功！")
                                hideOverlay()
                                Route.goHome(this@LoginActivity)
                                finish()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
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


    private fun inputFilterSimple(editText: EditText) {
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("^[a-zA-Z0-9]*$")
            if (regex.matches(source)) source else ""
        }
        editText.filters = arrayOf(inputFilter)
    }
}