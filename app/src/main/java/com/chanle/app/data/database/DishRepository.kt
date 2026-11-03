package com.chanle.app.data.database

import com.chanle.app.data.Dish
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import kotlinx.coroutines.flow.first

class DishRepository(private val database: RecipeDatabase) {
    private val searchEngine = DishSearchEngine(database)

    suspend fun getAllDishes(): List<Dish> {
        return database.dishDao().getAllDishesList().map { entity ->
            loadFullDish(entity)
        }
    }

    suspend fun getDishById(id: String): Dish? {
        val entity = database.dishDao().getDishById(id) ?: return null
        return loadFullDish(entity)
    }

    suspend fun getDishesByCategory(category: String): List<Dish> {
        val dishes = database.dishDao().getDishesByCategory(category).first()
        return dishes.map { entity -> loadFullDish(entity) }
    }

    suspend fun search(query: String): List<Dish> {
        return searchEngine.search(query)
    }

    suspend fun getAllIngredientNames(): List<String> {
        return database.ingredientDao().getAllIngredientNames()
    }

    suspend fun getAllTags(): List<String> {
        return database.tagDao().getAllTags()
    }

    suspend fun getDishCount(): Int {
        return database.dishDao().getDishCount()
    }

    private suspend fun loadFullDish(entity: DishEntity): Dish {
        val ingredients = database.ingredientDao()
            .getIngredientsByDishId(entity.id)
            .map { Ingredient(it.name, it.amount, it.unit, it.note, it.scalable) }

        val steps = database.stepDao()
            .getStepsByDishId(entity.id)
            .sortedBy { it.stepNumber }
            .map { it.description }

        val tips = database.tipDao()
            .getTipsByDishId(entity.id)
            .map { it.content }

        val tags = database.tagDao()
            .getTagsByDishId(entity.id)
            .map { it.tagName }

        return Dish(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            description = entity.description,
            imageUrl = entity.imageUrl,
            baseServings = entity.baseServings,
            timeMinutes = entity.timeMinutes,
            difficulty = entity.difficulty,
            ingredients = ingredients,
            steps = steps,
            tips = tips,
            tags = tags,
            nutritionPerServing = Nutrition(
                kcal = entity.nutritionKcal,
                protein = entity.nutritionProtein,
                fat = entity.nutritionFat,
                carbs = entity.nutritionCarbs,
            ),
            nutritionNote = entity.nutritionNote,
            source = entity.source,
            createdAt = entity.createdAt,
            model = entity.model,
            prompt = entity.prompt,
        )
    }
}