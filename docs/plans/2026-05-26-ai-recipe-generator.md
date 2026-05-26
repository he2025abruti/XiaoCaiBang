# AI 菜谱生成功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在自定义菜谱弹窗中添加「AI 生成菜谱」按钮，读取本地食材数据，通过 Google AI Edge SDK 本地推理或规则引擎自动生成菜名/食材/步骤/分类并填充到输入框。

**Architecture:** 策略模式 — AiRecipeGenerator 统一入口，优先 LlmRecipeEngine（Google AI Edge），失败时降级 RuleBasedRecipeEngine。ModelDownloader 管理模型文件生命周期。

**Tech Stack:** Kotlin, Android SDK, Google AI Edge SDK (`com.google.ai.edge:generativeai`), SQLiteOpenHelper, AlertDialog

---

## 文件结构总览

| 文件 | 操作 | 职责 |
|---|---|---|
| `RuleBasedRecipeEngine.kt` | 新增 | 纯本地规则引擎，食材→菜谱映射 + 组合生成 |
| `AiRecipeResult.kt` | 新增 | 数据类（name, ingredients, steps, category） |
| `ModelDownloader.kt` | 新增 | 模型文件下载/存储/状态检查 |
| `LlmRecipeEngine.kt` | 新增 | Google AI Edge LLM 推理封装 |
| `AiRecipeGenerator.kt` | 新增 | 统一入口，策略调度 LLM/规则引擎 |
| `bg_button_ai.xml` | 新增 | AI 按钮蓝紫色渐变样式（含夜间模式适配） |
| `dialog_add_recipe.xml` | 修改 | 添加 AI 按钮 + 状态文字 |
| `AddRecipeDialog.kt` | 修改 | 接入 AI 生成逻辑 + 下载弹窗 |
| `app/build.gradle.kts` | 修改 | 添加 Google AI Edge 依赖 |

---

## Task 1: 数据类 + 规则引擎

独立的纯 Kotlin 类，无 Android 依赖，最先实现。

**Files:**
- Create: `app/src/main/java/com/example/myapplication/AiRecipeResult.kt`
- Create: `app/src/main/java/com/example/myapplication/RuleBasedRecipeEngine.kt`

- [ ] **Step 1: 创建 AiRecipeResult 数据类**

```kotlin
package com.example.myapplication

data class AiRecipeResult(
    val name: String,
    val ingredients: String,
    val steps: String,
    val category: String
)
```

- [ ] **Step 2: 创建 RuleBasedRecipeEngine**

完整代码见设计文档 §3.3，包含：
- `RecipeTemplate` 内部数据类
- `recipeMap`：12 类食材 → 菜谱模板映射（鸡蛋/猪肉/排骨/牛肉/鱼/虾/豆腐/西红柿/土豆/黄瓜/白菜/茄子/西兰花）
- `categoryMap`：主料→分类映射
- `generate(ingredients: List<String>): AiRecipeResult` — 三级匹配：精确 → 模糊 → 组合生成
- `generateComboRecipe()` — 无匹配时随机烹饪方式 + 模板拼装

- [ ] **Step 3: 代码审查确认**

检查项：
- 所有 `RecipeTemplate` 的 `steps` 字段使用 `\n` 分隔（非分号），与现有 `Recipe.steps` 格式一致
- `categoryMap` 中的分类值与 `RecipeData.getAllCategories()` 返回值一致（家常菜/粤菜/川菜等）
- `generate()` 方法不会抛异常（ingredients 为空时由外层 AiRecipeGenerator 拦截）

---

## Task 2: 模型下载器

**Files:**
- Create: `app/src/main/java/com/example/myapplication/ModelDownloader.kt`

- [ ] **Step 1: 创建 ModelDownloader**

完整代码见设计文档 §3.4，核心逻辑：
- `getModelPath()` — 返回 `context.filesDir/llm_models/gemma-2b-it-q4.bin`
- `isModelExists()` — 检查文件存在 + SharedPreferences 标记
- `download()` — 后台线程 HttpURLConnection 下载，回调 onProgress/onComplete/onError
- 下载失败时删除不完整文件

- [ ] **Step 2: 确认 HttpURLConnection 用法**

