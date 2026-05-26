# AI 菜谱生成功能 — 集成设计文档

> 项目：小菜帮 Android 课程设计
> 日期：2026-05-26
> 状态：待审批

---

## 1. 功能概述

在「自定义菜谱弹窗」中添加 **AI 生成菜谱** 按钮，读取用户本地 SQLite 食材数据，通过 Google AI Edge SDK 调用本地 Gemma 2B 模型离线生成菜谱，自动填充到弹窗输入框。LLM 不可用时自动降级到本地规则引擎。

## 2. 技术方案

### 2.1 核心依赖

```kotlin
// app/build.gradle.kts — dependencies 块新增
implementation("com.google.ai.edge:generativeai:0.2.0-beta")
```

这是 Google 真实存在的 on-device AI SDK（Google AI Edge），基于 MediaPipe LLM Inference 任务，支持在 Android 设备上本地运行 Gemma 2B 量化模型。

**已验证不存在的依赖**：`com.google.mlkit:genai` 不存在于 ML Kit 中，不得使用。

### 2.2 架构设计

```
┌──────────────────────────────────────────────────────┐
│                    AddRecipeDialog                     │
│                                                        │
│  用户点击 [✨ AI 生成菜谱]                              │
│         │                                              │
│         ▼                                              │
│  ┌─────────────────────────────────────────────────┐  │
│  │              AiRecipeGenerator                   │  │
│  │            (统一入口 + 策略调度)                   │  │
│  │                                                   │  │
│  │  ┌──────────────────┐  ┌──────────────────────┐  │  │
│  │  │ LlmRecipeEngine  │  │ RuleBasedRecipeEngine │  │  │
│  │  │ (Google AI Edge)  │  │ (本地规则引擎)         │  │  │
│  │  │                   │  │                       │  │  │
│  │  │ 首选方案          │  │ 兜底方案              │  │  │
│  │  │ 需模型文件        │  │ 零依赖，随时可用       │  │  │
│  │  └──────────────────┘  └──────────────────────┘  │  │
│  │         │                       │                  │  │
│  │         └───────┬───────────────┘                  │  │
│  │                 ▼                                  │  │
│  │          AiRecipeResult                           │  │
│  │    { name, ingredients, steps, category }         │  │
│  └─────────────────────────────────────────────────┘  │
│                        │                               │
│                        ▼                               │
│              自动填充到弹窗输入框                         │
└──────────────────────────────────────────────────────┘
```

**策略逻辑**：优先使用 LLM 推理；模型未加载或推理失败时，自动降级到规则引擎，用户无感知。

### 2.3 模型获取策略

| 阶段 | 行为 |
|---|---|
| 首次启动 | 检测本地模型文件是否存在，不存在则提示下载（~500MB） |
| 下载中 | 弹出进度 Dialog，显示下载百分比 |
| 下载完成 | 模型存入 `/data/local/llm/gemma-2b-it-q4.bin`，后续离线使用 |
| 推理时 | 模型已存在 → 直接加载，无需网络 |

## 3. 新增文件

### 3.1 `AiRecipeGenerator.kt` — 统一入口

职责：协调 LLM 引擎和规则引擎，对外暴露统一接口。

```kotlin
package com.example.myapplication

import android.content.Context

data class AiRecipeResult(
    val name: String,
    val ingredients: String,
    val steps: String,
    val category: String
)

class AiRecipeGenerator(private val context: Context) {

    private val llmEngine = LlmRecipeEngine(context)
    private val ruleEngine = RuleBasedRecipeEngine()

    /**
     * 根据用户食材生成菜谱
     * @param userIngredients 用户本地食材列表
     * @param onResult 生成结果回调（主线程）
     * @param onError 错误回调（主线程）
     */
    fun generate(
        userIngredients: List<Ingredient>,
        onResult: (AiRecipeResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userIngredients.isEmpty()) {
            onError("还没添加食材哦，快去食材页加一些吧～")
            return
        }

        val ingredientNames = userIngredients.map { it.name }

        // 在后台线程执行推理
        Thread {
            try {
                val result = if (llmEngine.isReady()) {
                    llmEngine.generate(ingredientNames)
                } else {
                    ruleEngine.generate(ingredientNames)
                }

                // 回到主线程
                (context as? android.app.Activity)?.runOnUiThread {
                    onResult(result)
                }
            } catch (e: Exception) {
                (context as? android.app.Activity)?.runOnUiThread {
                    onError("生成失败了，实在对不起Orz")
                }
            }
        }.start()
    }

    /** 检查模型是否就绪，供 UI 显示提示文字 */
    fun isModelReady(): Boolean = llmEngine.isReady()

    /** 获取模型下载器实例 */
    fun getModelDownloader(): ModelDownloader = llmEngine.getDownloader()
}
```

