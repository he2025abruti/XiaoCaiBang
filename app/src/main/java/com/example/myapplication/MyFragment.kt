package com.example.myapplication

import android.app.AlertDialog
import android.content.SharedPreferences
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
        private val settings = listOf("我的收藏", "主题设置")

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
                    // 主题设置 - 显示 Switch
                    switchView.visibility = View.VISIBLE
                    val isNightMode = sharedPreferences.getBoolean("night_mode", false)
                    switchView.setOnCheckedChangeListener(null)
                    switchView.isChecked = isNightMode
                    switchView.setOnCheckedChangeListener { _, isChecked ->
                        sharedPreferences.edit().putBoolean("night_mode", isChecked).apply()
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
            if (isAdded && activity != null) {
                requireActivity().supportFragmentManager.beginTransaction()
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

    private fun showChangeAvatarDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("修改头像")
            .setMessage("选择头像来源")
            .setPositiveButton("相机") { _, _ ->
                println("打开相机")
            }
            .setNegativeButton("相册") { _, _ ->
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
                sharedPreferences.edit().putString("username", newUsername).apply()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
