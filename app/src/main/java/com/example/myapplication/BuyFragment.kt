package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BuyFragment : Fragment() {

    private lateinit var searchBox: EditText
    private lateinit var vegetableList: ListView
    private lateinit var alphabetScroll: ViewGroup
    private lateinit var loadingProgress: ProgressBar

    private val apiHelper = SpoonacularApiHelper()
    private val imageLoader = ImageLoader()
    private val handler = Handler(Looper.getMainLooper())

    // 所有食材数据（API 或本地兜底）
    private var allFoodItems: List<FoodItem> = emptyList()
    private var filteredItems: List<FoodItem> = emptyList()

    // 原有硬编码数据（兜底用）
    private val localVegetables = listOf(
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)

        searchBox = view.findViewById(R.id.search_box)
        vegetableList = view.findViewById(R.id.vegetable_list)
        alphabetScroll = view.findViewById(R.id.alphabet_scroll)
        loadingProgress = view.findViewById(R.id.loading_progress)

        // 先用本地数据展示
        allFoodItems = apiHelper.getLocalFoodItems()
        filteredItems = allFoodItems.toList()
        updateList(filteredItems)

        // 异步加载 API 数据
        loadFromApi()

        // 搜索框筛选
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                filteredItems = if (keyword.isEmpty()) {
                    allFoodItems.toList()
                } else {
                    allFoodItems.filter { it.name.contains(keyword) }
                }
                updateList(filteredItems)
            }
        })

        // 列表项点击
        vegetableList.setOnItemClickListener { _, _, position, _ ->
            if (position in filteredItems.indices) {
                showVegetableDetail(filteredItems[position])
            }
        }

        // 字母导航点击
        for (i in 0 until alphabetScroll.childCount) {
            val letterView = alphabetScroll.getChildAt(i) as TextView
            letterView.setOnClickListener {
                scrollToLetter(letterView.text.toString())
            }
        }

        // 食材识别 FAB
        val fabRecognize = view.findViewById<FloatingActionButton>(R.id.fab_recognize)
        fabRecognize.setOnClickListener {
            val intent = Intent(requireContext(), ImageRecognitionActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    /**
     * 后台线程从 API 加载数据
     */
    private fun loadFromApi() {
        loadingProgress.visibility = View.VISIBLE
        Thread {
            val apiItems = apiHelper.searchAllIngredients()
            handler.post {
                loadingProgress.visibility = View.GONE
                if (apiItems.isNotEmpty()) {
                    allFoodItems = apiItems
                    // 保持当前搜索状态
                    val keyword = searchBox.text.toString().trim()
                    filteredItems = if (keyword.isEmpty()) {
                        allFoodItems.toList()
                    } else {
                        allFoodItems.filter { it.name.contains(keyword) }
                    }
                    updateList(filteredItems)
                }
                // API 失败时保持本地数据，不闪退
            }
        }.start()
    }

    /**
     * 更新列表（自定义 Adapter，显示圆形图片 + 名称）
     */
    private fun updateList(items: List<FoodItem>) {
        vegetableList.adapter = VegetableAdapter(items)
    }

    /**
     * 自定义列表适配器
     */
    private inner class VegetableAdapter(private val items: List<FoodItem>) : BaseAdapter() {

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): FoodItem = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_vegetable, parent, false)

            val item = items[position]
            val imageView = view.findViewById<ImageView>(R.id.vegetable_image)
            val nameView = view.findViewById<TextView>(R.id.vegetable_name)
            val letterView = view.findViewById<TextView>(R.id.vegetable_letter)

            nameView.text = item.name
            letterView.text = item.pinyinLetter

            // 加载图片
            if (item.imageUrl.isNotEmpty()) {
                imageLoader.loadImage(item.imageUrl, imageView, circular = true)
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            return view
        }
    }

    /**
     * 显示食材详情
     */
    private fun showVegetableDetail(item: FoodItem) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_vegetable_detail, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.vegetable_image)
        val nameTextView = dialogView.findViewById<TextView>(R.id.vegetable_name)
        val seasonTextView = dialogView.findViewById<TextView>(R.id.vegetable_season)
        val priceTextView = dialogView.findViewById<TextView>(R.id.vegetable_price)
        val tipsTextView = dialogView.findViewById<TextView>(R.id.vegetable_tips)
        val storageTextView = dialogView.findViewById<TextView>(R.id.vegetable_storage)

        nameTextView.text = item.name
        seasonTextView.text = "季节: ${item.season}"
        priceTextView.text = if (item.price.isNotEmpty()) "参考价格: ${item.price}" else "参考价格: 本地市场价"
        tipsTextView.text = "挑选技巧: ${item.tips}"
        storageTextView.text = "保鲜时间: ${item.shelfLifeDays}天"

        // 加载详情图片
        if (item.imageUrl.isNotEmpty()) {
            imageLoader.loadImage(item.imageUrl, imageView, circular = false)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 如果有 API ID，异步加载详细信息
        if (item.id > 0) {
            Thread {
                val detail = apiHelper.getIngredientDetail(item.id)
                if (detail != null) {
                    handler.post {
                        if (detail.price.isNotEmpty()) {
                            priceTextView.text = "参考价格: ${detail.price}"
                        }
                        if (detail.tips.startsWith("分类:")) {
                            tipsTextView.text = "分类信息: ${detail.tips.removePrefix("分类:")}"
                        }
                    }
                }
            }.start()
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("+添加到我的食材") { _, _ ->
                addToIngredients(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun scrollToLetter(letter: String) {
        val index = filteredItems.indexOfFirst { it.pinyinLetter == letter }
        if (index >= 0) {
            vegetableList.setSelection(index)
        }
    }

    /**
     * 添加食材到我的食材
     */
    private fun addToIngredients(item: FoodItem) {
        val dbHelper = IngredientDatabaseHelper(requireContext())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val shelfDays = item.shelfLifeDays
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, shelfDays)
        val expireDate = dateFormat.format(calendar.time)
        dbHelper.addIngredient(item.name, "1", "斤", date, expireDate)
        AlertDialog.Builder(requireContext())
            .setTitle("添加成功")
            .setMessage("${item.name} 已添加到今日食材，保鲜期${shelfDays}天")
            .setPositiveButton("确定", null)
            .show()
    }
}
