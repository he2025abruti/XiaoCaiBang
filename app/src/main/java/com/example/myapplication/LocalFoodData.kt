package com.example.myapplication

/**
 * 本地食材静态数据
 *
 * 所有买菜页展示的食材数据都在这里手动填写。
 * 每个食材包含：名称、季节、各地参考价格、挑选技巧、保鲜天数、拼音首字母、图片URL。
 *
 * 价格格式示例: "北京3元/斤 上海3.5元/斤 广州2.8元/斤"
 * 图片URL先留空，后续有图时填入（显示默认图标）
 */
data class LocalFoodItem(
    val name: String,
    val season: String,
    val price: String,
    val tips: String,
    val shelfLifeDays: Int,
    val pinyinLetter: String,
    val imageUrl: String = ""
)

object LocalFoodData {

    // ⬇️⬇️⬇️ 在这里手动填写食材数据，按需增删改 ⬇️⬇️⬇️
    val FOOD_LIST = listOf(
        // ── 蔬菜 ──
        LocalFoodItem("白菜", "全年", "各地约2-3元/斤", "选择叶片紧实、颜色鲜绿的", 7, "B"),
        LocalFoodItem("菠菜", "秋冬", "各地约3-5元/斤", "选择叶片深绿、无黄叶的", 3, "B"),
        LocalFoodItem("胡萝卜", "全年", "各地约2-4元/斤", "选择颜色橙红、表皮光滑的", 14, "H"),
        LocalFoodItem("黄瓜", "春夏", "各地约2-4元/斤", "选择颜色翠绿、刺粒明显的", 5, "H"),
        LocalFoodItem("韭菜", "春夏", "各地约3-5元/斤", "选择叶片宽厚、颜色深绿的", 3, "J"),
        LocalFoodItem("苦瓜", "夏秋", "各地约3-5元/斤", "选择表面颗粒饱满的", 7, "K"),
        LocalFoodItem("萝卜", "秋冬", "各地约2-3元/斤", "选择表皮光滑、掂着沉的", 14, "L"),
        LocalFoodItem("辣椒", "夏秋", "各地约3-6元/斤", "选择颜色鲜亮、表皮光滑的", 7, "L"),
        LocalFoodItem("蘑菇", "秋冬", "各地约5-8元/斤", "选择菌盖完整、无黏液的", 3, "M"),
        LocalFoodItem("木耳", "全年", "各地约8-15元/斤(干)", "选择颜色黑亮、无杂质的", 7, "M"),
        LocalFoodItem("南瓜", "秋冬", "各地约2-4元/斤", "选择表皮硬实、颜色深的", 30, "N"),
        LocalFoodItem("芹菜", "秋冬", "各地约3-5元/斤", "选择茎秆挺直、颜色翠绿的", 5, "Q"),
        LocalFoodItem("茄子", "夏秋", "各地约2-4元/斤", "选择颜色紫亮、表皮光滑的", 7, "Q"),
        LocalFoodItem("青椒", "夏秋", "各地约3-5元/斤", "选择颜色深绿、表皮光滑的", 7, "Q"),
        LocalFoodItem("山药", "秋冬", "各地约5-8元/斤", "选择表皮无损伤、掂着沉的", 14, "S"),
        LocalFoodItem("生姜", "全年", "各地约5-10元/斤", "选择表皮紧实、无发芽的", 30, "S"),
        LocalFoodItem("丝瓜", "夏秋", "各地约3-5元/斤", "选择颜色翠绿、表皮光滑的", 5, "S"),
        LocalFoodItem("土豆", "全年", "各地约2-3元/斤", "选择表皮光滑、无发芽的", 14, "T"),
        LocalFoodItem("甜椒", "夏秋", "各地约4-6元/斤", "选择颜色鲜亮、表皮光滑的", 7, "T"),
        LocalFoodItem("莴笋", "秋冬", "各地约3-5元/斤", "选择茎秆粗壮、叶片鲜绿的", 5, "W"),
        LocalFoodItem("西红柿", "夏秋", "各地约3-5元/斤", "选择颜色鲜红、手感微软的", 7, "X"),
        LocalFoodItem("西兰花", "秋冬", "各地约4-6元/斤", "选择花球紧实、颜色深绿的", 5, "X"),
        LocalFoodItem("香菇", "秋冬", "各地约6-10元/斤", "选择菌盖完整、伞未完全张开的", 3, "X"),
        LocalFoodItem("洋葱", "全年", "各地约2-4元/斤", "选择表皮干燥、无发芽的", 30, "Y"),
        LocalFoodItem("玉米", "夏秋", "各地约3-5元/根", "选择颗粒饱满、苞叶鲜绿的", 3, "Y"),
        LocalFoodItem("芋头", "秋冬", "各地约4-6元/斤", "选择表皮无损伤、掂着沉的", 14, "Y"),
        // ── 水果 ──
        LocalFoodItem("菠萝", "春夏", "各地约3-6元/斤", "选择叶片翠绿、底部有香味的", 5, "B"),
        LocalFoodItem("橙子", "秋冬", "各地约4-6元/斤", "选择表皮光滑、掂着沉的", 14, "C"),
        LocalFoodItem("甘蔗", "秋冬", "各地约2-4元/根", "选择茎秆粗壮、颜色紫黑的", 7, "G"),
        LocalFoodItem("火龙果", "夏秋", "各地约6-10元/斤", "选择颜色鲜亮、鳞片新鲜的", 5, "H"),
        LocalFoodItem("橘子", "秋冬", "各地约4-6元/斤", "选择表皮光滑、颜色橙黄的", 10, "J"),
        LocalFoodItem("蓝莓", "夏秋", "各地约15-30元/盒", "选择颜色深蓝、有白霜的", 5, "L"),
        LocalFoodItem("梨", "秋冬", "各地约3-6元/斤", "选择表皮光滑、掂着沉的", 7, "L"),
        LocalFoodItem("荔枝", "夏季", "各地约8-15元/斤", "选择颜色鲜红、外壳有弹性的", 3, "L"),
        LocalFoodItem("芒果", "夏秋", "各地约5-10元/斤", "选择颜色金黄、有香味的", 5, "M"),
        LocalFoodItem("猕猴桃", "秋冬", "各地约5-10元/斤", "选择手感微软、有弹性的", 7, "M"),
        LocalFoodItem("柠檬", "全年", "各地约3-6元/个", "选择颜色鲜黄、表皮光滑的", 14, "N"),
        LocalFoodItem("苹果", "秋冬", "各地约5-8元/斤", "选择颜色鲜亮、表皮光滑的", 14, "P"),
        LocalFoodItem("葡萄", "夏秋", "各地约5-10元/斤", "选择果粒饱满、果梗绿色的", 5, "P"),
        LocalFoodItem("桃子", "夏季", "各地约4-8元/斤", "选择颜色红润、有香味的", 5, "T"),
        LocalFoodItem("香蕉", "全年", "各地约3-5元/斤", "选择颜色金黄、无黑斑的", 5, "X"),
        LocalFoodItem("西瓜", "夏季", "各地约1.5-3元/斤", "选择花纹清晰、拍打声音清脆的", 7, "X"),
        LocalFoodItem("柚子", "秋冬", "各地约4-8元/个", "选择外形上尖下宽、掂着沉的", 14, "Y"),
        // ── 肉类 ──
        LocalFoodItem("牛肉", "全年", "各地约35-50元/斤", "选择色泽鲜红、有弹性的", 3, "N"),
        // ── 其他 ──
        LocalFoodItem("大蒜", "全年", "各地约5-8元/斤", "选择蒜头饱满、无发芽的", 30, "D"),
        LocalFoodItem("冬瓜", "夏秋", "各地约2-3元/斤", "选择表皮有白霜、掂着沉的", 14, "D"),
        LocalFoodItem("番薯", "秋冬", "各地约2-4元/斤", "选择表皮完整、无黑斑的", 14, "F")
    )
    // ⬆️⬆️⬆️ 在这里手动填写食材数据 ⬆️⬆️⬆️

    /**
     * 获取所有食材（转为 FoodItem，供 BuyFragment 使用）
     */
    fun getAllFoodItems(): List<FoodItem> {
        return FOOD_LIST.map { item ->
            FoodItem(
                name = item.name,
                imageUrl = item.imageUrl,
                price = item.price,
                season = item.season,
                tips = item.tips,
                shelfLifeDays = item.shelfLifeDays,
                pinyinLetter = item.pinyinLetter
            )
        }.sortedBy { it.pinyinLetter }
    }

    /**
     * 按名称查找食材
     */
    fun findByName(name: String): LocalFoodItem? {
        return FOOD_LIST.find { it.name == name }
    }

    /**
     * 获取保鲜天数映射表（兼容旧代码）
     */
    fun getAllShelfLifeMap(): Map<String, Int> {
        return FOOD_LIST.associate { it.name to it.shelfLifeDays }
    }

    /**
     * 获取拼音首字母
     */
    fun getPinyinLetter(name: String): String {
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
}