项目已有 `SpoonacularApiHelper.kt` 使用 HttpURLConnection，确认 ModelDownloader 的网络请求模式与其一致。关键差异：ModelDownloader 使用 `contentLength` 追踪进度。

---

## Task 3: LLM 引擎

**Files:**
- Create: `app/src/main/java/com/example/myapplication/LlmRecipeEngine.kt`

- [ ] **Step 1: 创建 LlmRecipeEngine**

完整代码见设计文档 §3.2，核心逻辑：
- 持有 `ModelDownloader` 实例
- `isReady()` — 委托 `downloader.isModelExists() && tryInit()`
- `tryInit()` — 懒加载模型，首次调用初始化 Google AI Edge SDK
- `generate(ingredients)` — 构建 prompt → 调用 LLM → 解析响应
- `buildPrompt()` — 中文厨师角色 prompt，要求输出菜名/分类/食材/步骤
- `parseResponse()` — 正则提取字段，兜底默认值
- `callLlm()` — 标记 `NotImplementedError`，待接入真实 SDK

- [ ] **Step 2: 确认 prompt 格式**

prompt 必须输出结构化文本，`parseResponse()` 的正则能正确提取：
```
菜名：xxx
分类：xxx
食材：xxx
步骤：
1. xxx
2. xxx
```

---

## Task 4: AI 生成器统一入口

**Files:**
- Create: `app/src/main/java/com/example/myapplication/AiRecipeGenerator.kt`

- [ ] **Step 1: 创建 AiRecipeGenerator**

完整代码见设计文档 §3.1，核心逻辑：
- 持有 `LlmRecipeEngine` + `RuleBasedRecipeEngine`
- `generate(userIngredients, onResult, onError)` — 空食材检查 → 后台线程推理 → 主线程回调
- `isModelReady()` — 委托 llmEngine
- `getModelDownloader()` — 委托 llmEngine
- `runOnUiThread` 通过 `context as? Activity` 获取

- [ ] **Step 2: 确认线程安全**

- `generate()` 由 UI 线程调用
- 内部 `Thread { }.start()` 执行推理
- 回调通过 `runOnUiThread` 回到主线程
- 不使用协程（项目未引入 coroutines 依赖）

---

## Task 5: UI 按钮样式

**Files:**
- Create: `app/src/main/res/drawable/bg_button_ai.xml`

- [ ] **Step 1: 创建按钮背景 drawable**

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

- 日间模式：`#5849D1` → `#6C5CE7`
- 夜间模式：`#9C8EFF` → `#B4A2FF`
- 颜色自动适配，引用 `@color/primary_dark` 和 `@color/primary_color`

- [ ] **Step 2: 与现有 drawable 风格一致**

参考 `bg_button_primary.xml` 和 `bg_fab.xml`，确认圆角半径 14dp 与项目其他按钮一致。

---

## Task 6: 布局修改

**Files:**
- Modify: `app/src/main/res/layout/dialog_add_recipe.xml`

- [ ] **Step 1: 在详细做法和选择图片之间插入 AI 按钮**

在 `<EditText android:id="@+id/recipe_steps" .../>` 和 `<TextView android:id="@+id/recipe_select_image" .../>` 之间插入：

```xml
<!-- AI 生成按钮 -->
<Button
    android:id="@+id/btn_ai_generate"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="AI 生成菜谱"
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

- [ ] **Step 2: 验证布局层级**

确认新控件在 `LinearLayout` 内，顺序为：菜名 → 简介 → 食材 → 分类 → 做法 → **AI按钮** → **状态文字** → 选择图片 → 图片预览。

---

## Task 7: 弹窗逻辑集成

**Files:**
- Modify: `app/src/main/java/com/example/myapplication/AddRecipeDialog.kt`

- [ ] **Step 1: 添加 import**

```kotlin
import android.widget.Button
import android.widget.ArrayAdapter
```

`Button` 和 `ArrayAdapter` 可能已导入（检查现有 import）。`ProgressDialog` 使用全限定名 `android.app.ProgressDialog`，无需 import。

- [ ] **Step 2: 在 show() 方法中添加 AI 控件初始化**

在 `categorySpinner.adapter = spinnerAdapter` 之后、`selectImageText.visibility = View.GONE` 之前，插入：

```kotlin
val aiButton = view.findViewById<Button>(R.id.btn_ai_generate)
val aiStatusText = view.findViewById<TextView>(R.id.ai_status_text)

