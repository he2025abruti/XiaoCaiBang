package com.example.myapplication

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class FoodItem(
    val id: Int = 0,
    val name: String,
    val imageUrl: String = "",
    val price: String = "",
    val season: String = "全年",
    val tips: String = "选择外观新鲜，无损伤的",
    val shelfLifeDays: Int = 7,
    val pinyinLetter: String = ""
)

class SpoonacularApiHelper {

    companion object {
        private const val BASE_URL = "https://api.spoonacular.com"
        private const val API_KEY = "5e732cca6fcb4665ac3b120269c52fd4"
        private const val IMAGE_BASE = "https://spoonacular.com/cdn/ingredients_250x250/"
        private const val TIMEOUT = 10000

        // 本地拼音首字母映射（兜底用）
        val localPinyinMap = mapOf(
            "白菜" to "B", "菠萝" to "B",
            "橙子" to "C",
            "大蒜" to "D", "冬瓜" to "D",
            "番薯" to "F",
            "甘蔗" to "G",
            "胡萝卜" to "H", "黄瓜" to "H", "火龙果" to "H",
            "韭菜" to "J", "橘子" to "J",
            "苦瓜" to "K",
            "萝卜" to "L", "蓝莓" to "L", "辣椒" to "L", "梨" to "L", "荔枝" to "L",
            "芒果" to "M", "猕猴桃" to "M", "蘑菇" to "M", "木耳" to "M",
            "南瓜" to "N", "柠檬" to "N", "牛肉" to "N",
            "苹果" to "P", "葡萄" to "P",
            "芹菜" to "Q", "茄子" to "Q", "青椒" to "Q",
            "山药" to "S", "生姜" to "S", "丝瓜" to "S",
            "土豆" to "T", "桃子" to "T", "甜椒" to "T",
            "莴笋" to "W",
            "西红柿" to "X", "西兰花" to "X", "香蕉" to "X", "西瓜" to "X", "香菇" to "X",
            "洋葱" to "Y", "玉米" to "Y", "柚子" to "Y", "芋头" to "Y",
            "竹笋" to "Z"
        )

        // 本地保鲜天数映射（Spoonacular 不提供此数据）
        val localShelfLifeMap = mapOf(
            "白菜" to 7, "菠萝" to 5,
            "橙子" to 14,
            "大蒜" to 30, "冬瓜" to 14,
            "番薯" to 14,
            "甘蔗" to 7,
            "胡萝卜" to 14, "黄瓜" to 5, "火龙果" to 5,
            "韭菜" to 3, "橘子" to 10,
            "苦瓜" to 7,
            "萝卜" to 14, "蓝莓" to 5, "辣椒" to 7, "梨" to 7, "荔枝" to 3,
            "芒果" to 5, "猕猴桃" to 7, "蘑菇" to 3, "木耳" to 7,
            "南瓜" to 30, "柠檬" to 14, "牛肉" to 3,
            "苹果" to 14, "葡萄" to 5,
            "芹菜" to 5, "茄子" to 7, "青椒" to 7,
            "山药" to 14, "生姜" to 30, "丝瓜" to 5,
            "土豆" to 14, "桃子" to 5, "甜椒" to 7,
            "莴笋" to 5,
            "西红柿" to 7, "西兰花" to 5, "香蕉" to 5, "西瓜" to 7, "香菇" to 3,
            "洋葱" to 30, "玉米" to 3, "柚子" to 14, "芋头" to 14,
            "竹笋" to 5
        )

        // API 搜索关键词与中文类别的映射
        private val searchQueries = listOf(
            "vegetable", "fruit", "meat", "mushroom",
            "pepper", "tomato", "potato", "carrot",
            "onion", "garlic", "corn", "broccoli",
            "spinach", "lettuce", "cabbage", "cucumber",
            "eggplant", "banana", "apple", "orange",
            "lemon", "grape", "strawberry", "watermelon",
            "mango", "chicken", "pork", "beef", "fish"
        )

        // 英文名到中文名的映射
        private val enToCnMap = mapOf(
            "tomato" to "西红柿", "potato" to "土豆", "carrot" to "胡萝卜",
            "onion" to "洋葱", "garlic" to "大蒜", "corn" to "玉米",
            "broccoli" to "西兰花", "spinach" to "菠菜", "cabbage" to "白菜",
            "cucumber" to "黄瓜", "eggplant" to "茄子", "banana" to "香蕉",
            "apple" to "苹果", "orange" to "橙子", "lemon" to "柠檬",
            "grape" to "葡萄", "strawberry" to "草莓", "watermelon" to "西瓜",
            "mango" to "芒果", "chicken" to "鸡肉", "pork" to "猪肉",
            "beef" to "牛肉", "fish" to "鱼", "pepper" to "辣椒",
            "mushroom" to "蘑菇", "lettuce" to "生菜", "pineapple" to "菠萝",
            "blueberry" to "蓝莓", "pear" to "梨", "peach" to "桃子",
            "ginger" to "生姜", "celery" to "芹菜", "sweet potato" to "番薯",
            "pumpkin" to "南瓜", "radish" to "萝卜", "asparagus" to "芦笋",
            "zucchini" to "西葫芦", "cauliflower" to "花菜"
        )

        // 食材挑选技巧映射
        private val tipsMap = mapOf(
            "vegetable" to "选择颜色鲜亮、叶片饱满的，避免有黄叶或虫蛀",
            "fruit" to "选择表皮光滑、色泽均匀、手感沉甸的",
            "meat" to "选择色泽鲜红、弹性好、无异味的",
            "mushroom" to "选择菌盖完整、颜色自然、无黏液的"
        )
    }

