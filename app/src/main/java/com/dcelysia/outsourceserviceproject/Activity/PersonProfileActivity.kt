package com.dcelysia.outsourceserviceproject.Activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dcelysia.outsourceserviceproject.Model.data.response.UpdateUserProfile
import com.dcelysia.outsourceserviceproject.Network.Resource
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.UI.CustomToast
import com.dcelysia.outsourceserviceproject.UI.PhotoPickerDialog
import com.dcelysia.outsourceserviceproject.Utils.mmkv.UserInfoManager
import com.dcelysia.outsourceserviceproject.ViewModel.PersonProfileViewModel
import com.dcelysia.outsourceserviceproject.databinding.ActivityPersonProfileBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PersonProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonProfileBinding
    private val setAvatar by lazy { binding.personProfileSetAvatar }
    private val avatar by lazy { binding.userProfileAvatar }
    private val back by lazy { binding.personProfileBack }

    private val account by lazy { binding.userProfileName }
    private val bio by lazy { binding.userProfileBio }
    private val gender by lazy { binding.userProfileGender }
    private val birthday by lazy { binding.userProfileBirthday }
    private val website by lazy { binding.userProfileWebsite }

    private val submit by lazy { binding.userProfileSubmit }
    private val overlay by lazy { binding.personProfileLoadingOverlay }

    private var currentPhotoUri: Uri? = null

    private val viewModel by lazy { PersonProfileViewModel() }

    companion object {
        private const val REQUEST_CAMERA = 1001
        private const val REQUEST_GALLERY = 1002

        // 图片最大尺寸大于90 保证图片清晰度
        private const val MAX_IMAGE_SIZE = 180

        // 将质量压缩到85%左右
        private const val COMPRESS_QUALITY = 85
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        observeViewModel()
        binding.personProfileBack.setOnClickListener { finish() }
        setAvatar.setOnClickListener { setAvatar() }
        birthday.setOnClickListener { setBirthday() }
        gender.setOnClickListener { showGenderPickerDialog() }
        submit.setOnClickListener { submit() }
        back.setOnClickListener { finish() }
    }

    private fun submit() {
        val updateUserProfile = UpdateUserProfile(
            account.text.toString(),
            UserInfoManager.cacheBaseUserProfile?.avatarUrl ?: "",
            bio.text.toString(),
            when (gender.text.toString()) {
                "男" -> 0
                "女" -> 1
                else -> 2
            },
            1,
            birthday.text.toString(),
            null,
            website.text.toString(),
            "Passionate about technology, coding, and outdoor activities."
        )
        viewModel.updateUserProfile(updateUserProfile)
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return "${year}-${String.format(Locale.getDefault(), "%02d-%02d", month, day)}"
    }

    private fun setBirthday() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this@PersonProfileActivity,
            { _, year, month, day ->
                birthday.text = formatDate(year, month, day)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun showGenderPickerDialog() {
        // 定义性别选项数组
        val genders = arrayOf("男", "女", "保密")

        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = genders.size - 1
            displayedValues = genders
            wrapSelectorWheel = false
        }

        // 使用 AlertDialog 包含 NumberPicker
        AlertDialog.Builder(this)
            .setTitle("请选择性别")
            .setView(numberPicker)
            .setPositiveButton("确定") { dialog, _ ->
                // 获取选中的数组下标对应的性别
                val selectedGender = genders[numberPicker.value]
                // 例如可更新某个 TextView 显示选中的性别
                gender.text = selectedGender
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.personProfileAvatar.collect { avatarUrl ->
                        loadAvatar(avatarUrl)
                    }
                }
                launch {
                    viewModel.userProfile.collect { response ->
                        when (response) {
                            is Resource.Success -> {
                                Log.d("PersonProfileActivity", "data数据为${response.data}")
                                val userProfile = response.data
                                account.setText(userProfile.account)
                                bio.setText(userProfile.bio)
                                gender.text = when (userProfile.gender) {
                                    0 -> "男"
                                    1 -> "女"
                                    else -> "保密"
                                }
                                birthday.text = userProfile.birthDate
                                website.setText(userProfile.website)
                                Glide.with(this@PersonProfileActivity)
                                    .load(userProfile.avatarUrl)
                                    .into(avatar)
                            }

                            is Resource.Error -> {
                                CustomToast.showMessage(
                                    this@PersonProfileActivity,
                                    "出错啦, ${response.message}"
                                )
                            }

                            else -> {}
                        }
                    }
                }

                launch {
                    // 收集上传结果
                    viewModel.uploadResult.collect { result ->
                        when (result) {
                            is Resource.Error -> {
                                CustomToast.showMessage(
                                    this@PersonProfileActivity,
                                    "上传失败 ${result.message}"
                                )
                            }

                            is Resource.Success -> {
                                viewModel.updateAvatar(result.data.data.avatarUrl)
                            }

                            is Resource.Loading -> {}
                            null -> {}
                        }
                    }
                }

                launch {
                    viewModel.updateResponse.collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                showOverlay()
                            }
                            is Resource.Success -> {
                                hideOverlay()
                                result.data.data?.let {
                                    UserInfoManager.updateUserProfile(it)
                                }
                                CustomToast.showMessage(this@PersonProfileActivity, "更新成功")
                                finish()
                            }
                            is Resource.Error -> {
                                hideOverlay()
                                CustomToast.showMessage(this@PersonProfileActivity, "出错啦，${result.message}")
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private fun setAvatar() {
        PhotoPickerDialog(
            this,
            onGalleryClick = { checkGalleryPermission() },
            onCameraClick = { checkCameraPermission() }
        ).show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> openCamera()

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        }
    }

    private fun checkGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_GALLERY
            )
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            intent.resolveActivity(packageManager)?.let {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }

            REQUEST_GALLERY -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CAMERA -> currentPhotoUri?.let { startCrop(it) }
            REQUEST_GALLERY -> data?.data?.let { startCrop(it) }
            UCrop.REQUEST_CROP -> handleCropResult(data)
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = createCropDestinationUri()
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            .withOptions(getCropOptions())
            .start(this)
    }

    private fun getCropOptions(): UCrop.Options {
        return UCrop.Options().apply {
            setCircleDimmedLayer(true) // 设置圆形裁剪
            setShowCropFrame(false) // 隐藏裁剪框
            setShowCropGrid(false) // 隐藏网格
            setCompressionQuality(COMPRESS_QUALITY) // 设置压缩质量
            setHideBottomControls(true) // 隐藏底部控制栏
            setToolbarColor(ContextCompat.getColor(this@PersonProfileActivity, R.color.white))
            setStatusBarColor(ContextCompat.getColor(this@PersonProfileActivity, R.color.white))
        }
    }

    private fun createCropDestinationUri(): Uri {
        val file = File(this.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            file
        )
    }

    private fun loadAvatar(uri: String) {
        Glide.with(this)
            .load(uri)
            .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(avatar)
    }


    /**
     * 处理裁剪结果
     * @param data Intent 数据
     */
    private fun handleCropResult(data: Intent?) {
        val resultUri = UCrop.getOutput(data!!)
        resultUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val compressedFile = compressImage(uri) // 压缩图片
                    withContext(Dispatchers.Main) {
                        viewModel.updateAvatar(compressedFile.toUri().toString()) // 更新头像
                        viewModel.uploadAvatar(compressedFile) // 上传头像
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        CustomToast.showMessage(
                            this@PersonProfileActivity,
                            "图片处理失败：${e.message}"
                        )
                    }
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * 压缩图片
     * @param uri 要压缩的图片 Uri
     * @return 压缩后的文件
     */
    private fun compressImage(uri: Uri): File {
        // 获取图片原始尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // 计算压缩比例
        val scale = calculateInSampleSize(
            options.outWidth,
            options.outHeight,
            MAX_IMAGE_SIZE,
            MAX_IMAGE_SIZE
        )

        // 加载压缩后的图片
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = scale
        }

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("无法加载图片")

        // 保存压缩后的图片
        val outputFile = createImageFile()
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
            out.flush()
        }
        bitmap.recycle()
        return outputFile
    }

    /**
     * 计算图片压缩比例
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
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

}