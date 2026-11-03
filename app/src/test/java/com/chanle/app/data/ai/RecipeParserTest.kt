package com.chanle.app.data.ai

import com.chanle.app.data.DishCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeParserTest {

    private val validJson = """
        {
          "name": "番茄牛腩",
          "category": "家常菜",
          "description": "酸甜软烂，汤汁拌饭",
          "timeMinutes": 60,
          "difficulty": "中等",
          "ingredients": [
            {"name": "牛腩", "amount": 0.5, "unit": "kg"},
            {"name": "番茄", "amount": 3, "unit": "个"},
            {"name": "盐", "unit": "适量", "scalable": false}
          ],
          "steps": ["牛腩焯水。", "番茄炒出汁后炖 50 分钟。"],
          "tips": ["用热水炖肉更软烂。"],
          "tags": ["下饭"],
          "nutritionPerServing": {"kcal": 420, "protein": 30, "fat": 22, "carbs": 20}
        }
    """.trimIndent()

    @Test
    fun `parses fenced json with chatty prefix`() {
        val raw = "好的，这是菜谱：\n```json\n$validJson\n```\n希望你喜欢。"
        val dish = RecipeParser.parse(raw, "test-model", "番茄牛腩").getOrThrow()

        assertEquals("番茄牛腩", dish.name)
        assertEquals(DishCategory.HOME.label, dish.category)
        assertEquals(60, dish.timeMinutes)
        assertEquals(3, dish.ingredients.size)
        assertEquals("ai", dish.source)
        assertTrue(dish.id.startsWith("ai-"))
        assertTrue(dish.nutritionNote.contains("AI"))
        assertEquals(listOf("下饭", DishCategory.HOME.label), dish.tags)
    }

    @Test
    fun `converts kilograms to grams`() {
        val dish = RecipeParser.parse(validJson, "test-model", "番茄牛腩").getOrThrow()
        val beef = dish.ingredients.first { it.name == "牛腩" }
        assertEquals(500.0, beef.amount ?: 0.0, 0.0001)
        assertEquals("克", beef.unit)
    }

    @Test
    fun `keeps non scalable seasoning unscaled`() {
        val dish = RecipeParser.parse(validJson, "test-model", "番茄牛腩").getOrThrow()
        val salt = dish.ingredients.first { it.name == "盐" }
        assertEquals(null, salt.amount)
        assertEquals("适量", salt.unit)
        assertFalse(salt.scalable)
    }

    @Test
    fun `infers soup category from unknown label`() {
        val raw = validJson
            .replace("\"番茄牛腩\"", "\"紫菜蛋花汤\"")
            .replace("\"家常菜\"", "\"随便\"")
        val dish = RecipeParser.parse(raw, "test-model", "紫菜蛋花汤").getOrThrow()
        assertEquals(DishCategory.SOUP.label, dish.category)
    }

    @Test
    fun `rejects text without json`() {
        val result = RecipeParser.parse("抱歉，我无法生成这道菜。", "test-model", "随便")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("JSON"))
    }

    @Test
    fun `rejects error payload`() {
        val result = RecipeParser.parse("""{"error":"额度不足"}""", "test-model", "随便")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("额度不足"))
    }

    @Test
    fun `rejects too few ingredients`() {
        val raw = """
            {
              "name": "清汤",
              "category": "汤",
              "ingredients": [{"name": "水", "amount": 500, "unit": "毫升"}],
              "steps": ["烧开。", "装碗。"],
              "nutritionPerServing": {"kcal": 10, "protein": 0, "fat": 0, "carbs": 2}
            }
        """.trimIndent()
        val result = RecipeParser.parse(raw, "test-model", "清汤")
        assertTrue(result.isFailure)
    }

    @Test
    fun `extracts object containing braces and escaped quotes in strings`() {
        val text = """前缀 {"name":"测试{菜}","note":"他说\"好}\""} 后缀"""
        assertEquals(
            """{"name":"测试{菜}","note":"他说\"好}\""}""",
            RecipeParser.extractJsonObject(text),
        )
    }

    @Test
    fun `nutrition outside range is rejected`() {
        val raw = validJson.replace("\"kcal\": 420", "\"kcal\": 5000")
        val result = RecipeParser.parse(raw, "test-model", "番茄牛腩")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("营养"))
    }
}
