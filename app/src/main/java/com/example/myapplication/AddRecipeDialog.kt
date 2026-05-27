package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class AddRecipeDialog(
    private val context: Context,
    private val categories: List<String>,
    private val existingRecipe: Recipe? = null,
    private val onRecipeSaved: (Recipe) -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_recipe, null)

        val nameEdit = view.findViewById<EditText>(R.id.recipe_name)
        val descEdit = view.findViewById<EditText>(R.id.recipe_description)
        val ingredientsEdit = view.findViewById<EditText>(R.id.recipe_ingredients)
        val categorySpinner = view.findViewById<Spinner>(R.id.recipe_category_spinner)
        val stepsEdit = view.findViewById<EditText>(R.id.recipe_steps)
        val selectImageText = view.findViewById<TextView>(R.id.recipe_select_image)
        val imagePreview = view.findViewById<ImageView>(R.id.recipe_image_preview)
        val btnAiSearch = view.findViewById<Button>(R.id.btn_ai_search)

        // 设置分类下拉（去掉"全部"选项）
        val categoryList = categories.filter { it != "全部" }
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // 图片选择（暂不实现，预留入口）
        selectImageText.visibility = View.GONE
        imagePreview.visibility = View.GONE

        // 编辑模式：预填数据
        val isEdit = existingRecipe != null
        if (isEdit) {
            nameEdit.setText(existingRecipe!!.name)
            descEdit.setText(existingRecipe.description)
            ingredientsEdit.setText(existingRecipe.mainIngredients)
            stepsEdit.setText(existingRecipe.steps)
            val catIndex = categoryList.indexOf(existingRecipe.category)
            if (catIndex >= 0) categorySpinner.setSelection(catIndex)
        }

        // AI 联网搜索按钮
        btnAiSearch.setOnClickListener {
            val keyword = nameEdit.text.toString().trim()
            if (keyword.isEmpty()) {
                Toast.makeText(context, "请先输入菜名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "正在AI搜索菜谱...", Toast.LENGTH_SHORT).show()
            btnAiSearch.isEnabled = false
            btnAiSearch.text = "搜索中..."

            Thread {
                val result = GlmApiHelper.searchRecipe(keyword)

                handler.post {
                    btnAiSearch.isEnabled = true
                    btnAiSearch.text = "AI 联网搜索匹配"

                    if (result != null) {
                        // 只回填空字段，不覆盖用户已输入内容
                        if (descEdit.text.isNullOrBlank()) {
                            descEdit.setText(result.description)
                        }
                        if (ingredientsEdit.text.isNullOrBlank()) {
                            ingredientsEdit.setText(result.mainIngredients)
                        }
                        if (stepsEdit.text.isNullOrBlank()) {
                            stepsEdit.setText(result.steps)
                        }
                        // 匹配分类下拉
                        val catIdx = categoryList.indexOf(result.category)
                        if (catIdx >= 0) {
                            categorySpinner.setSelection(catIdx)
                        }
                        Toast.makeText(context, "搜索完成，已自动回填", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = GlmApiHelper.lastError ?: "请检查网络连接"
                        Toast.makeText(context, "搜索失败: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(if (isEdit) "编辑菜谱" else "添加自定义菜谱")
            .setView(view)
            .setPositiveButton("保存", null)
            .setNegativeButton("取消", null)
            .create()

        dialog.show()

        // 重写 positive 按钮点击，防止自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val desc = descEdit.text.toString().trim()
            val ingredientsStr = ingredientsEdit.text.toString().trim()
            val category = if (categorySpinner.selectedItem != null) {
                categorySpinner.selectedItem.toString()
            } else {
                "家常菜"
            }
            val steps = stepsEdit.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(context, "请输入菜名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ingredientsStr.isEmpty()) {
                Toast.makeText(context, "请输入食材清单", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (steps.isEmpty()) {
                Toast.makeText(context, "请输入详细做法", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recipe = if (isEdit) {
                existingRecipe.copy(
                    name = name,
                    category = category,
                    steps = steps,
                    mainIngredients = ingredientsStr,
                    description = desc
                )
            } else {
                Recipe(
                    id = System.currentTimeMillis().toInt(),
                    name = name,
                    category = category,
                    gongyi = "",
                    kouwei = "",
                    steps = steps,
                    mainIngredients = ingredientsStr,
                    sideIngredients = "",
                    seasonings = "",
                    cookTime = 30,
                    isCustom = 1,
                    description = desc
                )
            }

            onRecipeSaved(recipe)
            dialog.dismiss()
        }
    }
}
