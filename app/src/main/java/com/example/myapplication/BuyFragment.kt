package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date

class BuyFragment : Fragment() {

    private lateinit var searchBox: EditText
    private lateinit var vegetableList: ListView
    private lateinit var alphabetScroll: ViewGroup

    // 模拟蔬菜水果数据
    private val vegetables = listOf(
        "苹果", "香蕉", "橙子", "菠萝", "葡萄",
        "白菜", "萝卜", "西红柿", "黄瓜", "茄子",
        "土豆", "洋葱", "胡萝卜", "青椒", "西兰花"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)

        searchBox = view.findViewById(R.id.search_box)
        vegetableList = view.findViewById(R.id.vegetable_list)
        alphabetScroll = view.findViewById(R.id.alphabet_scroll)

        // 设置蔬菜水果列表
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, vegetables)
        vegetableList.adapter = adapter

        // 设置列表项点击事件
        vegetableList.setOnItemClickListener { _, _, position, _ ->
            showVegetableDetail(vegetables[position])
        }

        // 设置字母导航点击事件
        for (i in 0 until alphabetScroll.childCount) {
            val letterView = alphabetScroll.getChildAt(i) as TextView
            letterView.setOnClickListener {
                // 跳转到对应字母开头的蔬菜水果
                val letter = letterView.text.toString()
                scrollToLetter(letter)
            }
        }

        return view
    }

    private fun showVegetableDetail(vegetable: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_vegetable_detail, null)
        val nameTextView = dialogView.findViewById<TextView>(R.id.vegetable_name)
        val seasonTextView = dialogView.findViewById<TextView>(R.id.vegetable_season)
        val priceTextView = dialogView.findViewById<TextView>(R.id.vegetable_price)
        val tipsTextView = dialogView.findViewById<TextView>(R.id.vegetable_tips)
        val storageTextView = dialogView.findViewById<TextView>(R.id.vegetable_storage)

        // 设置蔬菜水果详情
        nameTextView.text = vegetable
        seasonTextView.text = "季节: 全年"
        priceTextView.text = "价格区间: 东南 3-5元/斤, 西北 4-6元/斤"
        tipsTextView.text = "挑选技巧: 选择外观新鲜，无损伤的"
        storageTextView.text = "保鲜时间: 3-5天"

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("+添加到我的食材") { _, _ ->
                // 添加到食材
                addToIngredients(vegetable)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun scrollToLetter(letter: String) {
        // 实现跳转到对应字母开头的蔬菜水果
        // 这里暂时只打印日志
        println("跳转到字母: $letter")
    }

    private fun addToIngredients(vegetable: String) {
        // 创建数据库帮助类
        val dbHelper = IngredientDatabaseHelper(requireContext())
        // 获取当前日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = dateFormat.format(Date())
        // 保存到数据库
        dbHelper.addIngredient(vegetable, "1", "斤", date)
        // 显示添加成功的提示
        AlertDialog.Builder(requireContext())
            .setTitle("添加成功")
            .setMessage("$vegetable 已添加到今日食材")
            .setPositiveButton("确定", null)
            .show()
    }
}
