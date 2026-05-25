package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
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

    // 所有食材数据（本地静态数据）
    private var allFoodItems: List<FoodItem> = emptyList()
    private var filteredItems: List<FoodItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buy, container, false)

        searchBox = view.findViewById(R.id.search_box)
        vegetableList = view.findViewById(R.id.vegetable_list)
        alphabetScroll = view.findViewById(R.id.alphabet_scroll)

        // 加载本地数据
        allFoodItems = LocalFoodData.getAllFoodItems()
        filteredItems = allFoodItems.toList()
        updateList(filteredItems)

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
     * 更新列表
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

            // 图片：有URL则加载，否则显示默认图标
            if (item.imageUrl.isNotEmpty()) {
                ImageLoader().loadImage(item.imageUrl, imageView, circular = true)
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
        priceTextView.text = "参考价格: ${item.price}"
        tipsTextView.text = "挑选技巧: ${item.tips}"
        storageTextView.text = "保鲜时间: ${item.shelfLifeDays}天"

        // 图片：有URL则加载，否则显示默认图标
        if (item.imageUrl.isNotEmpty()) {
            ImageLoader().loadImage(item.imageUrl, imageView, circular = false)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
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
