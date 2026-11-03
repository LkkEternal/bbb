package com.chanle.app.data.database

import com.chanle.app.data.Dish
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import kotlinx.coroutines.flow.first

class DishSearchEngine(private val database: RecipeDatabase) {

    private val normalizer = SearchNormalizer()

    suspend fun search(query: String): List<Dish> {
        val normalized = normalizer.normalize(query)
        if (normalized.isBlank()) return emptyList()

        val results = mutableSetOf<String>()

        val exactMatch = exactMatch(normalized)
        results.addAll(exactMatch)

        val aliasMatch = aliasMatch(normalized)
        results.addAll(aliasMatch)

        val ftsMatch = ftsMatch(normalized)
        results.addAll(ftsMatch)

        val ingredientMatch = ingredientMatch(normalized)
        results.addAll(ingredientMatch)

        if (results.isEmpty()) {
            val fuzzyMatch = fuzzyMatch(normalized)
            results.addAll(fuzzyMatch)
        }

        return database.dishDao().getDishesByIds(results.toList())
            .map { it.toDish() }
            .sortedBy { dish ->
                val nameMatch = normalizer.normalize(dish.name).contains(normalized)
                val descMatch = normalizer.normalize(dish.description).contains(normalized)
                when {
                    nameMatch && dish.name == query -> 0
                    nameMatch -> 1
                    descMatch -> 2
                    else -> 3
                }
            }
    }

    private suspend fun exactMatch(query: String): List<String> {
        val results = mutableListOf<String>()

        val normalizedQuery = normalizer.normalize(query)

        val allDishes = database.dishDao().getAllDishesList()
        allDishes.forEach { dish ->
            val normalizedName = normalizer.normalize(dish.name)
            if (normalizedName == normalizedQuery) {
                results.add(dish.id)
            }
        }

        return results
    }

    private suspend fun aliasMatch(query: String): List<String> {
        val results = mutableListOf<String>()

        val normalizedQuery = normalizer.normalize(query)

        val alias = database.aliasDao().getAlias(normalizedQuery)
        if (alias != null) {
            val dish = database.dishDao().getDishByName(alias.dishName)
            if (dish != null) {
                results.add(dish.id)
            }
        }

        return results
    }

    private suspend fun ftsMatch(query: String): List<String> {
        if (query.length < 2) return emptyList()

        return try {
            val ftsQuery = PinyinConverter.getMatchQuery(query)
            if (ftsQuery.isBlank()) return emptyList()

            val searchResults = database.dishFtsDao().searchDishes(ftsQuery)
            searchResults.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun ingredientMatch(query: String): List<String> {
        if (query.length < 2) return emptyList()

        return try {
            val ingredients = database.ingredientDao().searchIngredients(query)
            ingredients.map { it.dishId }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fuzzyMatch(query: String): List<String> {
        if (query.length < 2) return emptyList()

        val results = mutableListOf<String>()
        val normalizedQuery = normalizer.normalize(query)

        val allDishes = database.dishDao().getAllDishesList()
        allDishes.forEach { dish ->
            val normalizedName = normalizer.normalize(dish.name)
            val namePinyin = PinyinConverter.toPinyinInitials(dish.name)

            val nameMatch = normalizedName.contains(normalizedQuery) ||
                    namePinyin.contains(normalizedQuery) ||
                    normalizer.fuzzyMatch(normalizedName, normalizedQuery)

            if (nameMatch) {
                results.add(dish.id)
            }
        }

        return results.take(20)
    }

    suspend fun getAllDishes(): List<Dish> {
        return database.dishDao().getAllDishesList().map { it.toDish() }
    }

    suspend fun getDishesByCategory(category: String): List<Dish> {
        val dishes = database.dishDao().getDishesByCategory(category).first()
        return dishes.map { it.toDish() }
    }

    suspend fun getDishById(id: String): Dish? {
        return database.dishDao().getDishById(id)?.toDish()
    }

    suspend fun getDishByName(name: String): Dish? {
        val normalizedName = normalizer.normalize(name)
        val allDishes = database.dishDao().getAllDishesList()

        return allDishes.firstOrNull { dish ->
            normalizer.normalize(dish.name) == normalizedName
        }?.toDish()
    }
}

class SearchNormalizer {
    fun normalize(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("[\\s\\u3000]+"), "")
            .replace(Regex("[，,。.、！!？?：:；;（）()\\[\\]【】]"), "")
    }

    fun fuzzyMatch(source: String, target: String): Boolean {
        if (source.contains(target)) return true
        if (target.length < 2) return false

        var sourceIndex = 0
        var targetIndex = 0
        while (sourceIndex < source.length && targetIndex < target.length) {
            if (source[sourceIndex] == target[targetIndex]) {
                targetIndex++
            }
            sourceIndex++
        }
        return targetIndex == target.length
    }
}

private fun DishEntity.toDish(): Dish {
    return Dish(
        id = id,
        name = name,
        category = category,
        description = description,
        baseServings = baseServings,
        timeMinutes = timeMinutes,
        difficulty = difficulty,
        ingredients = emptyList(),
        steps = emptyList(),
        tips = emptyList(),
        tags = emptyList(),
        nutritionPerServing = Nutrition(
            kcal = nutritionKcal,
            protein = nutritionProtein,
            fat = nutritionFat,
            carbs = nutritionCarbs,
        ),
        nutritionNote = nutritionNote,
        source = source,
        createdAt = createdAt,
        model = model,
        prompt = prompt,
    )
}