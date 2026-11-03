package com.chanle.app.data.ai

import com.chanle.app.data.Dish
import com.chanle.app.data.DishCategory
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

object RecipeParser {

    fun parse(raw: String, model: String, prompt: String): Result<Dish> = runCatching {
        val jsonText = extractJsonObject(raw) ?: error("模型返回格式异常：没有找到 JSON")
        val root = Json.parseToJsonElement(jsonText).jsonObject

        root["error"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
            error("AI 没能生成菜谱：$it")
        }

        val name = root.string("name").ifBlank { error("模型没有返回菜名") }
        val category = normalizeCategory(root.string("category"), name)
        val description = root.string("description")
        val timeMinutes = root.int("timeMinutes").let { if (it in 1..600) it else 30 }
        val difficulty = root.string("difficulty").ifBlank { "中等" }

        val ingredients = parseIngredients(root["ingredients"])
        if (ingredients.size < 2) error("模型返回的食材太少")

        val steps = root.stringList("steps").filter { it.isNotBlank() }
        if (steps.size < 2) error("模型返回的步骤太少")

        val tips = root.stringList("tips").filter { it.isNotBlank() }
        val tags = (root.stringList("tags") + category).distinct().take(6)
        val nutrition = parseNutrition(root["nutritionPerServing"])
        val providedNote = root.string("nutritionNote").ifBlank { "AI 估算，仅供参考" }

        val warnings = mutableListOf<String>()
        val expectedKcal = nutrition.protein * 4 + nutrition.carbs * 4 + nutrition.fat * 9
        if (nutrition.kcal > 0 && abs(expectedKcal - nutrition.kcal) / nutrition.kcal > 0.35) {
            warnings += "营养数值偏差较大，仅供参考"
        }
        val note = buildString {
            append(providedNote)
            if (!contains("AI")) append(" · AI 估算")
            if (warnings.isNotEmpty()) append("（").append(warnings.joinToString("；")).append("）")
        }

        Dish(
            id = "ai-" + UUID.randomUUID(),
            name = name,
            category = category,
            description = description,
            baseServings = 2,
            timeMinutes = timeMinutes,
            difficulty = difficulty,
            ingredients = ingredients,
            steps = steps,
            tips = tips,
            tags = tags,
            nutritionPerServing = nutrition,
            nutritionNote = note,
            source = "ai",
            createdAt = System.currentTimeMillis(),
            model = model,
            prompt = prompt,
        )
    }

    private fun parseIngredients(element: JsonElement?): List<Ingredient> {
        val array = element?.let { runCatching { it.jsonArray }.getOrNull() } ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = runCatching { item.jsonObject }.getOrNull() ?: return@mapNotNull null
            val name = obj.string("name")
            if (name.isBlank()) return@mapNotNull null
            val rawUnit = obj.string("unit").ifBlank { "适量" }
            var amount = obj["amount"]?.let { parseDouble(it) }
            var unit = normalizeUnit(rawUnit)
            if (amount != null && unit == "千克") {
                amount *= 1000
                unit = "克"
            }
            if (amount != null && unit == "升") {
                amount *= 1000
                unit = "毫升"
            }
            if (amount != null && amount <= 0) amount = null
            val scalable = obj["scalable"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()
                ?: (amount != null)
            Ingredient(
                name = name,
                amount = amount,
                unit = unit,
                note = obj.string("note"),
                scalable = scalable && amount != null,
            )
        }
    }

    private fun parseNutrition(element: JsonElement?): Nutrition {
        val obj = element?.let { runCatching { it.jsonObject }.getOrNull() }
            ?: error("模型没有返回营养数据")
        val kcal = parseDouble(obj["kcal"])?.roundToInt() ?: error("营养数据缺少热量")
        val protein = parseDouble(obj["protein"]) ?: error("营养数据缺少蛋白质")
        val fat = parseDouble(obj["fat"]) ?: error("营养数据缺少脂肪")
        val carbs = parseDouble(obj["carbs"]) ?: error("营养数据缺少碳水")
        if (kcal !in 0..2000 || protein !in 0.0..300.0 || fat !in 0.0..300.0 || carbs !in 0.0..300.0) {
            error("营养数据超出合理范围")
        }
        return Nutrition(kcal = kcal, protein = protein, fat = fat, carbs = carbs)
    }

    private fun normalizeCategory(raw: String, name: String): String {
        DishCategory.fromLabel(raw.trim())?.let { return it.label }
        val text = raw + name
        return when {
            listOf("甜", "蛋糕", "布丁", "奶冻", "西米", "双皮奶", "糖水").any { it in text } -> DishCategory.DESSERT.label
            listOf("汤", "羹").any { it in text } -> DishCategory.SOUP.label
            listOf("凉拌", "冷菜", "口水", "夫妻肺片").any { it in text } -> DishCategory.COLD.label
            listOf("包子", "馒头", "饺子", "葱油饼", "面点", "花卷", "烧麦").any { it in text } -> DishCategory.PASTRY.label
            listOf("意面", "牛排", "沙拉", "汉堡", "西餐", "披萨").any { it in text } -> DishCategory.WESTERN.label
            listOf("小吃", "煎饼", "炸鸡", "肉夹馍", "丸子", "粑粑").any { it in text } -> DishCategory.SNACK.label
            listOf("饭", "面", "粥", "粉", "主食").any { it in text } -> DishCategory.STAPLE.label
            else -> DishCategory.HOME.label
        }
    }

    private fun normalizeUnit(raw: String): String = when (raw.trim().lowercase()) {
        "g", "克" -> "克"
        "kg", "千克" -> "千克"
        "ml", "毫升" -> "毫升"
        "l", "升" -> "升"
        "tbsp", "汤匙" -> "汤匙"
        "tsp", "茶匙" -> "茶匙"
        else -> raw.trim()
    }

    private fun parseDouble(element: JsonElement?): Double? {
        if (element == null) return null
        val primitive = runCatching { element.jsonPrimitive }.getOrNull() ?: return null
        return primitive.doubleOrNull ?: primitive.contentOrNull?.toDoubleOrNull()
    }

    private fun JsonObject.string(key: String): String =
        this[key]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()

    private fun JsonObject.int(key: String): Int =
        this[key]?.jsonPrimitive?.intOrNull ?: 0

    private fun JsonObject.stringList(key: String): List<String> {
        val array: JsonArray = this[key]?.let { runCatching { it.jsonArray }.getOrNull() } ?: return emptyList()
        return array.mapNotNull { item ->
            item.jsonPrimitive.contentOrNull?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    fun extractJsonObject(text: String): String? {
        var start = text.indexOf('{')
        while (start >= 0) {
            var depth = 0
            var inString = false
            var escaped = false
            for (index in start until text.length) {
                val ch = text[index]
                if (inString) {
                    when {
                        escaped -> escaped = false
                        ch == '\\' -> escaped = true
                        ch == '"' -> inString = false
                    }
                } else {
                    when (ch) {
                        '"' -> inString = true
                        '{' -> depth++
                        '}' -> {
                            depth--
                            if (depth == 0) return text.substring(start, index + 1)
                        }
                    }
                }
            }
            start = text.indexOf('{', start + 1)
        }
        return null
    }
}
