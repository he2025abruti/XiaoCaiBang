package com.example.myapplication

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RecipeFragment : Fragment() {

    private lateinit var searchBox: EditText
    private lateinit var categoryContainer: LinearLayout
    private lateinit var recipeList: ListView
    private lateinit var fabAdd: FloatingActionButton

    private var allRecipes = mutableListOf<Recipe>()
    private var filteredRecipes = listOf<RecipeWithStatus>()
    private var categories = listOf<String>()
    private var currentCategory = "全部"
    private var currentKeyword = ""
    private var userIngredientNames = setOf<String>()
    private var networkLoaded = false

    private lateinit var adapter: RecipeAdapter
    private lateinit var dbHelper: IngredientDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)

        searchBox = view.findViewById(R.id.search_box)
        categoryContainer = view.findViewById(R.id.category_container)
        recipeList = view.findViewById(R.id.recipe_list)
        fabAdd = view.findViewById(R.id.fab_add_recipe)

        dbHelper = IngredientDatabaseHelper(requireContext())

        // 加载用户食材
        loadUserIngredients()

        // 加载嵌入式菜谱数据
        allRecipes.addAll(RecipeData.RECIPE_LIST)

        // 加载自定义菜谱（从 SQLite）
        val customRecipes = dbHelper.getAllCustomRecipes()
        if (customRecipes.isNotEmpty()) {
            val existingNames = allRecipes.map { it.name }.toSet()
            allRecipes.addAll(0, customRecipes.filter { it.name !in existingNames })
        }

        // 初始化分类和列表
        categories = RecipeData.getAllCategories()
        setupCategoryTags()
        adapter = RecipeAdapter(requireContext(), emptyList())
        recipeList.adapter = adapter

        // 设置添加按钮点击收藏
        adapter.setOnAddClickListener { recipe ->
            showCollectDialog(recipe)
        }

        // 列表项点击显示详情
        recipeList.setOnItemClickListener { _, _, position, _ ->
            if (position in filteredRecipes.indices) {
                showRecipeDetail(filteredRecipes[position].recipe)
            }
        }

        // 搜索框
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s.toString().trim()
                applyFilter()
            }
        })

        // 悬浮按钮：添加自定义菜谱
        fabAdd.setOnClickListener {
            showAddRecipeDialog()
        }

        // 异步加载网络菜谱
        loadNetworkRecipes()

        // 初始过滤
        applyFilter()

        return view
    }

    override fun onResume() {
        super.onResume()
        // 刷新用户食材（可能在食材页有变动）
        loadUserIngredients()
        applyFilter()
    }

    private fun loadUserIngredients() {
        val ingredients = dbHelper.getAllIngredients()
        userIngredientNames = ingredients.map { it.name }.toSet()
    }

    /**
     * 异步加载网络菜谱数据
     */
    private fun loadNetworkRecipes() {
        Thread {
            try {
                val url = URL("https://songer.datasn.com/data/api/v1/u_4db7936df78dfe468fc2/recipe_for_food_1/main/list/?app=json")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.requestMethod = "GET"
                conn.connect()

                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    val jsonObj = JSONObject(json)
                    val output = jsonObj.optJSONObject("output") ?: return@Thread
                    val rows = output.optJSONObject("rows") ?: return@Thread

                    val networkRecipes = mutableListOf<Recipe>()
                    val keys = rows.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = rows.optJSONObject(key) ?: continue
                        val recipe = Recipe.fromJson(obj)
                        if (recipe.name.isNotEmpty()) {
                            networkRecipes.add(recipe)
                        }
                    }

                    if (networkRecipes.isNotEmpty()) {
                        activity?.runOnUiThread {
                            // 去重：不覆盖已有的嵌入式数据中的同名菜谱
                            val existingNames = allRecipes.map { it.name }.toSet()
                            val newRecipes = networkRecipes.filter { it.name !in existingNames }
                            allRecipes.addAll(newRecipes)
                            networkLoaded = true
                            categories = buildCategories()
                            rebuildCategoryTags()
                            applyFilter()
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                // 网络请求失败，静默降级，使用嵌入式数据
            }
        }.start()
    }

    private fun buildCategories(): List<String> {
        return listOf("全部") + allRecipes.map { it.category }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    /**
     * 设置分类标签
     */
    private fun setupCategoryTags() {
        categoryContainer.removeAllViews()
        for (cat in categories) {
            val tagView = createTagView(cat)
            categoryContainer.addView(tagView)
        }
    }

    /**
     * 网络数据加载后重建分类标签
     */
    private fun rebuildCategoryTags() {
        categoryContainer.removeAllViews()
        for (cat in categories) {
            val tagView = createTagView(cat)
            categoryContainer.addView(tagView)
        }
    }

    /**
     * 创建单个分类标签
     */
    private fun createTagView(category: String): TextView {
        val tagView = TextView(requireContext())
        tagView.text = category
        tagView.textSize = 13f
        tagView.gravity = Gravity.CENTER
        tagView.setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6))

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(dpToPx(4), 0, dpToPx(4), 0)
        tagView.layoutParams = lp

        updateTagStyle(tagView, category == currentCategory)

        tagView.setOnClickListener {
            currentCategory = category
            // 更新所有标签样式
            for (i in 0 until categoryContainer.childCount) {
                val child = categoryContainer.getChildAt(i) as TextView
                updateTagStyle(child, child.text == category)
            }
            applyFilter()
        }

        return tagView
    }

    private fun updateTagStyle(tagView: TextView, selected: Boolean) {
        if (selected) {
            tagView.setBackgroundResource(R.drawable.bg_tag_selected)
            tagView.setTextColor(0xFFFFFFFF.toInt())
            tagView.setTypeface(null, Typeface.BOLD)
        } else {
            tagView.setBackgroundResource(R.drawable.bg_tag_unselected)
            tagView.setTextColor(0xFF666666.toInt())
            tagView.setTypeface(null, Typeface.NORMAL)
        }
    }

    /**
     * 应用搜索和分类过滤
     */
    private fun applyFilter() {
        var result = allRecipes.toList()

        // 分类过滤
        if (currentCategory != "全部") {
            result = result.filter { it.category == currentCategory }
        }

        // 搜索过滤
        if (currentKeyword.isNotEmpty()) {
            result = result.filter { recipe ->
                recipe.name.contains(currentKeyword) ||
                        recipe.mainIngredients.contains(currentKeyword) ||
                        recipe.sideIngredients.contains(currentKeyword) ||
                        recipe.seasonings.contains(currentKeyword)
            }
        }

        // 计算食材匹配度
        filteredRecipes = result.map { recipe ->
            val recipeIngredients = recipe.getAllIngredientNames()
            val totalCount = recipeIngredients.size
            val matchedCount = if (totalCount > 0) {
                recipeIngredients.count { name ->
                    userIngredientNames.any { userName ->
                        userName.contains(name) || name.contains(userName)
                    }
                }
            } else {
                0
            }
            RecipeWithStatus(recipe, matchedCount, totalCount)
        }

        adapter.updateData(filteredRecipes)
    }

    /**
     * 显示菜谱详情
     */
    private fun showRecipeDetail(recipe: Recipe) {
        val sb = StringBuilder()
        sb.append("【菜名】${recipe.name}\n\n")
        if (recipe.category.isNotEmpty()) sb.append("【分类】${recipe.category}\n")
        if (recipe.gongyi.isNotEmpty()) sb.append("【工艺】${recipe.gongyi}\n")
        if (recipe.kouwei.isNotEmpty()) sb.append("【口味】${recipe.kouwei}\n")
        sb.append("【烹饪时间】${recipe.cookTime}分钟\n\n")
        sb.append("【主料】\n${recipe.mainIngredients}\n")
        if (recipe.sideIngredients.isNotEmpty()) sb.append("【辅料】\n${recipe.sideIngredients}\n")
        if (recipe.seasonings.isNotEmpty()) sb.append("【调料】\n${recipe.seasonings}\n")
        sb.append("\n【做法】\n${recipe.steps}")

        AlertDialog.Builder(requireContext())
            .setTitle(recipe.name)
            .setMessage(sb.toString())
            .setPositiveButton("收藏") { _, _ -> showCollectDialog(recipe) }
            .setNegativeButton("关闭", null)
            .show()
    }

    /**
     * 收藏菜谱提示
     */
    private fun showCollectDialog(recipe: Recipe) {
        AlertDialog.Builder(requireContext())
            .setTitle("收藏成功")
            .setMessage("「${recipe.name}」已加入收藏")
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 显示添加自定义菜谱弹窗
     */
    private fun showAddRecipeDialog() {
        val dialog = AddRecipeDialog(requireContext(), categories) { recipe ->
            // 保存到 SQLite
            dbHelper.addRecipe(
                recipe.name,
                recipe.category,
                recipe.steps,
                recipe.mainIngredients,
                recipe.imageUrl,
                recipe.description
            )
            // 加入列表
            allRecipes.add(0, recipe)
            categories = buildCategories()
            rebuildCategoryTags()
            applyFilter()
            Toast.makeText(requireContext(), "菜谱「${recipe.name}」添加成功", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