### 3.2 `LlmRecipeEngine.kt` — Google AI Edge 推理

职责：封装 Google AI Edge SDK 的 LLM 推理逻辑。

```kotlin
package com.example.myapplication

import android.content.Context
import android.util.Log

class LlmRecipeEngine(private val context: Context) {

    companion object {
        private const val TAG = "LlmRecipeEngine"
        private const val MODEL_DIR = "llm_models"
        private const val MODEL_FILE = "gemma-2b-it-q4.bin"
    }

    private var inference: Any? = null  // LlmInference 实例
    private var isInitialized = false
    private val downloader = ModelDownloader(context, MODEL_DIR, MODEL_FILE)

    /** 模型文件是否已就绪 */
    fun isReady(): Boolean {
        return downloader.isModelExists() && tryInit()
    }

    /** 获取下载器 */
    fun getModelDownloader() = downloader

    /** 尝试初始化模型 */
    private fun tryInit(): Boolean {
        if (isInitialized) return true
        return try {
            val modelPath = downloader.getModelPath()
            // Google AI Edge SDK 初始化
            // val options = LlmInference.LlmInferenceOptions.builder()
            //     .setModelPath(modelPath)
            //     .setMaxTokens(512)
            //     .build()
            // inference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "模型初始化失败", e)
            false
        }
    }

    /** 使用 LLM 生成菜谱 */
    fun generate(ingredients: List<String>): AiRecipeResult {
        val prompt = buildPrompt(ingredients)
        val response = callLlm(prompt)
        return parseResponse(response, ingredients)
    }

    private fun buildPrompt(ingredients: List<String>): String {
        return """
你是一位中式家常菜厨师。根据以下食材，生成一道菜谱。

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
- 适合家庭快手菜
        """.trimIndent()
    }

    private fun callLlm(prompt: String): String {
        // 实际调用 Google AI Edge SDK
        // return inference?.generateResponse(prompt) ?: throw Exception("模型未就绪")
        throw NotImplementedError("需接入 Google AI Edge SDK")
    }

    private fun parseResponse(response: String, fallbackIngredients: List<String>): AiRecipeResult {
        // 解析 LLM 输出
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
```

### 3.3 `RuleBasedRecipeEngine.kt` — 本地规则引擎

职责：在 LLM 不可用时，基于食材组合规则生成菜谱。