val aiGenerator = AiRecipeGenerator(context)

if (aiGenerator.isModelReady()) {
    aiStatusText.text = "AI 模型已就绪（离线可用）"
} else {
    aiStatusText.text = "首次使用需下载 AI 模型（约500MB）"
}

aiButton.setOnClickListener {
    val dbHelper = IngredientDatabaseHelper(context)
    val ingredients = dbHelper.getAllIngredients()

    if (ingredients.isEmpty()) {
        Toast.makeText(context, "还没添加食材哦，快去食材页加一些吧～", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
    }

    if (!aiGenerator.isModelReady()) {
        showModelDownloadDialog(aiGenerator, aiButton, aiStatusText) {
            performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
        }
        return@setOnClickListener
    }

    performAiGenerate(aiGenerator, ingredients, nameEdit, ingredientsEdit, categorySpinner, stepsEdit, aiButton)
}
```

- [ ] **Step 3: 添加 performAiGenerate 私有方法**

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
    aiButton.isEnabled = false
    aiButton.text = "AI 思考中..."

    generator.generate(
        userIngredients = ingredients,
        onResult = { result ->
            nameEdit.setText(result.name)
            ingredientsEdit.setText(result.ingredients)
            stepsEdit.setText(result.steps)

            val categoryList = (categorySpinner.adapter as ArrayAdapter<String>)
            val pos = (0 until categoryList.count).firstOrNull {
                categoryList.getItem(it) == result.category
            } ?: 0
            categorySpinner.setSelection(pos)

            aiButton.isEnabled = true
            aiButton.text = "AI 生成菜谱"
            Toast.makeText(context, "AI 菜谱生成成功！", Toast.LENGTH_SHORT).show()
        },
        onError = { errorMsg ->
            aiButton.isEnabled = true
            aiButton.text = "AI 生成菜谱"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    )
}
```

- [ ] **Step 4: 添加 showModelDownloadDialog 私有方法**

```kotlin
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

- [ ] **Step 5: 确认不破坏现有逻辑**

检查项：
- `nameEdit`/`descEdit`/`ingredientsEdit`/`categorySpinner`/`stepsEdit` 变量在 AI 代码之前已声明
- `selectImageText.visibility = View.GONE` 在 AI 代码之后执行，不影响 AI 按钮
- 正向按钮（保存）逻辑不受 AI 代码影响

---

## Task 8: 构建配置

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 添加 Google AI Edge 依赖**

在 `dependencies` 块末尾添加：

```kotlin
    // Google AI Edge — 本地 LLM 推理
    implementation("com.google.ai.edge:generativeai:0.2.0-beta")
```

- [ ] **Step 2: 同步 Gradle**

运行 `./gradlew :app:dependencies` 或在 Android Studio 中点击 Sync Now，确认依赖解析成功。

---

## 功能验证清单

全部代码完成后，按以下顺序验证：

### 验证 1: 规则引擎（无需模型）

1. 在食材页添加食材（鸡蛋、番茄、土豆）
2. 菜谱页 → 添加自定义菜谱
3. 点击 AI 生成菜谱按钮
4. 如弹出模型下载 Dialog → 取消
5. 观察：规则引擎自动填充菜名/食材/步骤/分类到输入框

### 验证 2: 无食材拦截

1. 清空食材页所有食材
2. 菜谱页 → 添加自定义菜谱 → 点击 AI 按钮
3. 预期：Toast "还没添加食材哦，快去食材页加一些吧～"

### 验证 3: 按钮状态

1. 点击 AI 按钮后
2. 预期：按钮文字变为"AI 思考中..."，按钮不可再次点击
3. 生成完成后：按钮恢复"AI 生成菜谱"，可再次点击

### 验证 4: 夜间模式

1. 我的 → 开启夜间模式
2. 菜谱页 → 添加自定义菜谱
3. 预期：AI 按钮渐变色自动变为夜间配色

### 验证 5: 现有功能无回归

1. 手动填写菜谱 → 保存 → 确认正常保存
2. 取消弹窗 → 确认正常关闭
3. 图片选择入口 → 确认仍为隐藏状态
