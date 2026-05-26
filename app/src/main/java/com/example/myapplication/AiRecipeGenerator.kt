package com.example.myapplication

import android.app.Activity
import android.content.Context

class AiRecipeGenerator(private val context: Context) {

    private val llmEngine = LlmRecipeEngine(context)
    private val ruleEngine = RuleBasedRecipeEngine()

    fun generate(
        userIngredients: List<Ingredient>,
        onResult: (AiRecipeResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userIngredients.isEmpty()) {
            onError("还没添加食材哦，快去食材页加一些吧～")
            return
        }

        val ingredientNames = userIngredients.map { it.name }

        Thread {
            try {
                // 当前 LLM 引擎为占位，始终使用规则引擎
                val result = ruleEngine.generate(ingredientNames)

                (context as? Activity)?.runOnUiThread {
                    onResult(result)
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    onError("生成失败了，实在对不起Orz")
                }
            }
        }.start()
    }

    // 规则引擎始终可用，无需下载模型
    fun isModelReady(): Boolean = true
}
