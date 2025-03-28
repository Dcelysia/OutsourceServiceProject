package com.dcelysia.outsourceserviceproject.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CustomToast
import com.dcelysia.outsourceserviceproject.ViewModel.RegisterViewModel
import com.dcelysia.outsourceserviceproject.core.Route
import com.dcelysia.outsourceserviceproject.databinding.ActivityRegisterBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    private val registerEditAccount: EditText by lazy { binding.registerEditAccount }
    private val registerEditPassword1: EditText by lazy { binding.registerEditPassword1 }
    private val registerEditPassword2: EditText by lazy { binding.registerEditPassword2 }
    private val registerPassword1Visible: ImageView by lazy { binding.registerPassword1Visible }
    private val registerPassword2Visible: ImageView by lazy { binding.registerPassword2Visible }
    private val register by lazy { binding.registerButton }
    private val registerBack by lazy { binding.registerBackLogin }
    private val overlay by lazy { binding.registerLoadingOverlay }

    private var isVisible1 = false
    private var isVisible2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setAccount()
        setPasswordEdit()
        setVisiblePassword()
        observeViewModel()

        registerBack.setOnClickListener {
            Route.goLoginForcibly(this@RegisterActivity)
        }
        register.setOnClickListener { register() }
    }

    private fun register() {
        val password1 = registerEditPassword1.text.toString()
        val password2 = registerEditPassword2.text.toString()
        
        if (password1 != password2) {
            CustomToast.showMessage(this, "两次输入的密码不一致")
            return
        }
        
        if (password1.length < 6) {
            CustomToast.showMessage(this, "密码长度不能少于6位")
            return
        }
        
        viewModel.register()
    }

    private fun setVisiblePassword() {
        // 第一个密码框的显示/隐藏
        registerPassword1Visible.setOnClickListener {
            isVisible1 = !isVisible1
            updatePasswordVisibility(registerEditPassword1, registerPassword1Visible, isVisible1)
        }

        // 第二个密码框的显示/隐藏
        registerPassword2Visible.setOnClickListener {
            isVisible2 = !isVisible2
            updatePasswordVisibility(registerEditPassword2, registerPassword2Visible, isVisible2)
        }
    }

    private fun updatePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.zhengyan)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.biyan)
        }
        editText.setSelection(editText.text.length)
    }

    private fun showOverlay() {
        overlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(200).setListener(null)
        }
    }

    private fun hideOverlay() {
        overlay.animate().alpha(0f).setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    overlay.visibility = View.GONE
                }
            })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isRegisterEnabled.collect() { isEnabled ->
                        register.setBackgroundResource(
                            if (isEnabled) R.drawable.gradient_enable_button
                            else R.drawable.gradient_disable_button
                        )
                        register.isEnabled = isEnabled
                    }
                }

                launch {
                    viewModel.registerState.collect { state ->
                        Log.d(TAG, "当前state：${state}")
                        when (state) {
                            is Resource.Error -> {
                                Log.d("TAG", "进入Resource.Error")
                                CustomToast.showMessage(this@RegisterActivity, "注册失败：${state.message}")
                                hideOverlay()
                            }
                            is Resource.Loading -> {
                                Log.d("TAG", "进入Resource.Loading")
                                showOverlay()
                            }
                            is Resource.Success -> {
                                Log.d("TAG", "进入Resource.Success")
                                hideOverlay()
                                CustomToast.showMessage(this@RegisterActivity, "注册成功")
                                Route.goLoginFromRegister(
                                    this@RegisterActivity,
                                    registerEditAccount.text.toString(),
                                    registerEditPassword1.text.toString()
                                )
                            }
                            null -> {
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setAccount() {
        registerEditAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateAccount(s?.toString() ?: "")
            }
        })
        inputFilterSimple(registerEditAccount)
    }

    private fun setPasswordEdit() {
        // 第一个密码框的监听
        registerEditPassword1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updatePassword1(s?.toString() ?: "")
                // 当第一个密码改变时，检查两个密码是否一致
                checkPasswordsMatch()
            }
        })
        inputFilterSeverity(registerEditPassword1)

        // 第二个密码框的监听
        registerEditPassword2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updatePassword2(s?.toString() ?: "")
                // 当第二个密码改变时，检查两个密码是否一致
                checkPasswordsMatch()
            }
        })
        inputFilterSeverity(registerEditPassword2)
    }

    private fun checkPasswordsMatch() {
        val password1 = registerEditPassword1.text.toString()
        val password2 = registerEditPassword2.text.toString()
        
        if (password1.isNotEmpty() && password2.isNotEmpty()) {
            if (password1 != password2) {
                registerEditPassword2.error = "两次输入的密码不一致"
            } else {
                registerEditPassword2.error = null
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