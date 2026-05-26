package com.example.myapplication

import kotlin.random.Random

class RuleBasedRecipeEngine {

    private data class RecipeTemplate(
        val name: String,
        val category: String,
        val ingredients: String,
        val steps: String
    )

    private val recipeMap = mapOf(
        "鸡蛋" to listOf(
            RecipeTemplate("番茄炒蛋", "家常菜", "鸡蛋3个、番茄2个", "1.鸡蛋打散，加少许盐搅拌均匀；\n2.番茄洗净切块；\n3.锅中放油，倒入蛋液炒至凝固盛出；\n4.锅中再放少许油，下番茄翻炒出汁；\n5.加入炒好的鸡蛋，加盐、糖翻炒均匀即可。"),
            RecipeTemplate("韭菜炒蛋", "家常菜", "鸡蛋3个、韭菜200克", "1.鸡蛋打散，加少许盐搅拌均匀；\n2.韭菜洗净切段；\n3.锅中放油，倒入蛋液炒至凝固盛出；\n4.锅中再放油，下韭菜翻炒至断生；\n5.加入鸡蛋翻炒均匀，加盐调味即可。"),
            RecipeTemplate("蒸蛋羹", "家常菜", "鸡蛋3个", "1.鸡蛋打入碗中，加少许盐搅拌均匀；\n2.加入等量温水，搅拌均匀后过筛；\n3.盖上保鲜膜，放入蒸锅；\n4.大火烧开后转小火蒸10分钟；\n5.取出后淋上香油、生抽即可。")
        ),
        "猪肉" to listOf(
            RecipeTemplate("蒜苗炒肉", "家常菜", "猪肉200克、蒜苗150克", "1.猪肉切片，加料酒、生抽、淀粉腌制10分钟；\n2.蒜苗洗净切段；\n3.锅中放油，下肉片炒至变色盛出；\n4.锅中留油，下蒜苗翻炒；\n5.加入肉片翻炒均匀，加盐、生抽调味即可。"),
            RecipeTemplate("青椒肉丝", "家常菜", "猪肉200克、青椒2个", "1.猪肉切丝，加料酒、生抽、淀粉腌制10分钟；\n2.青椒去籽切丝；\n3.锅中放油，下肉丝炒至变色盛出；\n4.锅中留油，下青椒丝翻炒；\n5.加入肉丝翻炒均匀，加盐调味即可。"),
            RecipeTemplate("白菜炖肉", "家常菜", "猪肉250克、白菜300克", "1.猪肉切块，入开水锅焯水捞出；\n2.白菜洗净切大块；\n3.锅中放油，下姜片爆香，加入猪肉翻炒；\n4.加入料酒、老抽、生抽和适量水烧开；\n5.加入白菜炖20分钟，加盐调味即可。")
        ),
        "排骨" to listOf(
            RecipeTemplate("糖醋排骨", "家常菜", "猪排骨500克", "1.排骨斩段，入开水锅焯水捞出；\n2.锅中放油，加入冰糖小火炒出糖色；\n3.放入排骨翻炒上色，加入葱、姜、料酒；\n4.加入醋、生抽和适量水，大火烧开转小火炖30分钟；\n5.大火收汁即可。"),
            RecipeTemplate("排骨玉米汤", "家常菜", "猪排骨400克、玉米1根", "1.排骨斩段，入开水锅焯水捞出；\n2.玉米切段；\n3.锅中放入排骨、姜片、料酒，加适量水；\n4.大火烧开后转小火炖40分钟；\n5.加入玉米继续炖20分钟，加盐调味即可。")
        ),
        "牛肉" to listOf(
            RecipeTemplate("土豆炖牛肉", "家常菜", "牛肉400克、土豆200克", "1.牛肉切块，入开水锅焯水捞出；\n2.土豆去皮切块；\n3.锅中放油，下洋葱炒香，加入牛肉翻炒；\n4.加入料酒、老抽、生抽和适量水烧开转小火炖40分钟；\n5.加入土豆继续炖20分钟，加盐调味即可。"),
            RecipeTemplate("青椒牛柳", "家常菜", "牛肉300克、青椒2个", "1.牛肉切条，加料酒、生抽、淀粉腌制15分钟；\n2.青椒去籽切条；\n3.锅中放油，下牛肉快速滑炒至变色盛出；\n4.锅中留油，下青椒翻炒；\n5.加入牛肉翻炒均匀，加盐、蚝油调味即可。")
        ),
        "鱼" to listOf(
            RecipeTemplate("清蒸鱼", "粤菜", "鱼1条（约500克）", "1.鱼处理干净，两面各划几刀，抹上料酒和盐腌制10分钟；\n2.鱼身铺上姜丝，放入蒸锅大火蒸8分钟；\n3.取出倒掉蒸出的水，铺上葱丝；\n4.淋上蒸鱼豉油，浇上热油即可。"),
            RecipeTemplate("红烧鱼", "家常菜", "鱼1条（约500克）", "1.鱼处理干净，两面抹上料酒和盐腌制10分钟；\n2.锅中放油，将鱼煎至两面金黄；\n3.加入葱、姜、蒜爆香，加入料酒、老抽、生抽；\n4.加入适量水，大火烧开转小火烧10分钟；\n5.大火收汁，撒上葱花即可。")
        ),
        "虾" to listOf(
            RecipeTemplate("白灼虾", "粤菜", "鲜虾500克", "1.鲜虾洗净，剪去须脚；\n2.锅中加水烧开，放入姜片、料酒；\n3.下入鲜虾焯至变红熟透捞出；\n4.调蘸料：生抽、香油、姜末拌匀；\n5.摆盘后蘸料食用即可。"),
            RecipeTemplate("蒜蓉粉丝虾", "粤菜", "鲜虾300克、粉丝1把", "1.粉丝泡软铺在盘底；\n2.鲜虾洗净去虾线，摆在粉丝上；\n3.蒜切末，锅中放油炒香蒜末；\n4.将蒜蓉铺在虾上，淋上生抽；\n5.入蒸锅大火蒸8分钟，取出撒上葱花即可。")
        ),
        "豆腐" to listOf(
            RecipeTemplate("麻婆豆腐", "川菜", "豆腐400克、猪肉末100克", "1.豆腐切小块，入开水中焯水捞出；\n2.锅中放油，下肉末炒散，加入豆瓣酱炒出红油；\n3.加入适量水，放入豆腐烧3分钟；\n4.加盐、生抽调味，水淀粉勾芡；\n5.撒上花椒粉和葱花即可。"),
            RecipeTemplate("家常豆腐", "家常菜", "豆腐400克", "1.豆腐切厚片，入油锅煎至两面金黄盛出；\n2.锅中放油，下葱、姜、蒜爆香；\n3.加入豆腐翻炒，加生抽、盐调味；\n4.加少许水烧2分钟，勾芡即可。")
        ),
        "西红柿" to listOf(
            RecipeTemplate("番茄炒蛋", "家常菜", "西红柿2个、鸡蛋3个", "1.西红柿洗净切块，鸡蛋打散加少许盐搅匀；\n2.锅中放油，倒入蛋液炒至凝固盛出；\n3.锅中再放油，下西红柿翻炒出汁；\n4.加入鸡蛋翻炒均匀，加盐、糖调味即可。"),
            RecipeTemplate("番茄蛋汤", "家常菜", "西红柿1个、鸡蛋2个", "1.西红柿洗净切块，鸡蛋打散；\n2.锅中放少许油，下西红柿翻炒出汁；\n3.加入适量水烧开；\n4.缓缓倒入蛋液，轻轻搅动形成蛋花；\n5.加盐调味，淋上香油即可。")
        ),
        "土豆" to listOf(
            RecipeTemplate("酸辣土豆丝", "家常菜", "土豆300克", "1.土豆去皮切丝，用清水浸泡去淀粉；\n2.锅中放油，下干辣椒、花椒爆香；\n3.加入土豆丝大火翻炒；\n4.加盐、醋调味，翻炒均匀即可。"),
            RecipeTemplate("土豆泥", "家常菜", "土豆400克", "1.土豆去皮切块，入蒸锅蒸熟；\n2.取出用勺子压成泥；\n3.加入少许牛奶拌匀；\n4.加盐、黑胡椒调味即可。")
        ),
        "黄瓜" to listOf(
            RecipeTemplate("凉拌黄瓜", "家常菜", "黄瓜300克", "1.黄瓜洗净拍碎切段；\n2.蒜切末，干辣椒切段；\n3.将蒜末、干辣椒放入碗中，浇上热油；\n4.加入生抽、醋、香油、盐拌匀；\n5.将调料汁浇在黄瓜上拌匀即可。"),
            RecipeTemplate("黄瓜炒蛋", "家常菜", "黄瓜1根、鸡蛋2个", "1.黄瓜洗净切片，鸡蛋打散加少许盐搅匀；\n2.锅中放油，倒入蛋液炒至凝固盛出；\n3.锅中再放油，下黄瓜翻炒；\n4.加入鸡蛋翻炒均匀，加盐调味即可。")
        ),
        "白菜" to listOf(
            RecipeTemplate("醋溜白菜", "鲁菜", "白菜400克", "1.白菜洗净切片，干辣椒切段；\n2.锅中放油，下干辣椒、花椒爆香；\n3.加入白菜大火翻炒至断生；\n4.加入醋、盐、白糖调味，翻炒均匀即可。"),
            RecipeTemplate("白菜炖粉条", "家常菜", "白菜400克、粉条100克", "1.白菜洗净切大块，粉条泡软；\n2.锅中放油，下葱、姜爆香；\n3.加入白菜翻炒至变软；\n4.加入适量水、生抽、盐烧开；\n5.放入粉条炖10分钟即可。")
        ),
        "茄子" to listOf(
            RecipeTemplate("鱼香茄子", "川菜", "茄子400克", "1.茄子切条，入油锅炸软捞出；\n2.锅中留少许油，下泡椒、蒜末、姜末炒香；\n3.加入茄子翻炒；\n4.加入醋、糖、生抽调好的汁翻炒均匀；\n5.勾芡收汁即可。"),
            RecipeTemplate("蒜蓉蒸茄子", "家常菜", "茄子300克", "1.茄子洗净切长条，摆入盘中；\n2.蒜切末，锅中放油炒香蒜末；\n3.加入生抽、蚝油拌匀；\n4.将蒜蓉汁浇在茄子上；\n5.入蒸锅大火蒸10分钟即可。")
        ),
        "西兰花" to listOf(
            RecipeTemplate("蒜蓉西兰花", "家常菜", "西兰花400克", "1.西兰花掰成小朵，入开水中焯水捞出；\n2.锅中放油，下蒜末爆香；\n3.加入西兰花翻炒，加盐、蚝油调味即可。"),
            RecipeTemplate("西兰花炒虾仁", "家常菜", "西兰花300克、虾仁150克", "1.西兰花掰成小朵焯水捞出；\n2.虾仁去虾线，加料酒、淀粉腌制10分钟；\n3.锅中放油，下虾仁滑炒至变色；\n4.加入西兰花翻炒均匀；\n5.加盐调味即可。")
        )
    )

