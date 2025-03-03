package com.dcelysia.outsourceserviceproject.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CodeEditText
import com.dcelysia.outsourceserviceproject.databinding.ActivityVerificationCodeBinding

class VerificationCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationCodeBinding
    private val codeEditText: CodeEditText by lazy { binding.etVerificationCode }
    private val btnContinue: TextView by lazy { binding.btnContinue }
    private val btnBack: ImageButton by lazy { binding.btnBack }
    private val tvResendCode: TextView by lazy { binding.tvResendCode }
    private val tvEmail: TextView by lazy { binding.tvEmail }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 获取传递过来的邮箱地址
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        // 设置邮箱地址
        tvEmail.text = email
        // 设置监听器
        setupListeners()
    }

    private fun setupListeners() {
        // 设置验证码输入完成监听
        codeEditText.setOnTextFinishListener(object : CodeEditText.OnTextFinishListener {
            override fun onTextFinish(text: CharSequence, length: Int) {
                verifyCode(text.toString())
            }
        })

        // 设置继续按钮点击监听
        btnContinue.setOnClickListener {
            val code = codeEditText.text.toString()
            if (code.length == 4) {
                verifyCode(code)
            } else {
                Toast.makeText(this, R.string.please_enter_verification_code, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
        tvResendCode.setOnClickListener {
            resendVerificationCode()
        }
    }

    private fun verifyCode(code: String) {
        // TODO: 实现验证码验证逻辑
        Toast.makeText(this, "验证码: $code", Toast.LENGTH_SHORT).show()

        // 验证成功后跳转到下一个页面
        // 示例：
        // val intent = Intent(this, NextActivity::class.java)
        // startActivity(intent)
        // finish()
    }

    private fun resendVerificationCode() {
        // TODO: 实现重新发送验证码的逻辑
        Toast.makeText(this, R.string.verification_code_resent, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val EXTRA_EMAIL = "extra_email"
    }
}