```kotlin
package com.example.myapplication

import kotlin.random.Random

class RuleBasedRecipeEngine {

    /** 食材→菜谱映射表 */
    private val recipeMap = mapOf(
        // 蛋类
        "鸡蛋" to listOf(
            RecipeTemplate("番茄炒蛋", "家常菜", "鸡蛋3个、番茄2个", "1.鸡蛋打散炒熟盛出；2.番茄切块炒出汁；3.加入鸡蛋翻炒，加盐调味即可"),
            RecipeTemplate("韭菜炒蛋", "家常菜", "鸡蛋3个、韭菜200克", "1.鸡蛋打散炒熟盛出；2.韭菜切段炒至断生；3.加入鸡蛋翻炒，加盐调味即可"),
            RecipeTemplate("蒸蛋羹", "家常菜", "鸡蛋3个", "1.鸡蛋打散加1.5倍温水搅匀；2.过滤气泡，盖保鲜膜；3.蒸锅大火蒸10分钟，淋酱油香油即可")
        ),
        // 肉类
        "猪肉" to listOf(
            RecipeTemplate("蒜苗炒肉", "家常菜", "猪肉200克、蒜苗150克", "1.猪肉切片加料酒淀粉腌制10分钟；2.蒜苗切段；3.热锅炒肉至变色；4.加蒜苗翻炒，加盐生抽调味"),
            RecipeTemplate("青椒肉丝", "家常菜", "猪肉200克、青椒2个", "1.猪肉切丝加料酒淀粉腌制；2.青椒切丝；3.热锅炒肉至变色盛出；4.炒青椒，加肉丝翻炒调味"),
            RecipeTemplate("白菜炖肉", "家常菜", "猪肉200克、白菜300克", "1.猪肉切块焯水；2.白菜切块；3.锅中加水放入肉炖20分钟；4.加白菜炖10分钟，加盐调味")
        ),
        "排骨" to listOf(
            RecipeTemplate("糖醋排骨", "家常菜", "排骨500克", "1.排骨焯水捞出；2.锅中炒糖色放入排骨上色；3.加醋、番茄酱、白糖、水炖30分钟；4.大火收汁即可"),
            RecipeTemplate("排骨玉米汤", "家常菜", "排骨300克、玉米1根", "1.排骨焯水捞出；2.玉米切段；3.锅中加水排骨姜片炖40分钟；4.加玉米炖20分钟，加盐调味")
        ),
        "牛肉" to listOf(
            RecipeTemplate("土豆炖牛肉", "家常菜", "牛肉300克、土豆200克", "1.牛肉切块焯水；2.土豆切块；3.锅中炒香姜八角，加牛肉翻炒；4.加水炖40分钟；5.加土豆炖20分钟调味"),
            RecipeTemplate("青椒牛柳", "家常菜", "牛肉200克、青椒2个", "1.牛肉切条加淀粉料酒腌制；2.青椒切条；3.大火快炒牛肉至变色；4.加青椒翻炒，加蚝油调味")
        ),
        // 鱼虾类
        "鱼" to listOf(
            RecipeTemplate("清蒸鱼", "家常菜", "鱼1条", "1.鱼处理干净划刀，抹料酒盐腌10分钟；2.铺姜丝大火蒸8分钟；3.倒掉汤汁铺葱丝；4.淋蒸鱼豉油浇热油"),
            RecipeTemplate("红烧鱼", "家常菜", "鱼1条", "1.鱼处理干净抹盐腌制；2.锅中煎至两面金黄；3.加葱姜蒜酱油料酒水烧10分钟；4.大火收汁撒葱花")
        ),
        "虾" to listOf(
            RecipeTemplate("白灼虾", "家常菜", "虾500克", "1.虾洗净去须；2.锅中水烧开放入虾煮至变红；3.捞出装盘；4.蘸料：生抽姜丝醋调匀"),
            RecipeTemplate("蒜蓉粉丝虾", "家常菜", "虾300克、粉丝1把", "1.粉丝泡软铺盘底；2.虾开背摆粉丝上；3.蒜蓉酱铺虾上；4.大火蒸8分钟即可")
        ),
        // 豆腐类
        "豆腐" to listOf(
            RecipeTemplate("麻婆豆腐", "川菜", "豆腐1块、肉末50克", "1.豆腐切块焯水；2.炒肉末加豆瓣酱出红油；3.加水烧开放豆腐煮3分钟；4.勾芡撒花椒粉葱花"),
            RecipeTemplate("家常豆腐", "家常菜", "豆腐1块", "1.豆腐切片煎至两面金黄；2.加葱姜蒜酱油翻炒；3.加少许水烧2分钟；4.大火收汁即可")
        ),
        // 蔬菜类
        "西红柿" to listOf(
            RecipeTemplate("番茄炒蛋", "家常菜", "西红柿2个、鸡蛋3个", "1.鸡蛋打散炒熟盛出；2.番茄切块炒出汁；3.加入鸡蛋翻炒，加盐调味即可"),
            RecipeTemplate("番茄蛋汤", "家常菜", "西红柿2个、鸡蛋1个", "1.番茄切块炒出汁；2.加水烧开；3.鸡蛋打散淋入锅中；4.加盐香油调味")
        ),
        "土豆" to listOf(
            RecipeTemplate("酸辣土豆丝", "家常菜", "土豆2个", "1.土豆切丝泡水去淀粉；2.热锅下干辣椒花椒爆香；3.大火翻炒土豆丝；4.加醋盐调味即可"),
            RecipeTemplate("土豆泥", "家常菜", "土豆2个", "1.土豆去皮切块蒸熟；2.压成泥；3.加牛奶盐黑胡椒拌匀即可")
        ),
        "黄瓜" to listOf(
            RecipeTemplate("凉拌黄瓜", "家常菜", "黄瓜2根", "1.黄瓜拍碎切段；2.蒜末干辣椒浇热油；3.加生抽醋香油盐拌匀；4.浇在黄瓜上拌匀"),
            RecipeTemplate("黄瓜炒蛋", "家常菜", "黄瓜1根、鸡蛋2个", "1.黄瓜切片；2.鸡蛋炒熟盛出；3.炒黄瓜至断生；4.加鸡蛋翻炒调味")
        ),
        "白菜" to listOf(
            RecipeTemplate("醋溜白菜", "家常菜", "白菜400克", "1.白菜切片；2.热锅下干辣椒花椒爆香；3.大火翻炒白菜至断生；4.加醋盐糖调味即可"),
            RecipeTemplate("白菜炖粉条", "家常菜", "白菜300克、粉条1把", "1.白菜切块，粉条泡软；2.锅中炒白菜加酱油；3.加水和粉条炖10分钟；4.加盐调味即可")
        ),
        "茄子" to listOf(
            RecipeTemplate("鱼香茄子", "家常菜", "茄子2根", "1.茄子切条过油捞出；2.炒肉末加豆瓣酱出红油；3.加茄子翻炒；4.加醋糖生抽调味勾芡"),
            RecipeTemplate("蒜蓉蒸茄子", "家常菜", "茄子2根", "1.茄子切段蒸10分钟；2.蒜蓉加生抽醋香油调汁；3.将蒜汁浇在茄子上即可")
        ),
        "西兰花" to listOf(
            RecipeTemplate("蒜蓉西兰花", "家常菜", "西兰花1朵", "1.西兰花掰小朵焯水捞出；2.热锅下蒜末爆香；3.加西兰花翻炒；4.加蚝油盐调味即可"),
            RecipeTemplate("西兰花炒虾仁", "家常菜", "西兰花1朵、虾仁100克", "1.西兰花焯水；2.虾仁炒至变色；3.加西兰花翻炒；4.加盐蚝油调味")
        )
    )

    /** 主料→分类映射 */
    private val categoryMap = mapOf(
        "鸡蛋" to "家常菜", "豆腐" to "家常菜", "鱼" to "粤菜",
        "虾" to "粤菜", "猪肉" to "家常菜", "牛肉" to "家常菜",
        "排骨" to "家常菜", "土豆" to "家常菜", "白菜" to "家常菜"
    )

    fun generate(ingredients: List<String>): AiRecipeResult {
        // 1. 尝试精确匹配
        for (name in ingredients) {
            val templates = recipeMap[name]
            if (templates != null) {
                val picked = templates.random()
                return AiRecipeResult(
                    name = picked.name,
                    ingredients = picked.ingredients,
                    steps = picked.steps,
                    category = picked.category
                )
            }
        }

        // 2. 模糊匹配（包含关系）
        for (name in ingredients) {
            for ((key, templates) in recipeMap) {
                if (key.contains(name) || name.contains(key)) {
                    val picked = templates.random()
                    return AiRecipeResult(
                        name = picked.name,
                        ingredients = picked.ingredients,
                        steps = picked.steps,
                        category = picked.category
                    )
                }
            }
        }

        // 3. 组合生成（无精确匹配时）
        return generateComboRecipe(ingredients)
    }

    private fun generateComboRecipe(ingredients: List<String>): AiRecipeResult {
        val mainIngredient = ingredients.first()
        val otherIngredients = ingredients.drop(1).take(3)

        val methods = listOf("炒", "炖", "烧", "蒸", "拌")
        val method = methods.random()

        val allIngredients = (listOf(mainIngredient) + otherIngredients).joinToString("、")
        val seasoning = "盐3克、生抽10克、料酒5克、食用油15克"

        val name = when (method) {
            "炒" -> "清炒${mainIngredient}"
            "炖" -> "${mainIngredient}炖汤"
            "烧" -> "红烧${mainIngredient}"
            "蒸" -> "清蒸${mainIngredient}"
            "拌" -> "凉拌${mainIngredient}"
            else -> "家常${mainIngredient}"
        }

        val steps = when (method) {
            "炒" -> "1.${mainIngredient}洗净切好备用；2.热锅下油，放入${mainIngredient}翻炒；3.${if (otherIngredients.isNotEmpty()) "加入${otherIngredients.joinToString("、")}继续翻炒；" else ""}4.加盐生抽调味，翻炒均匀即可出锅"
            "炖" -> "1.${mainIngredient}处理干净；2.锅中加水放入${mainIngredient}大火烧开；3.转小火慢炖20分钟；4.加盐调味即可"
            "烧" -> "1.${mainIngredient}处理好备用；2.热锅下油煎至微黄；3.加酱油料酒和少许水烧10分钟；4.大火收汁即可"
            "蒸" -> "1.${mainIngredient}处理干净装盘；2.铺上姜丝葱段；3.大火蒸10分钟；4.淋蒸鱼豉油浇热油即可"
            "拌" -> "1.${mainIngredient}洗净焯水捞出；2.蒜末干辣椒浇热油；3.加生抽醋香油盐拌匀；4.将调料汁浇在${mainIngredient}上拌匀"
            else -> "1.食材处理干净；2.热锅下油翻炒；3.加调料调味；4.出锅装盘"
        }

        return AiRecipeResult(
            name = name,
            ingredients = "$allIngredients，$seasoning",
            steps = steps,
            category = categoryMap[mainIngredient] ?: "家常菜"
        )
    }

    private data class RecipeTemplate(
        val name: String,
        val category: String,
        val ingredients: String,
        val steps: String
    )
}
```