    private val categoryMap = mapOf(
        "鸡蛋" to "家常菜", "猪肉" to "家常菜", "排骨" to "家常菜",
        "牛肉" to "家常菜", "鱼" to "粤菜", "虾" to "粤菜",
        "豆腐" to "川菜", "西红柿" to "家常菜", "土豆" to "家常菜",
        "黄瓜" to "家常菜", "白菜" to "家常菜", "茄子" to "川菜",
        "西兰花" to "家常菜"
    )

    private val cookingMethods = listOf("炒", "炖", "烧", "蒸", "拌")

    fun generate(ingredients: List<String>): AiRecipeResult {
        // Level 1: Exact match
        for (name in ingredients) {
            val templates = recipeMap[name]
            if (templates != null) {
                val picked = templates.random()
                return AiRecipeResult(picked.name, picked.ingredients, picked.steps, picked.category)
            }
        }

        // Level 2: Fuzzy match
        for (name in ingredients) {
            for ((key, templates) in recipeMap) {
                if (key.contains(name) || name.contains(key)) {
                    val picked = templates.random()
                    return AiRecipeResult(picked.name, picked.ingredients, picked.steps, picked.category)
                }
            }
        }

        // Level 3: Combo generation
        return generateComboRecipe(ingredients)
    }

    private fun generateComboRecipe(ingredients: List<String>): AiRecipeResult {
        val mainIngredient = ingredients.first()
        val otherIngredients = ingredients.drop(1).take(3)
        val method = cookingMethods.random()

        val allIngredients = (listOf(mainIngredient) + otherIngredients).joinToString("、") + "，盐3克、生抽10克、料酒5克、食用油15克"

        val name = when (method) {
            "炒" -> "清炒${mainIngredient}"
            "炖" -> "${mainIngredient}炖汤"
            "烧" -> "红烧${mainIngredient}"
            "蒸" -> "清蒸${mainIngredient}"
            "拌" -> "凉拌${mainIngredient}"
            else -> "家常${mainIngredient}"
        }

        val steps = when (method) {
            "炒" -> "1.${mainIngredient}洗净切好备用；\n2.热锅下油，放入${mainIngredient}翻炒；\n3.${if (otherIngredients.isNotEmpty()) "加入${otherIngredients.joinToString("、")}继续翻炒；\n" else ""}4.加盐生抽调味，翻炒均匀即可出锅。"
            "炖" -> "1.${mainIngredient}处理干净；\n2.锅中加水放入${mainIngredient}大火烧开；\n3.转小火慢炖20分钟；\n4.加盐调味即可。"
            "烧" -> "1.${mainIngredient}处理好备用；\n2.热锅下油煎至微黄；\n3.加酱油料酒和少许水烧10分钟；\n4.大火收汁即可。"
            "蒸" -> "1.${mainIngredient}处理干净装盘；\n2.铺上姜丝葱段；\n3.大火蒸10分钟；\n4.淋蒸鱼豉油浇热油即可。"
            "拌" -> "1.${mainIngredient}洗净焯水捞出；\n2.蒜末干辣椒浇热油；\n3.加生抽醋香油盐拌匀；\n4.将调料汁浇在${mainIngredient}上拌匀即可。"
            else -> "1.食材处理干净；\n2.热锅下油翻炒；\n3.加调料调味；\n4.出锅装盘。"
        }

        return AiRecipeResult(
            name = name,
            ingredients = allIngredients,
            steps = steps,
            category = categoryMap[mainIngredient] ?: "家常菜"
        )
    }
}
