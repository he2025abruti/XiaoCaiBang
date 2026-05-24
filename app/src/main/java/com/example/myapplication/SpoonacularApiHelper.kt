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

            // 蔬菜
            "tomato" to "西红柿", "potato" to "土豆", "carrot" to "胡萝卜",
            "onion" to "洋葱", "garlic" to "大蒜", "corn" to "玉米",
            "broccoli" to "西兰花", "spinach" to "菠菜", "cabbage" to "白菜",
            "cucumber" to "黄瓜", "eggplant" to "茄子",
            "pepper" to "辣椒", "bell pepper" to "甜椒", "chili pepper" to "红辣椒",
            "green pepper" to "青椒", "hot pepper" to "辣椒",
            "mushroom" to "蘑菇", "lettuce" to "生菜",
            "asparagus" to "芦笋", "zucchini" to "西葫芦", "cauliflower" to "花菜",
            "pumpkin" to "南瓜", "radish" to "萝卜", "sweet potato" to "番薯",
            "celery" to "芹菜", "ginger" to "生姜",
            "green beans" to "四季豆", "green bean" to "四季豆",
            "peas" to "豌豆", "pea" to "豌豆",
            "edamame" to "毛豆", "soybean" to "大豆",
            "kale" to "羽衣甘蓝", "bok choy" to "小白菜", "pak choi" to "小白菜",
            "chinese cabbage" to "大白菜", "napa cabbage" to "大白菜",
            "water spinach" to "空心菜", "morning glory" to "空心菜",
            "lotus root" to "莲藕", "taro" to "芋头",
            "yam" to "山药", "chinese yam" to "山药",
            "bitter melon" to "苦瓜", "bitter gourd" to "苦瓜",
            "loofah" to "丝瓜", "sponge gourd" to "丝瓜",
            "winter melon" to "冬瓜", "wax gourd" to "冬瓜",
            "bamboo shoot" to "竹笋", "bamboo shoots" to "竹笋",
            "water chestnut" to "荸荠", "bean sprout" to "豆芽",
            "bean sprouts" to "豆芽", "sprout" to "豆芽",
            "leek" to "韭菜", "chive" to "韭菜",
            "shallot" to "青葱", "scallion" to "葱",
            "chili" to "辣椒", "parsnip" to "欧洲萝卜",
            "turnip" to "芜菁", "beet" to "甜菜", "beetroot" to "甜菜根",
            "artichoke" to "朝鲜蓟", "fennel" to "茴香",
            "okra" to "秋葵", "rutabaga" to "芜菁甘蓝",
            "swiss chard" to "甜菜叶", "collard greens" to "羽衣甘蓝",
            "arugula" to "芝麻菜", "endive" to "苦苣",
            "daikon" to "白萝卜", "jicama" to "豆薯",
            // 水果
            "banana" to "香蕉", "apple" to "苹果", "orange" to "橙子",
            "lemon" to "柠檬", "grape" to "葡萄", "strawberry" to "草莓",
            "watermelon" to "西瓜", "mango" to "芒果",
            "pineapple" to "菠萝", "blueberry" to "蓝莓",
            "pear" to "梨", "peach" to "桃子",
            "cherry" to "樱桃", "cherries" to "樱桃",
            "plum" to "李子", "apricot" to "杏",
            "kiwi" to "猕猴桃", "kiwifruit" to "猕猴桃",
            "dragon fruit" to "火龙果", "pitaya" to "火龙果",
            "lychee" to "荔枝", "litchi" to "荔枝",
            "longan" to "龙眼", "rambutan" to "红毛丹",
            "pomelo" to "柚子", "grapefruit" to "葡萄柚",
            "tangerine" to "橘子", "mandarin" to "橘子",
            "mandarin orange" to "橘子", "clementine" to "小柑橘",
            "lime" to "青柠", "coconut" to "椰子",
            "papaya" to "木瓜", "guava" to "番石榴",
            "passion fruit" to "百香果", "star fruit" to "杨桃",
            "persimmon" to "柿子", "pomegranate" to "石榴",
            "fig" to "无花果", "date" to "椰枣",
            "cantaloupe" to "哈密瓜", "honeydew" to "蜜瓜",
            "cranberry" to "蔓越莓", "raspberry" to "覆盆子",
            "blackberry" to "黑莓", "gooseberry" to "醋栗",
            "jackfruit" to "菠萝蜜", "durian" to "榴莲",
            "starfruit" to "杨桃", "carambola" to "杨桃",
            "sugarcane" to "甘蔗", "cane" to "甘蔗",
            "loquat" to "枇杷", "wax apple" to "莲雾",
            // 肉类
            "chicken" to "鸡肉", "pork" to "猪肉", "beef" to "牛肉",
            "fish" to "鱼", "lamb" to "羊肉", "mutton" to "羊肉",
            "duck" to "鸭肉", "goose" to "鹅肉",
            "turkey" to "火鸡肉", "rabbit" to "兔肉",
            "shrimp" to "虾", "prawn" to "大虾",
            "crab" to "螃蟹", "lobster" to "龙虾",
            "squid" to "鱿鱼", "octopus" to "章鱼",
            "clam" to "蛤蜊", "mussel" to "贻贝",
            "oyster" to "牡蛎", "scallop" to "扇贝",
            "salmon" to "三文鱼", "tuna" to "金枪鱼",
            "cod" to "鳕鱼", "tilapia" to "罗非鱼",
            "sardine" to "沙丁鱼", "mackerel" to "鲭鱼",
            "bacon" to "培根", "ham" to "火腿",
            "sausage" to "香肠", "minced pork" to "猪肉末",
            "pork belly" to "五花肉", "pork rib" to "排骨",
            "ribs" to "排骨", "tenderloin" to "里脊",
            "chicken breast" to "鸡胸肉", "chicken thigh" to "鸡腿",
            "chicken wing" to "鸡翅", "drumstick" to "鸡腿",
            // 菌菇
            "shiitake" to "香菇", "shiitake mushroom" to "香菇",
            "enoki" to "金针菇", "enoki mushroom" to "金针菇",
            "oyster mushroom" to "平菇", "king oyster mushroom" to "杏鲍菇",
            "king trumpet" to "杏鲍菇", "porcini" to "牛肝菌",
            "truffle" to "松露", "wood ear" to "木耳",
            "black fungus" to "木耳", "cloud ear" to "木耳",
            "straw mushroom" to "草菇", "button mushroom" to "口蘑",
            "portobello" to "大褐菇", "cremini" to "小褐菇",
            // 其他
            "tofu" to "豆腐", "egg" to "鸡蛋", "eggs" to "鸡蛋",
            "rice" to "大米", "noodle" to "面条", "noodles" to "面条",
            "flour" to "面粉", "milk" to "牛奶",
            "cheese" to "奶酪", "butter" to "黄油",
            "sesame" to "芝麻", "peanut" to "花生",
            "walnut" to "核桃", "almond" to "杏仁",
            "cashew" to "腰果", "chestnut" to "栗子",
            "wheat" to "小麦", "oat" to "燕麦",
            "soy sauce" to "酱油", "vinegar" to "醋",
            "sugar" to "糖", "salt" to "盐",
            "honey" to "蜂蜜"
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
