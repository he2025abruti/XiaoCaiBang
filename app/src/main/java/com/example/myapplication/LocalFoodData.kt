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
    val imageRes: Int = 0
)

object LocalFoodData {

    // ⬇️⬇️⬇️ 在这里手动填写食材数据，按需增删改 ⬇️⬇️⬇️
    val FOOD_LIST = listOf(
        // ── 蔬菜 ──
        LocalFoodItem("白菜", "全年", "各地约2-3元/斤", "选择叶片紧实、颜色鲜绿的", 7, "B", R.drawable.ic_food_baicai),
        LocalFoodItem("菠菜", "秋冬", "各地约3-5元/斤", "选择叶片深绿、无黄叶的", 3, "B", R.drawable.ic_food_bocai),
        LocalFoodItem("胡萝卜", "全年", "各地约2-4元/斤", "选择颜色橙红、表皮光滑的", 14, "H", R.drawable.ic_food_huluobo),
        LocalFoodItem("黄瓜", "春夏", "各地约2-4元/斤", "选择颜色翠绿、刺粒明显的", 5, "H", R.drawable.ic_food_huanggua),
        LocalFoodItem("韭菜", "春夏", "各地约3-5元/斤", "选择叶片宽厚、颜色深绿的", 3, "J", R.drawable.ic_food_jiucai),
        LocalFoodItem("苦瓜", "夏秋", "各地约3-5元/斤", "选择表面颗粒饱满的", 7, "K", R.drawable.ic_food_kugua),
        LocalFoodItem("萝卜", "秋冬", "各地约2-3元/斤", "选择表皮光滑、掂着沉的", 14, "L", R.drawable.ic_food_luobo),
        LocalFoodItem("辣椒", "夏秋", "各地约3-6元/斤", "选择颜色鲜亮、表皮光滑的", 7, "L", R.drawable.ic_food_lajiao),
        LocalFoodItem("蘑菇", "秋冬", "各地约5-8元/斤", "选择菌盖完整、无黏液的", 3, "M", R.drawable.ic_food_mogu),
        LocalFoodItem("木耳", "全年", "各地约8-15元/斤(干)", "选择颜色黑亮、无杂质的", 7, "M", R.drawable.ic_food_muer),
        LocalFoodItem("南瓜", "秋冬", "各地约2-4元/斤", "选择表皮硬实、颜色深的", 30, "N", R.drawable.ic_food_nangua),
        LocalFoodItem("芹菜", "秋冬", "各地约3-5元/斤", "选择茎秆挺直、颜色翠绿的", 5, "Q", R.drawable.ic_food_qincai),
        LocalFoodItem("茄子", "夏秋", "各地约2-4元/斤", "选择颜色紫亮、表皮光滑的", 7, "Q", R.drawable.ic_food_qiezi),
        LocalFoodItem("青椒", "夏秋", "各地约3-5元/斤", "选择颜色深绿、表皮光滑的", 7, "Q", R.drawable.ic_food_qingjiao),
        LocalFoodItem("山药", "秋冬", "各地约5-8元/斤", "选择表皮无损伤、掂着沉的", 14, "S", R.drawable.ic_food_shanyao),
        LocalFoodItem("生姜", "全年", "各地约5-10元/斤", "选择表皮紧实、无发芽的", 30, "S", R.drawable.ic_food_shengjiang),
        LocalFoodItem("丝瓜", "夏秋", "各地约3-5元/斤", "选择颜色翠绿、表皮光滑的", 5, "S", R.drawable.ic_food_sigua),
        LocalFoodItem("土豆", "全年", "各地约2-3元/斤", "选择表皮光滑、无发芽的", 14, "T", R.drawable.ic_food_tudou),
        LocalFoodItem("甜椒", "夏秋", "各地约4-6元/斤", "选择颜色鲜亮、表皮光滑的", 7, "T", R.drawable.ic_food_tianjiao),
        LocalFoodItem("莴笋", "秋冬", "各地约3-5元/斤", "选择茎秆粗壮、叶片鲜绿的", 5, "W", R.drawable.ic_food_wosun),
        LocalFoodItem("西红柿", "夏秋", "各地约3-5元/斤", "选择颜色鲜红、手感微软的", 7, "X", R.drawable.ic_food_xihongshi),
        LocalFoodItem("西兰花", "秋冬", "各地约4-6元/斤", "选择花球紧实、颜色深绿的", 5, "X", R.drawable.ic_food_xilanhua),
        LocalFoodItem("香菇", "秋冬", "各地约6-10元/斤", "选择菌盖完整、伞未完全张开的", 3, "X", R.drawable.ic_food_xianggu),
        LocalFoodItem("洋葱", "全年", "各地约2-4元/斤", "选择表皮干燥、无发芽的", 30, "Y", R.drawable.ic_food_yangcong),
        LocalFoodItem("玉米", "夏秋", "各地约3-5元/根", "选择颗粒饱满、苞叶鲜绿的", 3, "Y", R.drawable.ic_food_yumi),
        LocalFoodItem("芋头", "秋冬", "各地约4-6元/斤", "选择表皮无损伤、掂着沉的", 14, "Y", R.drawable.ic_food_yutou),
        // ── 水果 ──
        LocalFoodItem("菠萝", "春夏", "各地约3-6元/斤", "选择叶片翠绿、底部有香味的", 5, "B", R.drawable.ic_food_boluo),
        LocalFoodItem("橙子", "秋冬", "各地约4-6元/斤", "选择表皮光滑、掂着沉的", 14, "C", R.drawable.ic_food_chengzi),
        LocalFoodItem("甘蔗", "秋冬", "各地约2-4元/根", "选择茎秆粗壮、颜色紫黑的", 7, "G", R.drawable.ic_food_ganzhe),
        LocalFoodItem("火龙果", "夏秋", "各地约6-10元/斤", "选择颜色鲜亮、鳞片新鲜的", 5, "H", R.drawable.ic_food_huolongguo),
        LocalFoodItem("橘子", "秋冬", "各地约4-6元/斤", "选择表皮光滑、颜色橙黄的", 10, "J", R.drawable.ic_food_juzi),
        LocalFoodItem("蓝莓", "夏秋", "各地约15-30元/盒", "选择颜色深蓝、有白霜的", 5, "L", R.drawable.ic_food_lanmei),
        LocalFoodItem("梨", "秋冬", "各地约3-6元/斤", "选择表皮光滑、掂着沉的", 7, "L", R.drawable.ic_food_li),
        LocalFoodItem("荔枝", "夏季", "各地约8-15元/斤", "选择颜色鲜红、外壳有弹性的", 3, "L", R.drawable.ic_food_lizhi),
        LocalFoodItem("芒果", "夏秋", "各地约5-10元/斤", "选择颜色金黄、有香味的", 5, "M", R.drawable.ic_food_mangguo),
        LocalFoodItem("猕猴桃", "秋冬", "各地约5-10元/斤", "选择手感微软、有弹性的", 7, "M", R.drawable.ic_food_mihoutao),
        LocalFoodItem("柠檬", "全年", "各地约3-6元/个", "选择颜色鲜黄、表皮光滑的", 14, "N", R.drawable.ic_food_ningmeng),
        LocalFoodItem("苹果", "秋冬", "各地约5-8元/斤", "选择颜色鲜亮、表皮光滑的", 14, "P", R.drawable.ic_food_pingguo),
        LocalFoodItem("葡萄", "夏秋", "各地约5-10元/斤", "选择果粒饱满、果梗绿色的", 5, "P", R.drawable.ic_food_putao),
        LocalFoodItem("桃子", "夏季", "各地约4-8元/斤", "选择颜色红润、有香味的", 5, "T", R.drawable.ic_food_taozi),
        LocalFoodItem("香蕉", "全年", "各地约3-5元/斤", "选择颜色金黄、无黑斑的", 5, "X", R.drawable.ic_food_xiangjiao),
        LocalFoodItem("西瓜", "夏季", "各地约1.5-3元/斤", "选择花纹清晰、拍打声音清脆的", 7, "X", R.drawable.ic_food_xigua),
        LocalFoodItem("柚子", "秋冬", "各地约4-8元/个", "选择外形上尖下宽、掂着沉的", 14, "Y", R.drawable.ic_food_youzi),
        // ── 肉类 ──
        LocalFoodItem("牛肉", "全年", "各地约35-50元/斤", "选择色泽鲜红、有弹性的", 3, "N", R.drawable.ic_food_niurou),
        // ── 其他 ──
        LocalFoodItem("大蒜", "全年", "各地约5-8元/斤", "选择蒜头饱满、无发芽的", 30, "D", R.drawable.ic_food_dasuan),
        LocalFoodItem("冬瓜", "夏秋", "各地约2-3元/斤", "选择表皮有白霜、掂着沉的", 14, "D", R.drawable.ic_food_donggua),
        LocalFoodItem("番薯", "秋冬", "各地约2-4元/斤", "选择表皮完整、无黑斑的", 14, "F", R.drawable.ic_food_fanshu),
        // 新增40条食材数据（蔬菜+水果+肉类+菌菇+豆制品）
        LocalFoodItem("生菜", "全年", "各地约2-4元/斤", "选择叶片舒展、颜色嫩绿、无枯萎的，茎部无腐烂", 3, "S", R.drawable.ic_food_shengcai),
        LocalFoodItem("油麦菜", "全年", "各地约3-5元/斤", "选择叶片细长、颜色深绿、无黄斑的，根部无腐烂", 3, "Y", R.drawable.ic_food_youmaicai),
        LocalFoodItem("茼蒿", "秋冬", "各地约4-6元/斤", "选择株型整齐、叶片鲜绿、无异味的，茎部挺实", 2, "T", R.drawable.ic_food_tonghao),
        LocalFoodItem("苋菜", "春夏", "各地约3-5元/斤", "选择叶片肥厚、颜色紫红（红苋）或鲜绿（青苋）、无老梗的", 2, "X", R.drawable.ic_food_xiancai),
        LocalFoodItem("空心菜", "夏秋", "各地约2-4元/斤", "选择茎秆脆嫩、叶片鲜绿、无发黄的，掐断无老筋", 2, "K", R.drawable.ic_food_kongxincai),
        LocalFoodItem("娃娃菜", "全年", "各地约3-5元/斤", "选择包裹紧实、颜色嫩黄、根部无腐烂的", 7, "W", R.drawable.ic_food_wawacai),
        LocalFoodItem("西兰花苗", "秋冬", "各地约5-7元/斤", "选择茎秆纤细、叶片嫩绿、无虫害的", 3, "X", R.drawable.ic_food_xilanhuamiao),
        LocalFoodItem("菜花", "秋冬", "各地约3-5元/斤", "选择花球紧实、颜色洁白、无黑斑的，花梗粗壮", 7, "C", R.drawable.ic_food_caihua),
        LocalFoodItem("芥蓝", "秋冬", "各地约4-6元/斤", "选择茎秆粗壮、叶片鲜绿、无空心的，掐断脆嫩", 3, "J", R.drawable.ic_food_jielan),
        LocalFoodItem("菜心", "秋冬", "各地约4-6元/斤", "选择菜薹粗壮、叶片少、无开花的，颜色翠绿", 2, "C", R.drawable.ic_food_caixin),
        LocalFoodItem("豆角", "夏秋", "各地约3-6元/斤", "选择粗细均匀、颜色鲜绿、无鼓粒的，掐断无老筋", 5, "D", R.drawable.ic_food_doujiao),
        LocalFoodItem("荷兰豆", "秋冬", "各地约6-10元/斤", "选择豆荚饱满、颜色翠绿、无破损的，筋少易撕", 3, "H", R.drawable.ic_food_helandou),
        LocalFoodItem("扁豆", "夏秋", "各地约4-7元/斤", "选择荚皮光滑、颜色嫩绿、无虫眼的，掂着有分量", 5, "B", R.drawable.ic_food_biandou),
        LocalFoodItem("毛豆", "夏季", "各地约4-8元/斤", "选择豆荚饱满、颜色鲜绿、无干瘪的，捏着有弹性", 3, "M", R.drawable.ic_food_maodou),
        LocalFoodItem("莲藕", "秋冬", "各地约6-10元/斤", "选择表皮光滑、颜色淡黄、无黑斑的，掂着沉且藕节粗短", 7, "L", R.drawable.ic_food_lianou),
        LocalFoodItem("草莓", "冬春", "各地约15-30元/斤", "选择果形端正、颜色鲜红、果蒂鲜绿的，无空心无畸形", 2, "C", R.drawable.ic_food_caomei),
        LocalFoodItem("樱桃", "春夏", "各地约20-50元/斤", "选择果粒饱满、颜色深红、果梗青绿的，无破损无霉点", 2, "Y", R.drawable.ic_food_yingtao),
        LocalFoodItem("杨梅", "夏季", "各地约10-20元/斤", "选择果形饱满、颜色紫红、果蒂新鲜的，手感稍硬无软烂", 2, "Y", R.drawable.ic_food_yangmei),
        LocalFoodItem("枇杷", "春夏", "各地约8-15元/斤", "选择果皮金黄、果形端正、手感微软的，无碰伤无黑斑", 3, "P", R.drawable.ic_food_pipa),
        LocalFoodItem("山竹", "夏季", "各地约15-30元/斤", "选择果壳紫黑、手感微软有弹性的，果蒂新鲜无破损", 5, "S", R.drawable.ic_food_shanzhu),
        LocalFoodItem("榴莲", "夏季", "各地约20-40元/斤", "选择果形端正、刺尖略软、底部有香味的，摇动能听到果肉晃动", 7, "L", R.drawable.ic_food_liulian),
        LocalFoodItem("贵妃芒", "夏秋", "各地约8-15元/斤", "选择果皮红黄相间、果蒂无腐烂的，手感微软有果香", 5, "G", R.drawable.ic_food_guifeimang),
        LocalFoodItem("百香果", "夏秋", "各地约5-8元/斤", "选择果皮皱但无破损的，掂着沉且有浓郁果香", 7, "B", R.drawable.ic_food_baixiangguo),
        LocalFoodItem("圣女果", "全年", "各地约6-10元/斤", "选择颜色鲜红、果形均匀、无裂口的，手感紧实有弹性", 5, "S", R.drawable.ic_food_shengnvguo),
        LocalFoodItem("石榴", "秋冬", "各地约5-10元/斤", "选择果皮鲜红、果形饱满、掂着沉的，果嘴闭合无裂口", 14, "S", R.drawable.ic_food_shiliu),
        LocalFoodItem("五花肉", "全年", "各地约15-25元/斤", "选择肥瘦分层均匀、色泽粉红、无异味的，表皮无淤血", 3, "W", R.drawable.ic_food_wuhuarou),
        LocalFoodItem("里脊肉", "全年", "各地约18-28元/斤", "选择色泽鲜红、肉质紧实、无筋膜过多的，无渗水无异味", 3, "L", R.drawable.ic_food_lijirou),
        LocalFoodItem("羊腿肉", "全年", "各地约40-60元/斤", "选择色泽鲜红、脂肪呈白色、无膻味过重的，肉质有弹性", 3, "Y", R.drawable.ic_food_yangtuirou),
        LocalFoodItem("鸡胸肉", "全年", "各地约8-12元/斤", "选择色泽淡粉、肉质紧实、无淤血的，表皮无黏腻感", 3, "J", R.drawable.ic_food_jixiongrou),
        LocalFoodItem("鸭腿", "全年", "各地约10-15元/斤", "选择色泽暗红、肉质紧实、无腥臭味的，表皮无破损", 3, "Y", R.drawable.ic_food_yatui),
        LocalFoodItem("猪肋排", "全年", "各地约25-35元/斤", "选择骨肉相连紧实、色泽粉红、无异味的，无过多血水", 3, "Z", R.drawable.ic_food_zhuleipai),
        LocalFoodItem("金针菇", "全年", "各地约5-8元/斤", "选择菌柄细长均匀、菌盖小巧、无黏腻的，根部无腐烂", 3, "J", R.drawable.ic_food_jinzhengu),
        LocalFoodItem("蟹味菇", "全年", "各地约8-12元/斤", "选择菌盖圆润、菌柄挺直、无发黄的，包装无渗水", 3, "X", R.drawable.ic_food_xieweigu),
        LocalFoodItem("白玉菇", "全年", "各地约8-12元/斤", "选择菌盖洁白、菌柄粗壮、无霉点的，整体无异味", 3, "B", R.drawable.ic_food_baiyugu),
        LocalFoodItem("杏鲍菇", "全年", "各地约6-10元/斤", "选择菌柄粗壮、手感紧实、无空心的，表皮无破损", 7, "X", R.drawable.ic_food_xingbaogu),
        LocalFoodItem("草菇", "夏季", "各地约10-15元/斤", "选择菌盖未完全张开、颜色灰白、无黏液的，菌柄短粗", 2, "C", R.drawable.ic_food_caogu),
        LocalFoodItem("豆腐", "全年", "各地约2-4元/斤", "选择表面光滑、无蜂窝、手感紧实的，无酸臭味", 3, "D", R.drawable.ic_food_doufu),
        LocalFoodItem("千张", "全年", "各地约5-8元/斤", "选择薄厚均匀、色泽米白、无霉点的，质地柔韧无破损", 7, "Q", R.drawable.ic_food_qianzhang),
        LocalFoodItem("腐竹", "全年", "各地约15-25元/斤(干)", "选择色泽金黄、无霉斑、质地紧实的，泡发后无碎渣", 90, "F", R.drawable.ic_food_fuzhu),
        LocalFoodItem("豆干", "全年", "各地约6-10元/斤", "选择表面光滑、色泽均匀、无异味的，质地紧实有弹性", 14, "D", R.drawable.ic_food_dougan),
    )
    // ⬆️⬆️⬆️ 在这里手动填写食材数据 ⬆️⬆️⬆️

    /**
     * 获取所有食材（转为 FoodItem，供 BuyFragment 使用）
     */
    fun getAllFoodItems(): List<FoodItem> {
        return FOOD_LIST.map { item ->
            FoodItem(
                name = item.name,
                imageRes = item.imageRes,
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
