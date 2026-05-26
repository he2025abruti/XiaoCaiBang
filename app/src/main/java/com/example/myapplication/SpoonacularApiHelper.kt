package com.example.myapplication

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Spoonacular API 封装（仅保留食材图片识别功能）
 *
 * 买菜页食材数据已迁移到 LocalFoodData.kt。
 * 本类仅用于 ImageRecognitionActivity 的拍照识别。
 */
data class FoodItem(
    val id: Int = 0,
    val name: String,
    val imageRes: Int = 0,
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
        private const val TIMEOUT = 10000

        // 英文名到中文名的映射（用于图片识别结果翻译）
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
    }

    /**
     * 通过 multipart 上传图片进行食材识别
     * @return 识别出的英文食材名，失败返回 null
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

    // ========== HTTP 工具方法 ==========

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