    /**
     * 获取本地兜底数据
     */
    fun getLocalFoodItems(): List<FoodItem> {
        return localPinyinMap.map { (name, letter) ->
            FoodItem(
                name = name,
                pinyinLetter = letter,
                shelfLifeDays = localShelfLifeMap[name] ?: 7,
                imageUrl = ""
            )
        }.sortedBy { it.pinyinLetter }
    }

    /**
     * 从 API 搜索食材，返回去重后的结果
     */
    fun searchAllIngredients(): List<FoodItem> {
        val allItems = mutableListOf<FoodItem>()
        val seenNames = mutableSetOf<String>()

        for (query in searchQueries) {
            try {
                val items = searchIngredients(query, 10)
                for (item in items) {
                    val cnName = translateName(item.name)
                    if (cnName !in seenNames) {
                        seenNames.add(cnName)
                        val letter = getPinyinLetter(cnName)
                        allItems.add(item.copy(
                            name = cnName,
                            pinyinLetter = letter,
                            shelfLifeDays = localShelfLifeMap[cnName] ?: 7
                        ))
                    }
                }
            } catch (e: Exception) {
                // 单个查询失败不影响其他
            }
        }

        return allItems.sortedBy { it.pinyinLetter }
    }

    /**
     * 搜索食材 API
     */
    fun searchIngredients(query: String, number: Int = 10, offset: Int = 0): List<FoodItem> {
        val urlStr = "$BASE_URL/food/ingredients/search?" +
                "query=${URLEncoder.encode(query, "UTF-8")}" +
                "&number=$number&offset=$offset&apiKey=$API_KEY"
        val response = httpGet(urlStr) ?: return emptyList()

        val json = JSONObject(response)
        val results = json.optJSONArray("results") ?: return emptyList()

        val items = mutableListOf<FoodItem>()
        for (i in 0 until results.length()) {
            val obj = results.getJSONObject(i)
            val name = obj.optString("name", "")
            val image = obj.optString("image", "")
            val imageUrl = if (image.isNotEmpty()) "$IMAGE_BASE$image" else ""
            val cost = obj.optDouble("estimatedCost", 0.0)
            val price = if (cost > 0) "约${String.format("%.1f", cost / 100)}美元/份" else ""

            items.add(FoodItem(
                id = obj.optInt("id", 0),
                name = name,
                imageUrl = imageUrl,
                price = price
            ))
        }
        return items
    }

    /**
     * 获取食材详情
     */
    fun getIngredientDetail(id: Int): FoodItem? {
        val urlStr = "$BASE_URL/food/ingredients/$id/information?apiKey=$API_KEY"
        val response = httpGet(urlStr) ?: return null

        val json = JSONObject(response)
        val name = json.optString("name", "")
        val image = json.optString("image", "")
        val imageUrl = if (image.isNotEmpty()) {
            if (image.startsWith("http")) image else "$IMAGE_BASE$image"
        } else ""
        val cost = json.optJSONObject("estimatedCost")
        val costValue = cost?.optDouble("value", 0.0) ?: 0.0
        val price = if (costValue > 0) "约${String.format("%.0f", costValue / 100)}美分/份" else ""
        val aisle = json.optString("aisle", "")

        return FoodItem(
            id = id,
            name = name,
            imageUrl = imageUrl,
            price = price,
            tips = if (aisle.isNotEmpty()) "分类: $aisle" else "选择外观新鲜的"
        )
    }

