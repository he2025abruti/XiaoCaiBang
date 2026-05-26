package com.example.myapplication

import android.content.Context
import android.util.Log

class LlmRecipeEngine(private val context: Context) {

    companion object {
        private const val TAG = "LlmRecipeEngine"
        private const val MODEL_DIR = "llm_models"
        private const val MODEL_FILE = "gemma-2b-it-q4.bin"
    }

    private var inference: Any? = null
    private var isInitialized = false
    private val downloader = ModelDownloader(context, MODEL_DIR, MODEL_FILE)

    fun isReady(): Boolean {
        return downloader.isModelExists() && tryInit()
    }

    fun getDownloader(): ModelDownloader = downloader

    private fun tryInit(): Boolean {
        if (isInitialized) return true
        return try {
            val modelPath = downloader.getModelPath()
            // TODO: Google AI Edge SDK 初始化
            // val options = LlmInference.LlmInferenceOptions.builder()
            //     .setModelPath(modelPath)
            //     .setMaxTokens(512)
            //     .build()
            // inference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "模型初始化成功: $modelPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "模型初始化失败", e)
            false
        }
    }

    fun generate(ingredients: List<String>): AiRecipeResult {
        val prompt = buildPrompt(ingredients)
        val response = callLlm(prompt)
        return parseResponse(response, ingredients)
    }

    private fun buildPrompt(ingredients: List<String>): String {
        return """你是一位中式家常菜厨师。根据以下食材，生成一道菜谱。

食材：${ingredients.joinToString("、")}

请按以下格式输出（不要有多余文字）：
菜名：xxx
分类：热菜/凉菜/汤/小吃
食材：食材1、食材2、食材3
步骤：
1. 第一步
2. 第二步
3. 第三步

要求：
- 只使用提供的食材，可补充盐、油、酱油等基础调料
- 步骤不超过5步，简洁实用
- 适合家庭快手菜"""
    }

    private fun callLlm(prompt: String): String {
        // TODO: 接入 Google AI Edge SDK
        // return inference?.generateResponse(prompt) ?: throw Exception("模型未就绪")
        throw NotImplementedError("需接入 Google AI Edge SDK")
    }

    private fun parseResponse(response: String, fallbackIngredients: List<String>): AiRecipeResult {
        val name = extractField(response, "菜名") ?: "创意菜"
        val category = extractField(response, "分类") ?: "家常菜"
        val ingredientsStr = extractField(response, "食材")
            ?: fallbackIngredients.joinToString("、")
        val steps = extractSteps(response)

        return AiRecipeResult(name, ingredientsStr, steps, category)
    }

    private fun extractField(text: String, field: String): String? {
        val regex = "$field[：:](.+)".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.trim()
    }

    private fun extractSteps(text: String): String {
        val stepLines = text.lines()
            .filter { it.trim().matches(Regex("^\\d+[.、].+")) }
        return if (stepLines.isNotEmpty()) {
            stepLines.joinToString("\n")
        } else {
            "1. 将食材洗净切好\n2. 热锅下油翻炒\n3. 加调料调味出锅"
        }
    }
}