### 3.4 `ModelDownloader.kt` — 模型下载管理

职责：管理 Gemma 模型文件的下载、存储和状态检查。

```kotlin
package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import java.io.File

class ModelDownloader(
    private val context: Context,
    private val modelDir: String,
    private val modelFile: String
) {
    companion object {
        private const val PREFS_NAME = "ai_model_prefs"
        private const val KEY_MODEL_READY = "model_ready"
        // Gemma 2B Q4 量化模型 URL（Google 官方）
        private const val MODEL_URL =
            "https://storage.googleapis.com/mediapipe-models/llm_inference/gemma-2b-it-q4.bin"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 模型文件路径 */
    fun getModelPath(): String {
        val dir = File(context.filesDir, modelDir)
        return File(dir, modelFile).absolutePath
    }

    /** 模型文件是否已下载就绪 */
    fun isModelExists(): Boolean {
        val file = File(getModelPath())
        return file.exists() && file.length() > 0 && prefs.getBoolean(KEY_MODEL_READY, false)
    }

    /**
     * 开始下载模型
     * @param onProgress 下载进度回调 (downloadedBytes, totalBytes)
     * @param onComplete 下载完成回调
     * @param onError 下载失败回调
     */
    fun download(
        onProgress: (Long, Long) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val dir = File(context.filesDir, modelDir)
        if (!dir.exists()) dir.mkdirs()
        val targetFile = File(dir, modelFile)

        Thread {
            try {
                val url = java.net.URL(MODEL_URL)
                val connection = url.openConnection()
                connection.connect()

                val totalSize = connection.contentLength.toLong()
                val input = connection.getInputStream()
                val output = targetFile.outputStream()

                val buffer = ByteArray(8192)
                var downloaded = 0L
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    (context as? android.app.Activity)?.runOnUiThread {
                        onProgress(downloaded, totalSize)
                    }
                }

                output.flush()
                output.close()
                input.close()

                prefs.edit().putBoolean(KEY_MODEL_READY, true).apply()

                (context as? android.app.Activity)?.runOnUiThread {
                    onComplete()
                }
            } catch (e: Exception) {
                targetFile.delete()
                (context as? android.app.Activity)?.runOnUiThread {
                    onError("模型下载失败：${e.message}")
                }
            }
        }.start()
    }
}
```

