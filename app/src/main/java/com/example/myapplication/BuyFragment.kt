package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BuyFragment : Fragment() {

    private lateinit var searchBox: EditText
    private lateinit var vegetableList: ListView
    private lateinit var alphabetScroll: ViewGroup

    // 蔬菜水果数据（中文名, 拼音首字母）
    private val allVegetables = listOf(
        "白菜" to "B", "菠萝" to "B",
        "橙子" to "C",
        "大蒜" to "D", "冬瓜" to "D",
        "番薯" to "F",
        "甘蔗" to "G",
        "胡萝卜" to "H", "黄瓜" to "H", "火龙果" to "H",
        "韭菜" to "J", "橘子" to "J",
        "苦瓜" to "K",
        "萝卜" to "L", "蓝莓" to "L", "辣椒" to "L", "梨" to "L", "荔枝" to "L",
        "芒果" to "M", "猕猴桃" to "M", "蘑菇" to "M", "木耳" to "M",
        "南瓜" to "N", "柠檬" to "N", "牛肉" to "N",
        "苹果" to "P", "葡萄" to "P",
        "芹菜" to "Q", "茄子" to "Q", "青椒" to "Q",
        "山药" to "S", "生姜" to "S", "丝瓜" to "S",
        "土豆" to "T", "桃子" to "T", "甜椒" to "T",
        "莴笋" to "W",
        "西红柿" to "X", "西兰花" to "X", "香蕉" to "X", "西瓜" to "X", "香菇" to "X",
        "洋葱" to "Y", "玉米" to "Y", "柚子" to "Y", "芋头" to "Y",
        "竹笋" to "Z"
    ).sortedBy { it.second }

    private var filteredVegetables: List<Pair<String, String>> = allVegetables.toList()

    // 各食材默认保鲜天数
    private val shelfLifeMap = mapOf(
        "白菜" to 7, "菠萝" to 5,
        "橙子" to 14,
        "大蒜" to 30, "冬瓜" to 14,
        "番薯" to 14,
        "甘蔗" to 7,
        "胡萝卜" to 14, "黄瓜" to 5, "火龙果" to 5,
        "韭菜" to 3, "橘子" to 10,
        "苦瓜" to 7,
        "萝卜" to 14, "蓝莓" to 5, "辣椒" to 7, "梨" to 7, "荔枝" to 3,
        "芒果" to 5, "猕猴桃" to 7, "蘑菇" to 3, "木耳" to 7,
        "南瓜" to 30, "柠檬" to 14, "牛肉" to 3,
        "苹果" to 14, "葡萄" to 5,
        "芹菜" to 5, "茄子" to 7, "青椒" to 7,
        "山药" to 14, "生姜" to 30, "丝瓜" to 5,
        "土豆" to 14, "桃子" to 5, "甜椒" to 7,
        "莴笋" to 5,
        "西红柿" to 7, "西兰花" to 5, "香蕉" to 5, "西瓜" to 7, "香菇" to 3,
        "洋葱" to 30, "玉米" to 3, "柚子" to 14, "芋头" to 14,
        "竹笋" to 5
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

        updateList(filteredVegetables)

        // 搜索框筛选
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                filteredVegetables = if (keyword.isEmpty()) {
                    allVegetables.toList()
                } else {
                    allVegetables.filter { it.first.contains(keyword) }
                }
                updateList(filteredVegetables)
            }
        })

        // 设置列表项点击事件
        vegetableList.setOnItemClickListener { _, _, position, _ ->
            showVegetableDetail(filteredVegetables[position].first)
        }

        // 设置字母导航点击事件
        for (i in 0 until alphabetScroll.childCount) {
            val letterView = alphabetScroll.getChildAt(i) as TextView
            letterView.setOnClickListener {
                val letter = letterView.text.toString()
                scrollToLetter(letter)
            }
        }

        return view
    }

    private fun updateList(vegetables: List<Pair<String, String>>) {
        val data = vegetables.map { mapOf("name" to it.first, "letter" to it.second) }
        val adapter = SimpleAdapter(
            requireContext(),
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("name", "letter"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        vegetableList.adapter = adapter
    }

    private fun showVegetableDetail(vegetable: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_vegetable_detail, null)
        val nameTextView = dialogView.findViewById<TextView>(R.id.vegetable_name)
        val seasonTextView = dialogView.findViewById<TextView>(R.id.vegetable_season)
        val priceTextView = dialogView.findViewById<TextView>(R.id.vegetable_price)
        val tipsTextView = dialogView.findViewById<TextView>(R.id.vegetable_tips)
        val storageTextView = dialogView.findViewById<TextView>(R.id.vegetable_storage)

        nameTextView.text = vegetable
        seasonTextView.text = "季节: 全年"
        priceTextView.text = "价格区间: 东南 3-5元/斤, 西北 4-6元/斤"
        tipsTextView.text = "挑选技巧: 选择外观新鲜，无损伤的"
        val shelfDays = shelfLifeMap[vegetable] ?: 7
        storageTextView.text = "保鲜时间: ${shelfDays}天"

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("+添加到我的食材") { _, _ ->
                addToIngredients(vegetable)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun scrollToLetter(letter: String) {
        val index = filteredVegetables.indexOfFirst { it.second == letter }
        if (index >= 0) {
            vegetableList.setSelection(index)
        }
    }

    private fun addToIngredients(vegetable: String) {
        val dbHelper = IngredientDatabaseHelper(requireContext())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val shelfDays = shelfLifeMap[vegetable] ?: 7
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, shelfDays)
        val expireDate = dateFormat.format(calendar.time)
        dbHelper.addIngredient(vegetable, "1", "斤", date, expireDate)
        AlertDialog.Builder(requireContext())
            .setTitle("添加成功")
            .setMessage("$vegetable 已添加到今日食材，保鲜期${shelfDays}天")
            .setPositiveButton("确定", null)
            .show()
    }
}