    /**
     * 从文本中检测食材名称
     */
    fun detectFoodFromText(text: String): List<String> {
        try {
            val urlStr = "$BASE_URL/food/detect"
            val params = "text=${URLEncoder.encode(text, "UTF-8")}&apiKey=$API_KEY"
            val response = httpPost(urlStr, params) ?: return emptyList()

            val json = JSONObject(response)
            val annotations = json.optJSONArray("annotations") ?: return emptyList()

            val foods = mutableListOf<String>()
            for (i in 0 until annotations.length()) {
                val ann = annotations.getJSONObject(i)
                val annotation = ann.optString("annotation", "")
                if (annotation.isNotEmpty()) {
                    foods.add(annotation)
                }
            }
            return foods
        } catch (e: Exception) {
            return emptyList()
        }
    }

    /**
     * 通过 URL 对图片进行分类识别
     */
    fun classifyImageByUrl(imageUrl: String): Pair<String, Double>? {
        try {
            val urlStr = "$BASE_URL/food/images/classify?" +
                    "imageUrl=${URLEncoder.encode(imageUrl, "UTF-8")}&apiKey=$API_KEY"
            val response = httpGet(urlStr) ?: return null

            val json = JSONObject(response)
            val category = json.optString("category", "")
            val probability = json.optDouble("probability", 0.0)
            if (category.isNotEmpty()) return Pair(category, probability)
            return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 尝试通过 multipart 上传图片进行识别
     * 如果 API 不支持则返回 null
     */
    fun classifyImageByUpload(imageBytes: ByteArray): String? {
        try {
            val urlStr = "$BASE_URL/food/images/classify?apiKey=$API_KEY"
            val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
            val result = httpPostMultipart(urlStr, imageBytes, boundary) ?: return null

            val json = JSONObject(result)
            val category = json.optString("category", "")
            if (category.isNotEmpty()) return category
            return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 英文名翻译为中文名
     */
    fun translateName(enName: String): String {
        val lower = enName.lowercase().trim()
        return enToCnMap[lower] ?: enName
    }

    /**
     * 获取拼音首字母
     */
    fun getPinyinLetter(name: String): String {
        localPinyinMap[name]?.let { return it }
        // 对于不在映射中的名称，取第一个字符判断
        if (name.isEmpty()) return "#"
        val first = name[0]
        return when {
            first in '阿'..'熬' -> "A"
            first in '八'..'部' -> "B"
            first in '擦'..'错' -> "C"
            first in '搭'..'惰' -> "D"
            first in '鹅'..'儿' -> "E"
            first in '发'..'付' -> "F"
            first in '嘎'..'过' -> "G"
            first in '哈'..'或' -> "H"
            first in '几'..'句' -> "J"
            first in '卡'..'阔' -> "K"
            first in '垃'..'落' -> "L"
            first in '妈'..'木' -> "M"
            first in '拿'..'暖' -> "N"
            first in '哦'..'欧' -> "O"
            first in '怕'..'瀑' -> "P"
            first in '七'..'群' -> "Q"
            first in '然'..'弱' -> "R"
            first in '撒'..'所' -> "S"
            first in '他'..'褪' -> "T"
            first in '哇'..'物' -> "W"
            first in '夕'..'血' -> "X"
            first in '呀'..'运' -> "Y"
            first in '匝'..'座' -> "Z"
            else -> "#"
        }
    }

    // ========== HTTP 工具方法 ==========

    private fun httpGet(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = TIMEOUT
            conn.readTimeout = TIMEOUT

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                response
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun httpPost(urlStr: String, params: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = TIMEOUT
            conn.readTimeout = TIMEOUT
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val os: OutputStream = conn.outputStream
            os.write(params.toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                response
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun httpPostMultipart(urlStr: String, imageBytes: ByteArray, boundary: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = TIMEOUT
            conn.readTimeout = TIMEOUT
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val os = conn.outputStream
            val header = "--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n"
            os.write(header.toByteArray(Charsets.UTF_8))
            os.write(imageBytes)
            val footer = "\r\n--$boundary--\r\n"
            os.write(footer.toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()
                response
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
