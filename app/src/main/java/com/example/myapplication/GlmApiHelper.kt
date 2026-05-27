package com.example.myapplication

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * GLM-4.6V-Flash 菜谱联网搜索
 * 纯文本调用，返回结构化菜谱信息，仅用 HttpURLConnection
 */
object
GlmApiHelper {

    private const val TAG = "GlmApiHelper"
    private const val API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
    private const val MODEL = "glm-4.6v-flash"
    private const val TIMEOUT = 30000
    private const val API_KEY = "35cf9a2481244ed89271bfe58ee7a297.HNQDC6irnfb8sviy"

    var lastError: String? = null
        private set

    data class RecipeSearchResult(
        val category: String,
        val mainIngredients: String,
        val steps: String,
        val description: String,
        val cookTime: Int
    )

    /**
     * 根据菜名/关键词搜索菜谱信息
     * @param keyword 菜名或食材关键词
     * @return 结构化菜谱信息，失败返回 null
     */
    fun searchRecipe(keyword: String): RecipeSearchResult? {
        lastError = null
        return try {
            val jsonBody = buildRequestBody(keyword)
            val response = doPost(jsonBody)
            Log.d(TAG, "API响应: $response")
            parseResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "搜索异常: ${e.message}", e)
            lastError = e.message ?: "未知错误"
            null
        }
    }

    private fun buildRequestBody(keyword: String): String {
        val prompt = """
请联网搜索"$keyword"这道菜的完整菜谱信息，严格按以下 JSON 格式返回，不要返回任何其他文字、不要用 markdown 代码块包裹：

{"category":"分类(粤菜/川菜/鲁菜/湘菜/家常菜/私家菜/汤羹/主食/甜品之一)","mainIngredients":"主要食材，用逗号分隔，如：猪肉,青椒,蒜","steps":"详细烹饪步骤，每步换行，编号1.2.3.，至少5步","description":"一句话简介，50字以内","cookTime":"烹饪时间(纯数字，单位分钟)"}

注意：
- 食材只写名称，不带用量
- 步骤要具体可操作
- 分类只能从给定选项中选一个
        """.trimIndent()

        val contentArray = JSONArray()
        val textObj = JSONObject()
        textObj.put("type", "text")
        textObj.put("text", prompt)
        contentArray.put(textObj)

        val message = JSONObject()
        message.put("role", "user")
        message.put("content", contentArray)

        val messages = JSONArray()
        messages.put(message)

        val body = JSONObject()
        body.put("model", MODEL)
        body.put("messages", messages)

        return body.toString()
    }

    private fun doPost(jsonBody: String): String {
        val url = URL(API_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $API_KEY")
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.connectTimeout = TIMEOUT
        conn.readTimeout = TIMEOUT
        conn.doOutput = true

        conn.outputStream.use { os ->
            os.write(jsonBody.toByteArray(Charsets.UTF_8))
            os.flush()
        }

        val responseCode = conn.responseCode
        Log.d(TAG, "HTTP 响应码: $responseCode")
        val stream = if (responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream
        } else {
            val errBody = conn.errorStream?.let {
                InputStreamReader(it, Charsets.UTF_8).readText()
            } ?: ""
            Log.e(TAG, "HTTP 错误: $responseCode, $errBody")
            throw Exception("HTTP 错误: $responseCode${if (errBody.isNotBlank()) " - $errBody" else ""}")
        }

        return stream.use { InputStreamReader(it, Charsets.UTF_8).readText() }
    }

    private fun parseResponse(response: String): RecipeSearchResult? {
        val json = JSONObject(response)

        // 检查 API 错误
        if (json.has("error")) {
            val error = json.getJSONObject("error")
            val msg = error.optString("message", "未知错误")
            Log.e(TAG, "GLM API 错误: $msg")
            lastError = "GLM API 错误: $msg"
            return null
        }

        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            lastError = "模型未返回结果"
            return null
        }

        val message = choices.getJSONObject(0).getJSONObject("message")
        var content = message.optString("content", "")
        if (content.isBlank()) {
            content = message.optString("reasoning_content", "")
        }
        if (content.isBlank()) {
            lastError = "模型返回内容为空"
            return null
        }

        Log.d(TAG, "模型原始返回: $content")

        // 尝试从文本中提取 JSON
        return try {
            val jsonStr = extractJson(content)
            val result = JSONObject(jsonStr)
            RecipeSearchResult(
                category = result.optString("category", "家常菜"),
                mainIngredients = result.optString("mainIngredients", ""),
                steps = result.optString("steps", ""),
                description = result.optString("description", ""),
                cookTime = result.optString("cookTime", "30").toIntOrNull() ?: 30
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析菜谱JSON失败: ${e.message}", e)
            lastError = "未找到匹配的菜谱信息，请手动输入"
            null
        }
    }

    /**
     * 从模型返回文本中提取 JSON 对象（兼容模型可能包裹 markdown 代码块的情况）
     */
    private fun extractJson(text: String): String {
        var s = text.trim()
        // 去掉可能的 ```json ... ``` 包裹
        if (s.startsWith("```")) {
            val firstNewline = s.indexOf('\n')
            if (firstNewline >= 0) s = s.substring(firstNewline + 1)
            if (s.endsWith("```")) s = s.substring(0, s.length - 3)
            s = s.trim()
        }
        // 提取第一个 { 到最后一个 }
        val start = s.indexOf('{')
        val end = s.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1)
        }
        return s
    }
}
