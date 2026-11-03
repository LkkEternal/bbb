package com.chanle.app.domain

import com.chanle.app.data.Dish
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

data class ScaledIngredient(
    val name: String,
    val amountText: String,
    val unit: String,
    val note: String,
)

object IngredientScaler {

    private val countUnits = setOf("个", "只", "根", "瓣", "片", "块", "颗", "张", "条", "枚")
    private val spoonUnits = setOf("勺", "茶匙", "汤匙", "小勺", "大勺")
    private val weightUnits = setOf("克", "g", "毫升", "ml")

    fun scale(dish: Dish, targetServings: Int): List<ScaledIngredient> {
        val base = dish.baseServings.coerceAtLeast(1)
        val factor = targetServings.toDouble() / base
        return dish.ingredients.map { scaleOne(it, factor) }
    }

    private fun scaleOne(ingredient: Ingredient, factor: Double): ScaledIngredient {
        val unit = normalizeUnit(ingredient.unit)
        val amount = ingredient.amount
        if (amount == null) {
            return ScaledIngredient(ingredient.name, "", unit, ingredient.note)
        }
        val value = if (ingredient.scalable) amount * factor else amount
        val rounded = roundAmount(value, unit)
        return ScaledIngredient(
            name = ingredient.name,
            amountText = formatNumber(rounded),
            unit = unit,
            note = ingredient.note,
        )
    }

    fun roundAmount(value: Double, unit: String): Double {
        val safe = value.coerceAtLeast(0.0)
        return when {
            unit in countUnits || unit in spoonUnits -> {
                val stepped = round(safe * 2) / 2
                stepped.coerceAtLeast(0.5)
            }
            unit in weightUnits -> when {
                safe < 10 -> round(safe * 10) / 10
                safe <= 50 -> round(safe)
                else -> round(safe / 5) * 5
            }
            else -> when {
                safe < 10 -> round(safe * 10) / 10
                safe <= 50 -> round(safe)
                else -> round(safe / 5) * 5
            }
        }
    }

    fun formatNumber(value: Double): String {
        if (value == floor(value)) return value.toInt().toString()
        return String.format(Locale.US, "%.1f", value).trimEnd('0').trimEnd('.')
    }

    fun totalNutrition(perServing: Nutrition, servings: Int): Nutrition = Nutrition(
        kcal = (perServing.kcal * servings),
        protein = perServing.protein * servings,
        fat = perServing.fat * servings,
        carbs = perServing.carbs * servings,
    )

    fun normalizeUnit(raw: String): String {
        val unit = raw.trim()
        return when (unit.lowercase(Locale.US)) {
            "g", "克" -> "克"
            "kg", "千克" -> "千克"
            "ml", "毫升" -> "毫升"
            "l", "升" -> "升"
            "tbsp", "汤匙" -> "汤匙"
            "tsp", "茶匙" -> "茶匙"
            else -> unit
        }
    }

    fun amountText(scaled: ScaledIngredient): String {
        if (scaled.amountText.isBlank()) return scaled.unit
        return if (scaled.unit.isBlank()) scaled.amountText else "${scaled.amountText} ${scaled.unit}"
    }
}
