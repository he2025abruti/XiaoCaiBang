package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
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

    private val apiHelper = SpoonacularApiHelper()
    private val handler = Handler(Looper.getMainLooper())

    private var recognizedFoodName: String? = null
    private var currentImageBytes: ByteArray? = null

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
            try {
                val photoFile = createImageFile()
                photoUri = androidx.core.content.FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", photoFile
                )
                takePictureLauncher.launch(photoUri!!)
            } catch (e: Exception) {
                // 如果相机不可用，提示
                AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("相机不可用，请从相册选择图片")
                    .setPositiveButton("确定", null)
                    .show()
            }
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

    private fun handleImageSelected(uri: Uri) {
        // 显示图片
        val bitmap = uriToBitmap(uri)
        if (bitmap != null) {
            previewImage.setImageBitmap(bitmap)
            // 压缩图片用于 API 识别
            currentImageBytes = compressBitmap(bitmap)
            // 开始识别
            startRecognition()
        }
    }

    private fun startRecognition() {
        val imageBytes = currentImageBytes ?: return

        recognitionProgress.visibility = View.VISIBLE
        resultText.visibility = View.GONE
        btnAddIngredient.visibility = View.GONE

        Thread {
            // 尝试 API 图片识别
            val uploadResult = apiHelper.classifyImageByUpload(imageBytes)

            handler.post {
                recognitionProgress.visibility = View.GONE

                if (uploadResult != null) {
                    // API 识别成功
                    recognizedFoodName = apiHelper.translateName(uploadResult)
                    resultText.text = "识别结果: $recognizedFoodName"
                    resultText.visibility = View.VISIBLE
                    btnAddIngredient.visibility = View.VISIBLE
                } else {
                    // API 识别失败，提示手动输入
                    resultText.text = "自动识别失败，请手动输入食材名称"
                    resultText.visibility = View.VISIBLE
                    manualInputLayout.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun addIngredientByName(name: String) {
        // 查找保鲜天数
        val shelfDays = SpoonacularApiHelper.localShelfLifeMap[name] ?: 7

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

    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        // 限制最大尺寸 800px
        val maxDim = maxOf(bitmap.width, bitmap.height)
        val scaled = if (maxDim > 800) {
            val scale = 800f / maxDim
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }

    private fun createImageFile(): java.io.File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = cacheDir
        return java.io.File.createTempFile("FOOD_${timeStamp}_", ".jpg", storageDir)
    }
}