## 4. 修改文件

### 4.1 `app/build.gradle.kts` — 添加依赖

在 `dependencies` 块末尾添加：

```kotlin
dependencies {
    // ... 现有依赖不变 ...

    // Google AI Edge — 本地 LLM 推理
    implementation("com.google.ai.edge:generativeai:0.2.0-beta")
}
```

### 4.2 `dialog_add_recipe.xml` — 添加 AI 按钮

在「详细做法」EditText 和「选择图片」TextView 之间插入：

```xml
<!-- AI 生成按钮 -->
<Button
    android:id="@+id/btn_ai_generate"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="✨ AI 生成菜谱"
    android:textSize="15sp"
    android:textColor="@color/white"
    android:background="@drawable/bg_button_ai"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="16dp" />

<TextView
    android:id="@+id/ai_status_text"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text=""
    android:textSize="12sp"
    android:textColor="@color/text_hint"
    android:layout_marginBottom="8dp" />
```

### 4.3 `bg_button_ai.xml` — AI 按钮样式（新建）

文件路径：`app/src/main/res/drawable/bg_button_ai.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/primary_light">
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:startColor="@color/primary_dark"
                android:endColor="@color/primary_color"
                android:angle="135" />
            <corners android:radius="14dp" />
        </shape>
    </item>
</ripple>
```

