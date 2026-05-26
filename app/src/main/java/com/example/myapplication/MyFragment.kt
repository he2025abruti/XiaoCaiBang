package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

class MyFragment : Fragment() {

    private lateinit var avatar: ImageView
    private lateinit var username: TextView
    private lateinit var settingsList: ListView
    private lateinit var sharedPreferences: SharedPreferences

    // 相册选图
    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { setAvatarFromUri(it) }
    }

    // 拍照（返回缩略图 Bitmap，更可靠）
    private val takeAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { setAvatarFromBitmap(it) }
    }

    // 相机权限请求
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            takeAvatarLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my, container, false)

        avatar = view.findViewById(R.id.avatar)
        username = view.findViewById(R.id.username)
        settingsList = view.findViewById(R.id.settings_list)
        sharedPreferences = requireContext().getSharedPreferences("user_settings", 0)

        // 加载保存的用户名
        val savedUsername = sharedPreferences.getString("username", "用户名")
        username.text = savedUsername

        // 加载保存的头像
        loadSavedAvatar()

        // 设置头像点击事件
        avatar.setOnClickListener {
            showChangeAvatarDialog()
        }

        // 设置用户名点击事件
        username.setOnClickListener {
            showChangeUsernameDialog()
        }

        // 设置设置列表
        settingsList.adapter = SettingsAdapter()

        return view
    }

    private inner class SettingsAdapter : BaseAdapter() {
        private val settings = listOf("我的收藏", "猜猜今天吃什么", "主题设置")

        override fun getCount(): Int = settings.size
        override fun getItem(position: Int): String = settings[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.settings_item, parent, false)

            val nameView = view.findViewById<TextView>(R.id.setting_name)
            val switchView = view.findViewById<Switch>(R.id.setting_switch)

            nameView.text = settings[position]

            when (position) {
                0 -> {
                    // 我的收藏 - 隐藏 Switch，点击跳转
                    switchView.visibility = View.GONE
                    switchView.setOnCheckedChangeListener(null)
                    view.setOnClickListener {
                        showFavoritePage()
                    }
                }
                1 -> {
                    // 猜猜今天吃什么 - 隐藏 Switch，点击跳转
                    switchView.visibility = View.GONE
                    switchView.setOnCheckedChangeListener(null)
                    view.setOnClickListener {
                        showSpinWheelPage()
                    }
                }
                2 -> {
                    // 主题设置 - 显示 Switch
                    switchView.visibility = View.VISIBLE
                    val isNightMode = sharedPreferences.getBoolean("night_mode", false)
                    switchView.setOnCheckedChangeListener(null)
                    switchView.isChecked = isNightMode
                    switchView.setOnCheckedChangeListener { _, isChecked ->
                        sharedPreferences.edit()
                            .putBoolean("night_mode", isChecked)
                            .putInt("restore_tab", R.id.nav_my)
                            .apply()
                        requireActivity().recreate()
                    }
                    view.setOnClickListener(null)
                }
            }

            return view
        }
    }

    private fun showFavoritePage() {
        val fragment = FavoriteRecipeFragment()
        fragment.onBackClick = {
            val act = fragment.activity
            if (act != null && fragment.isAdded) {
                act.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MyFragment())
                    .commitAllowingStateLoss()
            }
        }
        fragment.onFavoriteChanged = {
            // 收藏变化时的回调，留空即可
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun showSpinWheelPage() {
        val fragment = SpinWheelFragment()
        fragment.onBackClick = {
            val act = fragment.activity
            if (act != null && fragment.isAdded) {
                act.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MyFragment())
                    .commitAllowingStateLoss()
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun showChangeAvatarDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("修改头像")
            .setMessage("选择头像来源")
            .setPositiveButton("相机") { _, _ ->
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    takeAvatarLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            .setNegativeButton("相册") { _, _ ->
                pickAvatarLauncher.launch("image/*")
            }
            .show()
    }

    private fun setAvatarFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                setAvatarFromBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAvatarFromBitmap(bitmap: Bitmap) {
        avatar.setImageBitmap(bitmap)
        avatar.scaleType = ImageView.ScaleType.CENTER_CROP
        saveAvatarToInternal(bitmap)
    }

    private fun saveAvatarToInternal(bitmap: Bitmap) {
        try {
            val file = File(requireContext().filesDir, "avatar.jpg")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }
            sharedPreferences.edit().putString("avatar_path", file.absolutePath).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedAvatar() {
        val path = sharedPreferences.getString("avatar_path", null)
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(path)
                if (bitmap != null) {
                    avatar.setImageBitmap(bitmap)
                    avatar.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }
    }

    private fun showChangeUsernameDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_username, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.username_edit)
        usernameEditText.setText(username.text)

        AlertDialog.Builder(requireContext())
            .setTitle("修改用户名")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val newUsername = usernameEditText.text.toString()
                username.text = newUsername
                sharedPreferences.edit().putString("username", newUsername).apply()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
