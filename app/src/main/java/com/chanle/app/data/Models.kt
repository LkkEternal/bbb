package com.chanle.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val amount: Double? = null,
    val unit: String = "",
    val note: String = "",
    val scalable: Boolean = true,
)

@Serializable
data class Nutrition(
    val kcal: Int = 0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,
)

@Serializable
data class Dish(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val baseServings: Int = 2,
    val timeMinutes: Int = 30,
    val difficulty: String = "简单",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val nutritionPerServing: Nutrition = Nutrition(),
    val nutritionNote: String = "",
    val source: String = "curated",
    val createdAt: Long = 0L,
    val model: String = "",
    val prompt: String = "",
    val imageUrl: String? = null,
) {
    val isAi: Boolean get() = source == "ai"
}

@Serializable
data class RecipeFile(
    val version: Int = 1,
    val dishes: List<Dish> = emptyList(),
)