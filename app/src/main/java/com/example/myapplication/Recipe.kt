package com.example.myapplication

import org.json.JSONObject

data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val gongyi: String,
    val kouwei: String,
    val steps: String,
    val mainIngredients: String,
    val sideIngredients: String,
    val seasonings: String,
    val cookTime: Int,
    val isCustom: Int = 0,
    val imageRes: Int = 0,
    val description: String = ""
) {
    fun getAllIngredientNames(): Set<String> {
        val result = mutableSetOf<String>()
        for (s in listOf(mainIngredients, sideIngredients, seasonings)) {
            for (part in s.split("\\s+".toRegex())) {
                val name = parseIngredientName(part.trim())
                if (name.isNotEmpty()) result.add(name)
            }
        }
        return result
    }

    companion object {
        fun parseIngredientName(raw: String): String {
            if (raw.isEmpty()) return ""
            val sb = StringBuilder()
            for (c in raw) {
                if (c.isDigit() || c == '(' || c == ')' || c == '（' || c == '）') break
                if (c in '一'..'鿿') sb.append(c)
            }
            return sb.toString()
        }

        fun fromJson(obj: JSONObject): Recipe {
            val catObj = obj.optJSONObject("" +
                    "caipu.category_1_x_caipu_id")
            var catName = ""
            if (catObj != null && catObj.length() > 0) {
                val firstKey = catObj.keys().next()
                catName = catObj.optJSONObject(firstKey)?.optString("category_1.title", "") ?: ""
            }
            val timeIndex = obj.optString("caipu.shijian_zhishu", "5").toIntOrNull() ?: 5
            return Recipe(
                id = obj.optInt("caipu.id", 0),
                name = obj.optString("caipu.title", ""),
                category = catName,
                gongyi = obj.optString("caipu.gongyi", ""),
                kouwei = obj.optString("caipu.kouwei", ""),
                steps = obj.optString("caipu.zuofa", "").replace("\\n", "\n"),
                mainIngredients = obj.optString("caipu.zhuliao", ""),
                sideIngredients = obj.optString("caipu.fuliao", ""),
                seasonings = obj.optString("caipu.tiaoliao", ""),
                cookTime = timeIndex * 10
            )
        }
    }
}
