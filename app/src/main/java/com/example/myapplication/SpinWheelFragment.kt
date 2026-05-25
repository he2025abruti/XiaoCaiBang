package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SpinWheelFragment : Fragment() {

    private lateinit var spinWheel: SpinWheelView
    private lateinit var hintText: TextView
    private lateinit var backBtn: TextView

    var onBackClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spin_wheel, container, false)

        spinWheel = view.findViewById(R.id.spin_wheel)
        hintText = view.findViewById(R.id.hint_text)
        backBtn = view.findViewById(R.id.back_btn)

        backBtn.setOnClickListener {
            onBackClick?.invoke()
        }

        // 加载菜谱列表
        val allRecipes = RecipeData.RECIPE_LIST.toMutableList()

        // 也加入自定义菜谱
        try {
            val dbHelper = IngredientDatabaseHelper(requireContext())
            val customRecipes = dbHelper.getAllCustomRecipes()
            allRecipes.addAll(customRecipes)
        } catch (_: Exception) {}

        if (allRecipes.isEmpty()) {
            hintText.text = "暂无菜谱，请先添加菜谱"
            return view
        }

        spinWheel.setRecipes(allRecipes)

        spinWheel.onSpinStateChanged = { spinning ->
            hintText.text = if (spinning) "转盘旋转中...点击停止" else "点击转盘开始，再点击停止"
        }

        spinWheel.onSpinEnd = { recipe ->
            showResultDialog(recipe)
        }

        return view
    }

    private fun showResultDialog(recipe: Recipe) {
        val sb = StringBuilder()
        if (recipe.category.isNotEmpty()) sb.append("分类：${recipe.category}\n")
        if (recipe.kouwei.isNotEmpty()) sb.append("口味：${recipe.kouwei}\n")
        sb.append("烹饪时间：${recipe.cookTime}分钟")

        AlertDialog.Builder(requireContext())
            .setTitle("今天就吃这个！")
            .setMessage("${recipe.name}\n\n$sb")
            .setPositiveButton("查看菜谱详情") { _, _ ->
                showRecipeDetail(recipe)
            }
            .setNegativeButton("再来一次") { _, _ ->
                spinWheel.spin()
            }
            .setCancelable(true)
            .show()
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
