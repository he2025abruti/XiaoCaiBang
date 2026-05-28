# 安卓菜谱管理APP
一款免费开源的Android食材管理&自定义菜谱应用，集成GLM-4.6V-Flash AI联网搜索匹配菜谱功能

## ✨ 功能特色
- 自定义菜谱添加/编辑弹窗
- AI联网搜索匹配：自动生成食材、步骤、简介
- 本地SQLite存储，无服务器依赖
- Material Design 3 界面，日间/夜间模式

## 🛠️ 技术栈
- 语言：Kotlin
- 架构：Fragment + 底部导航
- 存储：SQLiteOpenHelper
- AI模型：GLM-4.6V-Flash
- UI：Material Design 3

## 🚀 使用方法
1. 克隆项目到本地
2. 在对应文件中中添加你的 GLM API Key（参考必看配置）
3. 直接运行即可

## ⚠️ 必看配置：AI功能使用说明
本项目集成了 GLM-4.6V-Flash AI 联网搜索菜谱功能，使用前需要填写你自己的免费API密钥：
1. 打开文件：app/src/main/java/com/example/myapplication/GlmApiHelper.kt
2. 找到代码行：private const val GLM_API_KEY = ""
3. 在双引号内填写你的 GLM API Key
4. 重新运行项目，即可使用AI搜索功能

🔗 GLM API Key 免费申请地址：https://bigmodel.cn/

## 📄 开源协议
MIT License
