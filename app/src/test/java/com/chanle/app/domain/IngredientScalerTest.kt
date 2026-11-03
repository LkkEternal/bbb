package com.chanle.app.domain

import com.chanle.app.data.Dish
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientScalerTest {

    private fun dish(vararg ingredients: Ingredient) = Dish(
        id = "test",
        name = "测试菜",
        category = "家常菜",
        ingredients = ingredients.toList(),
    )

    @Test
    fun `weight below ten keeps one decimal`() {
        assertEquals(7.5, IngredientScaler.roundAmount(7.54, "克"), 0.0001)
    }

    @Test
    fun `weight between ten and fifty rounds to integer`() {
        assertEquals(10.0, IngredientScaler.roundAmount(10.0, "克"), 0.0001)
        assertEquals(50.0, IngredientScaler.roundAmount(51.0, "克"), 0.0001)
    }

    @Test
    fun `weight above fifty rounds to five`() {
        assertEquals(50.0, IngredientScaler.roundAmount(52.0, "克"), 0.0001)
        assertEquals(55.0, IngredientScaler.roundAmount(53.0, "克"), 0.0001)
        assertEquals(1000.0, IngredientScaler.roundAmount(998.0, "克"), 0.0001)
    }

    @Test
    fun `count units step by half and minimum half`() {
        assertEquals(0.5, IngredientScaler.roundAmount(0.2, "个"), 0.0001)
        assertEquals(1.0, IngredientScaler.roundAmount(1.2, "个"), 0.0001)
        assertEquals(1.5, IngredientScaler.roundAmount(1.3, "个"), 0.0001)
    }

    @Test
    fun `scales from base two to four servings`() {
        val result = IngredientScaler.scale(
            dish(
                Ingredient("五花肉", 500.0, "克"),
                Ingredient("大葱", 1.0, "根"),
            ),
            targetServings = 4,
        )
        assertEquals("1000", result[0].amountText)
        assertEquals("克", result[0].unit)
        assertEquals("2", result[1].amountText)
        assertEquals("根", result[1].unit)
    }

    @Test
    fun `scales down to one serving`() {
        val result = IngredientScaler.scale(
            dish(
                Ingredient("五花肉", 500.0, "克"),
                Ingredient("大葱", 1.0, "根"),
            ),
            targetServings = 1,
        )
        assertEquals("250", result[0].amountText)
        assertEquals("0.5", result[1].amountText)
    }

    @Test
    fun `non scalable amount stays unchanged`() {
        val result = IngredientScaler.scale(
            dish(Ingredient("盐", 3.0, "克", scalable = false)),
            targetServings = 4,
        )
        assertEquals("3", result[0].amountText)
        assertEquals("克", result[0].unit)
    }

    @Test
    fun `null amount shows unit only`() {
        val result = IngredientScaler.scale(
            dish(Ingredient("盐", null, "适量")),
            targetServings = 4,
        )
        assertEquals("", result[0].amountText)
        assertEquals("适量", result[0].unit)
        assertEquals("适量", IngredientScaler.amountText(result[0]))
    }

    @Test
    fun `total nutrition multiplies per serving values`() {
        val total = IngredientScaler.totalNutrition(
            Nutrition(kcal = 200, protein = 10.0, fat = 5.0, carbs = 20.0),
            servings = 3,
        )
        assertEquals(600, total.kcal)
        assertEquals(30.0, total.protein, 0.0001)
        assertEquals(15.0, total.fat, 0.0001)
        assertEquals(60.0, total.carbs, 0.0001)
    }

    @Test
    fun `formats numbers without trailing zeros`() {
        assertEquals("2", IngredientScaler.formatNumber(2.0))
        assertEquals("2.5", IngredientScaler.formatNumber(2.5))
        assertEquals("0.5", IngredientScaler.formatNumber(0.5))
    }

    @Test
    fun `normalizes common units`() {
        assertEquals("克", IngredientScaler.normalizeUnit("g"))
        assertEquals("毫升", IngredientScaler.normalizeUnit("ml"))
        assertEquals("汤匙", IngredientScaler.normalizeUnit("tbsp"))
        assertEquals("个", IngredientScaler.normalizeUnit("个"))
    }
}
