package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
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
    private val onRecipeAdded: (Recipe) -> Unit
) {

    private var selectedImageUri: Uri? = null

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_recipe, null)

        val nameEdit = view.findViewById<EditText>(R.id.recipe_name)
        val descEdit = view.findViewById<EditText>(R.id.recipe_description)
        val ingredientsEdit = view.findViewById<EditText>(R.id.recipe_ingredients)
        val categorySpinner = view.findViewById<Spinner>(R.id.recipe_category_spinner)
        val stepsEdit = view.findViewById<EditText>(R.id.recipe_steps)
        val selectImageText = view.findViewById<TextView>(R.id.recipe_select_image)
        val imagePreview = view.findViewById<ImageView>(R.id.recipe_image_preview)

        // 设置分类下拉（去掉"全部"选项）
        val categoryList = categories.filter { it != "全部" }
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // AI 生成按钮
        val aiButton = view.findViewById<Button>(R.id.btn_ai_generate)
        val aiStatusText = view.findViewById<TextView>(R.id.ai_status_text)
        val aiGenerator = AiRecipeGenerator(context)

        if (aiGenerator.isModelReady()) {
            aiStatusText.text = "AI 模型已就绪（离线可用）"
        } else {
            aiStatusText.text = "首次使用需下载 AI 模型（约500MB）"
        }

        aiButton.setOnClickListener {
            val dbHelper = IngredientDatabaseHelper(context)
            val ingredients = dbHelper.getAllIngredients()

            if (ingredients.isEmpty()) {
                Toast.makeText(context, "还没添加食材哦，快去食材页加一些吧～", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!aiGenerator.isModelReady()) {
                showModelDownloadDialog(aiGenerator, aiButton, aiStatusText) {
                    performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
                }
                return@setOnClickListener
            }

            performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
        }

        // 图片选择（暂不实现，预留入口）
        selectImageText.visibility = View.GONE
        imagePreview.visibility = View.GONE

        val dialog = AlertDialog.Builder(context)
            .setTitle("添加自定义菜谱")
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

            val recipe = Recipe(
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
                isCustom = true,
                imageUrl = "",
                description = desc
            )

            onRecipeAdded(recipe)
            dialog.dismiss()
        }
    }

    private fun performAiGenerate(
        generator: AiRecipeGenerator,
        ingredients: List<Ingredient>,
        nameEdit: EditText,
        ingredientsEdit: EditText,
        categorySpinner: Spinner,
        stepsEdit: EditText,
        aiButton: Button
    ) {
        aiButton.isEnabled = false
        aiButton.text = "AI 思考中..."

        generator.generate(
            userIngredients = ingredients,
            onResult = { result ->
                nameEdit.setText(result.name)
                ingredientsEdit.setText(result.ingredients)
                stepsEdit.setText(result.steps)

                val categoryList = (categorySpinner.adapter as ArrayAdapter<String>)
                val pos = (0 until categoryList.count).firstOrNull {
                    categoryList.getItem(it) == result.category
                } ?: 0
                categorySpinner.setSelection(pos)

                aiButton.isEnabled = true
                aiButton.text = "AI 生成菜谱"
                Toast.makeText(context, "AI 菜谱生成成功！", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMsg ->
                aiButton.isEnabled = true
                aiButton.text = "AI 生成菜谱"
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showModelDownloadDialog(
        generator: AiRecipeGenerator,
        aiButton: Button,
        aiStatusText: TextView,
        onComplete: () -> Unit
    ) {
        val progressDialog = android.app.ProgressDialog(context)
        progressDialog.setTitle("正在准备 AI 模型")
        progressDialog.setMessage("首次使用需下载模型文件（约500MB），下载后完全离线可用")
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        progressDialog.show()

        generator.getModelDownloader().download(
            onProgress = { downloaded, total ->
                val percent = (downloaded * 100 / total).toInt()
                progressDialog.progress = percent
            },
            onComplete = {
                progressDialog.dismiss()
                aiStatusText.text = "AI 模型已就绪（离线可用）"
                onComplete()
            },
            onError = { error ->
                progressDialog.dismiss()
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
