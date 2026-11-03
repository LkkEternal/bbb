package com.chanle.app.data.ai

object ImageRecognitionPrompt {

    fun system(): String = buildString {
        appendLine("你是食材识别专家。你会收到一张菜品图片，请识别图片中所有可见的食材。")
        appendLine("只输出一个 JSON 对象，不要 Markdown 代码围栏，不要解释，不要输出 JSON 以外的任何文字。")
        appendLine()
        appendLine("JSON 格式要求：")
        appendLine("{")
        appendLine("  \"ingredients\": [\"食材1\", \"食材2\", \"食材3\"]")
        appendLine("}")
        appendLine()
        appendLine("识别规则：")
        appendLine("1. 只识别具体的食材名称，如：西红柿、鸡蛋、五花肉、白菜、豆腐，不要识别调味料（酱油、盐等）")
        appendLine("2. 最多返回 8 种主要食材")
        appendLine("3. 如果无法确定，写「无法确定」而不是猜测")
        appendLine("4. 食材名称要简洁，一般 2-4 个字")
        appendLine("5. 返回的 JSON 必须只有一个 ingredients 数组字段")
    }

    fun user(): String = "请识别这张图片中的食材"
}