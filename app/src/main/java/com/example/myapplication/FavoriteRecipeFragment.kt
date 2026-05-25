package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class FavoriteRecipeFragment : Fragment() {

    private lateinit var favoriteList: ListView
    private lateinit var emptyText: TextView
    private lateinit var backBtn: TextView
    private lateinit var dbHelper: IngredientDatabaseHelper
    private lateinit var adapter: RecipeAdapter
    private var userIngredientNames = setOf<String>()

    var onBackClick: (() -> Unit)? = null
    var onFavoriteChanged: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_recipe, container, false)

        favoriteList = view.findViewById(R.id.favorite_list)
        emptyText = view.findViewById(R.id.empty_text)
        backBtn = view.findViewById(R.id.back_btn)
        dbHelper = IngredientDatabaseHelper(requireContext())

        backBtn.setOnClickListener {
            onBackClick?.invoke()
        }

        // 加载用户食材
        val ingredients = dbHelper.getAllIngredients()
        userIngredientNames = ingredients.map { it.name }.toSet()

        adapter = RecipeAdapter(requireContext(), emptyList())
        favoriteList.adapter = adapter

        // 取消收藏
        adapter.setOnFavoriteToggle { recipe ->
            AlertDialog.Builder(requireContext())
                .setTitle("取消收藏")
                .setMessage("确定取消收藏「${recipe.name}」吗？")
                .setPositiveButton("确定") { _, _ ->
                    dbHelper.removeFavorite(recipe.name)
                    Toast.makeText(requireContext(), "已取消收藏", Toast.LENGTH_SHORT).show()
                    loadFavorites()
                    onFavoriteChanged?.invoke()
                }
                .setNegativeButton("取消", null)
                .show()
        }

        // 点击查看详情
        favoriteList.setOnItemClickListener { _, _, position, _ ->
            val items = adapter.getItems()
            if (position in items.indices) {
                showRecipeDetail(items[position].recipe)
            }
        }

        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        val favorites = dbHelper.getAllFavorites()
        if (favorites.isEmpty()) {
            favoriteList.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            favoriteList.visibility = View.VISIBLE
            emptyText.visibility = View.GONE

            val favoriteNames = favorites.map { it.name }.toSet()
            val items = favorites.map { recipe ->
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
            adapter.updateData(items)
            adapter.updateFavorites(favoriteNames)
        }
    }

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
            .setPositiveButton("关闭", null)
            .show()
    }
}