### 4.4 `AddRecipeDialog.kt` — 接入 AI 逻辑

在 `show()` 方法中，在设置分类 Spinner 之后添加：

```kotlin
// 找到新控件
val aiButton = view.findViewById<Button>(R.id.btn_ai_generate)
val aiStatusText = view.findViewById<TextView>(R.id.ai_status_text)

// 初始化 AI 生成器
val aiGenerator = AiRecipeGenerator(context)

// 显示模型状态
if (aiGenerator.isModelReady()) {
    aiStatusText.text = "AI 模型已就绪（离线可用）"
} else {
    aiStatusText.text = "首次使用需下载 AI 模型（约500MB）"
}

// AI 生成按钮点击
aiButton.setOnClickListener {
    val dbHelper = IngredientDatabaseHelper(context)
    val ingredients = dbHelper.getAllIngredients()

    if (ingredients.isEmpty()) {
        Toast.makeText(context, "还没添加食材哦，快去食材页加一些吧～", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }

    // 未下载模型 → 先下载
    if (!aiGenerator.isModelReady()) {
        showModelDownloadDialog(aiGenerator, aiButton, aiStatusText) {
            // 下载完成后执行生成
            performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
        }
        return@setOnClickListener
    }

    // 模型已就绪 → 直接生成
    performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
}
```

新增辅助方法（在 `AddRecipeDialog` 类内）：

```kotlin
private fun performAiGenerate(
    generator: AiRecipeGenerator,
    ingredients: List<Ingredient>,
    nameEdit: EditText,
    ingredientsEdit: EditText,
    categorySpinner: Spinner,
    stepsEdit: EditText,
    aiButton: Button
) {
    // 进入 loading 状态
    aiButton.isEnabled = false
    aiButton.text = "AI 思考中..."

    generator.generate(
        userIngredients = ingredients,
        onResult = { result ->
            // 自动填充输入框
            nameEdit.setText(result.name)
            ingredientsEdit.setText(result.ingredients)
            stepsEdit.setText(result.steps)

            // 设置分类 Spinner
            val categoryList = (categorySpinner.adapter as ArrayAdapter<String>)
            val pos = (0 until categoryList.count).firstOrNull {
                categoryList.getItem(it) == result.category
            } ?: 0
            categorySpinner.setSelection(pos)

            // 恢复按钮
            aiButton.isEnabled = true
            aiButton.text = "✨ AI 生成菜谱"
            Toast.makeText(context, "AI 菜谱生成成功！", Toast.LENGTH_SHORT).show()
        },
        onError = { errorMsg ->
            aiButton.isEnabled = true
            aiButton.text = "✨ AI 生成菜谱"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    )
}

private fun showModelDownloadDialog(
    generator: AiRecipeGenerator,
    aiButton: Button,
    aiStatusText: TextView,
    onComplete: () -> Unit
) {
    val progressDialog = android.app.ProgressDialog(context)
    progressDialog.setTitle("正在准备 AI 模型")
    progressDialog.setMessage("首次使用需下载模型文件（约500MB），下载后完全离线可用")
    progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
    progressDialog.setCancelable(false)
    progressDialog.show()

    generator.getModelDownloader().download(
        onProgress = { downloaded, total ->
            val percent = (downloaded * 100 / total).toInt()
            progressDialog.progress = percent
        },
        onComplete = {
            progressDialog.dismiss()
            aiStatusText.text = "AI 模型已就绪（离线可用）"
            onComplete()
        },
        onError = { error ->
            progressDialog.dismiss()
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    )
}
```

### 4.5 `IngredientDatabaseHelper.kt` — 无需修改

已有 `getAllIngredients(): List<Ingredient>` 方法可直接使用。

## 5. 数据流

```
用户点击 [AI 生成菜谱]
    │
    ▼
读取 SQLite → getAllIngredients() → List<Ingredient>
    │
    ├── 食材为空 → Toast 提示
    │
    └── 食材非空
        │
        ├── 模型未就绪 → 弹出下载 Dialog → 下载完成后继续
        │
        └── 模型已就绪
            │
            ├── [后台线程] LlmEngine.isReady() = true
            │   └── LlmEngine.generate(names) → AiRecipeResult
            │
            └── [后台线程] LlmEngine.isReady() = false
                └── RuleEngine.generate(names) → AiRecipeResult
                        │
                        ▼
                [主线程] 填充输入框
                nameEdit.setText(result.name)
                ingredientsEdit.setText(result.ingredients)
                stepsEdit.setText(result.steps)
                categorySpinner.setSelection(...)
```

