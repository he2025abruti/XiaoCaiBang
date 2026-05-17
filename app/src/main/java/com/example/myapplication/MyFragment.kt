package com.example.myapplication

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class MyFragment : Fragment() {

    private lateinit var avatar: ImageView
    private lateinit var username: TextView
    private lateinit var settingsList: ListView
    private lateinit var sharedPreferences: SharedPreferences

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

        // 加载保存的主题设置
        val isNightMode = sharedPreferences.getBoolean("night_mode", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 设置头像点击事件
        avatar.setOnClickListener {
            showChangeAvatarDialog()
        }

        // 设置用户名点击事件
        username.setOnClickListener {
            showChangeUsernameDialog()
        }

        // 设置设置列表
        val settings = listOf("主题设置")
        val adapter = ArrayAdapter(requireContext(), R.layout.settings_item, settings)
        settingsList.adapter = adapter

        // 设置列表项点击事件
        settingsList.setOnItemClickListener { _, itemView, position, _ ->
            when (position) {
                0 -> {
                    val switch = itemView.findViewById<Switch>(R.id.setting_switch)
                    switch.isChecked = isNightMode
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            // 切换到夜间模式
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            sharedPreferences.edit().putBoolean("night_mode", true).apply()
                        } else {
                            // 切换到日间模式
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            sharedPreferences.edit().putBoolean("night_mode", false).apply()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun showChangeAvatarDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("修改头像")
            .setMessage("选择头像来源")
            .setPositiveButton("相机") { _, _ ->
                // 打开相机
                println("打开相机")
            }
            .setNegativeButton("相册") { _, _ ->
                // 打开相册
                println("打开相册")
            }
            .show()
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
                // 保存用户名到SharedPreferences
                sharedPreferences.edit().putString("username", newUsername).apply()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
