package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ImageRecognitionActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var recognitionProgress: ProgressBar
    private lateinit var resultText: TextView
    private lateinit var inputFoodName: EditText
    private lateinit var btnAddManual: Button
    private lateinit var btnAddIngredient: Button
    private lateinit var manualInputLayout: View

    private val handler = Handler(Looper.getMainLooper())

    private var recognizedFoodName: String? = null
    private var currentBitmap: Bitmap? = null

    // 相册选图
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    // 拍照
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = photoUri
        if (success && uri != null) {
            handleImageSelected(uri)
        }
    }

    private var photoUri: Uri? = null

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)

        previewImage = findViewById(R.id.preview_image)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        recognitionProgress = findViewById(R.id.recognition_progress)
        resultText = findViewById(R.id.result_text)
        inputFoodName = findViewById(R.id.input_food_name)
        btnAddManual = findViewById(R.id.btn_add_manual)
        btnAddIngredient = findViewById(R.id.btn_add_ingredient)
        manualInputLayout = findViewById(R.id.manual_input_layout)

        btnCamera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnAddManual.setOnClickListener {
            val name = inputFoodName.text.toString().trim()
            if (name.isNotEmpty()) {
                addIngredientByName(name)
            }
        }

        btnAddIngredient.setOnClickListener {
            recognizedFoodName?.let { name ->
                addIngredientByName(name)
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = androidx.core.content.FileProvider.getUriForFile(
                this, "${packageName}.fileprovider", photoFile
            )
            takePictureLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("相机不可用，请从相册选择图片")
                .setPositiveButton("确定", null)
                .show()
        }
    }

    private fun handleImageSelected(uri: Uri) {
        val bitmap = uriToBitmap(uri)
        if (bitmap != null) {
            previewImage.setImageBitmap(bitmap)
            currentBitmap = bitmap
            startRecognition(bitmap)
        }
    }

    private fun startRecognition(bitmap: Bitmap) {
        recognitionProgress.visibility = View.VISIBLE
        resultText.visibility = View.GONE
        btnAddIngredient.visibility = View.GONE
        manualInputLayout.visibility = View.GONE

        Thread {
            val glResult = GlmImageRecognizer.recognize(bitmap)

            handler.post {
                recognitionProgress.visibility = View.GONE

                if (glResult != null && glResult.isNotBlank()) {
                    // GLM 识别成功
                    recognizedFoodName = extractFoodName(glResult)
                    resultText.text = "识别结果: $recognizedFoodName"
                    resultText.visibility = View.VISIBLE
                    btnAddIngredient.visibility = View.VISIBLE
                } else {
                    // 识别失败，显示错误原因并提示手动输入
                    val errorMsg = GlmImageRecognizer.lastError ?: "请检查网络连接"
                    resultText.text = "识别失败: $errorMsg\n请手动输入食材名称"
                    resultText.visibility = View.VISIBLE
                    manualInputLayout.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    /**
     * 从 GLM 返回文本中提取食材名称（取第一行或整个文本作为食材名）
     */
    private fun extractFoodName(raw: String): String {
        // 取第一行非空文本作为食材名
        val firstLine = raw.lines().firstOrNull { it.isNotBlank() } ?: return raw
        // 去掉可能的序号前缀 "1. " "、" 等
        return firstLine.replace(Regex("^[\\d.、\\s]+"), "").trim()
    }

    private fun addIngredientByName(name: String) {
        val shelfDays = LocalFoodData.getAllShelfLifeMap()[name] ?: 7

        val dbHelper = IngredientDatabaseHelper(this)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, shelfDays)
        val expireDate = dateFormat.format(calendar.time)

        dbHelper.addIngredient(name, "1", "斤", date, expireDate)

        AlertDialog.Builder(this)
            .setTitle("添加成功")
            .setMessage("$name 已添加到今日食材，保鲜期${shelfDays}天")
            .setPositiveButton("确定") { _, _ -> finish() }
            .show()
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    private fun createImageFile(): java.io.File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = cacheDir
        return java.io.File.createTempFile("FOOD_${timeStamp}_", ".jpg", storageDir)
    }
}