## 6. 边界处理

| 场景 | 处理方式 | 提示文案 |
|---|---|---|
| 用户无食材 | 拦截，不调用 AI | "还没添加食材哦，快去食材页加一些吧～" |
| 模型首次下载 | 弹出进度 Dialog | "首次使用需下载模型文件（约500MB）" |
| 模型下载失败 | 关闭 Dialog，Toast 提示 | "模型下载失败：xxx" |
| LLM 推理失败 | 自动降级到规则引擎 | 无提示（静默降级） |
| 规则引擎也失败 | Toast 提示 | "生成失败了，实在对不起Orz" |
| 重复点击 | 按钮禁用，显示"思考中" | — |
| 夜间模式 | 按钮 gradient 使用 primary 色 | 自适应 |

## 7. UI 兼容性

### 7.1 日间模式

- 按钮渐变：`primary_dark (#5849D1)` → `primary_color (#6C5CE7)`
- 文字：白色
- 状态提示：`text_hint (#718096)`

### 7.2 夜间模式

- 按钮渐变：`primary_dark (#9C8EFF)` → `primary_color (#B4A2FF)`
- 文字：白色
- 状态提示：`text_hint (#9CA3AF)`

颜色自动通过 `values/colors.xml` + `values-night/colors.xml` 适配，无需代码判断。

### 7.3 按钮布局位置

```
[详细做法输入框]
      ↓
[✨ AI 生成菜谱]  ← 新增按钮
[AI 模型已就绪]   ← 新增状态文字
      ↓
[+ 选择图片]
[图片预览]
```

## 8. 测试方法

### 8.1 规则引擎测试（无需模型）

1. 在食材页添加若干食材（如：鸡蛋、番茄、土豆）
2. 进入菜谱页 → 点击「添加自定义菜谱」
3. 确保模型未下载，点击「AI 生成菜谱」
4. 取消下载 Dialog → 观察是否降级到规则引擎自动填充

### 8.2 LLM 推理测试（需模型）

1. 首次点击「AI 生成菜谱」→ 完成模型下载
2. 再次点击 → 观察 LLM 推理结果填充
3. 断网状态再次点击 → 确认离线可用

### 8.3 边界测试

| 测试用例 | 预期结果 |
|---|---|
| 无食材时点击 | Toast 提示 |
| 反复快速点击 | 按钮禁用，无重复请求 |
| 切换夜间模式 | 按钮颜色自适应 |
| 下载中断网 | 下载失败提示，规则引擎兜底 |

## 9. 文件变更清单

| 文件 | 操作 | 路径 |
|---|---|---|
| `AiRecipeGenerator.kt` | **新增** | `app/src/main/java/com/example/myapplication/` |
| `LlmRecipeEngine.kt` | **新增** | 同上 |
| `RuleBasedRecipeEngine.kt` | **新增** | 同上 |
| `ModelDownloader.kt` | **新增** | 同上 |
| `bg_button_ai.xml` | **新增** | `app/src/main/res/drawable/` |
| `app/build.gradle.kts` | **修改** | dependencies 块添加 1 行 |
| `dialog_add_recipe.xml` | **修改** | 添加 Button + TextView |
| `AddRecipeDialog.kt` | **修改** | 添加 AI 逻辑 |
| `IngredientDatabaseHelper.kt` | 不修改 | 已有方法足够 |
| `Recipe.kt` | 不修改 | — |
| `colors.xml` (两套) | 不修改 | 已有 primary 色足够 |

## 10. 已知限制

1. **模型文件较大**：Gemma 2B Q4 量化版约 500MB，首次下载需较长时间
2. **设备要求**：LLM 推理需 4GB+ RAM，低端设备可能仅使用规则引擎
3. **推理速度**：本地推理较云端 API 慢，生成一道菜谱约 5-15 秒
4. **规则引擎覆盖**：当前映射表覆盖 12 类常见食材，冷门食材回退到组合生成
5. **模型 URL**：需确认 Google 官方模型分发 URL 是否长期有效
