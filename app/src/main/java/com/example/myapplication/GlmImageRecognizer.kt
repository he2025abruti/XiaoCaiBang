package com.example.myapplication

import android.graphics.Bitmap
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64

/**
 * GLM-4.6V-Flash 免费多模态图片食材识别
 * 使用原生 HttpURLConnection + Base64，无第三方 SDK
 */
object GlmImageRecognizer {

    private const val TAG = "GlmImageRecognizer"
    private const val API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
    private const val MODEL = "glm-4.6v-flash"
    private const val TIMEOUT = 30000

    // 在此配置 API Key，从 GLM大模型/API key.txt 获取
    private const val API_KEY = "35cf9a2481244ed89271bfe58ee7a297.HNQDC6irnfb8sviy"

    private const val PROMPT =
        "识别图中的食材，只返回中文名称，一次识别只返回一个食材的相关信息，" +
        "如果食材界面有就对应弹出，如果没有可以帮忙联网搜索对应的挑选技巧和相应地区参考价格"

    /** 最近一次错误信息，方便 UI 层显示具体原因 */
    var lastError: String? = null
        private set

    /**
     * 识别图片中的食材
     * @param bitmap 要识别的图片
     * @return 识别结果文本（食材名+相关信息），失败返回 null
     */
    fun recognize(bitmap: Bitmap): String? {
        lastError = null
        return try {
            val base64 = bitmapToBase64(bitmap)
            Log.d(TAG, "图片转Base64完成，长度=${base64.length}")
            val jsonBody = buildRequestBody(base64)
            val response = doPost(jsonBody)
            Log.d(TAG, "API响应: $response")
            parseResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "识别异常: ${e.message}", e)
            lastError = e.message ?: "未知错误"
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // 限制最大尺寸 800px，压缩为 JPEG quality 80
        val maxDim = maxOf(bitmap.width, bitmap.height)
        val scaled = if (maxDim > 800) {
            val scale = 800f / maxDim
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }

    private fun buildRequestBody(base64Image: String): String {
        val contentArray = JSONArray()

        // 图片
        val imageObj = JSONObject()
        imageObj.put("type", "image_url")
        val imageUrl = JSONObject()
        imageUrl.put("url", base64Image)
        imageObj.put("image_url", imageUrl)
        contentArray.put(imageObj)

        // 文本提示
        val textObj = JSONObject()
        textObj.put("type", "text")
        textObj.put("text", PROMPT)
        contentArray.put(textObj)

        // message
        val message = JSONObject()
        message.put("role", "user")
        message.put("content", contentArray)

        val messages = JSONArray()
        messages.put(message)

        // request body
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

    private fun parseResponse(response: String): String? {
        return try {
            val json = JSONObject(response)

            // 检查 API 返回的错误
            if (json.has("error")) {
                val error = json.getJSONObject("error")
                val msg = error.optString("message", "未知错误")
                Log.e(TAG, "GLM API 错误: $msg")
                lastError = "GLM API 错误: $msg"
                return null
            }

            val choices = json.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                Log.w(TAG, "响应中没有 choices")
                lastError = "模型未返回识别结果"
                return null
            }

            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.optString("content", "")

            // 如果 content 为空，尝试提取 reasoning_content（思考模式返回）
            if (content.isBlank()) {
                val reasoning = message.optString("reasoning_content", "")
                if (reasoning.isNotBlank()) reasoning.trim() else null
            } else {
                content.trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析响应异常: ${e.message}", e)
            lastError = "解析响应失败: ${e.message}"
            null
        }
    }
}
