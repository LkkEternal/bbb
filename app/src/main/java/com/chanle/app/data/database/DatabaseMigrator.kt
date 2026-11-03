package com.chanle.app.data.database

import android.content.Context
import com.chanle.app.data.Dish
import com.chanle.app.data.RecipeFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object DatabaseMigrator {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dishImageUrls: Map<String, String> = emptyMap()

    suspend fun migrateFromJson(
        context: Context,
        database: RecipeDatabase,
        onProgress: ((Int, Int) -> Unit)? = null
    ): MigrationResult = withContext(Dispatchers.IO) {
        val result = runCatching {
            val raw = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
            val recipeFile = json.decodeFromString<RecipeFile>(raw)
            val dishes = recipeFile.dishes

            if (dishes.isEmpty()) {
                return@runCatching MigrationResult(0, 0, null)
            }

            val total = dishes.size
            var processed = 0

            dishes.forEach { dish ->
                val existingDish = database.dishDao().getDishById(dish.id)
                if (existingDish == null) {
                    insertDishWithRelations(database, dish)
                } else {
                    val updatedEntity = existingDish.copy(imageUrl = null)
                    database.dishDao().updateDish(updatedEntity)
                }
                processed++
                onProgress?.invoke(processed, total)
            }

            insertDefaultAliases(database)

            MigrationResult(total, processed, null)
        }

        result.fold(
            onSuccess = { it },
            onFailure = { MigrationResult(0, 0, it) }
        )
    }

    fun getImageUrlForDish(dishId: String): String? {
        return dishImageUrls[dishId]
    }

    private suspend fun insertDishWithRelations(database: RecipeDatabase, dish: Dish) {
        val dishEntity = dish.toEntity()
        database.dishDao().insertDish(dishEntity)

        val ingredients = dish.ingredients.map { it.toEntity(dish.id) }
        database.ingredientDao().insertIngredients(ingredients)

        val steps = dish.steps.mapIndexed { index, step -> step.toStepEntity(dish.id, index + 1) }
        database.stepDao().insertSteps(steps)

        val tips = dish.tips.map { tip -> TipEntity(dishId = dish.id, content = tip) }
        database.tipDao().insertTips(tips)

        val tags = dish.tags.map { tag -> TagEntity(dishId = dish.id, tagName = tag) }
        database.tagDao().insertTags(tags)
    }

    private suspend fun insertDefaultAliases(database: RecipeDatabase) {
        val aliases = listOf(
            AliasEntity("西红柿炒鸡蛋", "番茄炒蛋"),
            AliasEntity("西红柿炒蛋", "番茄炒蛋"),
            AliasEntity("番茄炒鸡蛋", "番茄炒蛋"),
            AliasEntity("宫爆鸡丁", "宫保鸡丁"),
            AliasEntity("红烧五花肉", "红烧肉"),
            AliasEntity("水煮肉片", "水煮肉片"),
            AliasEntity("麻婆豆腐", "麻婆豆腐"),
            AliasEntity("番茄蛋花汤", "番茄蛋汤"),
            AliasEntity("西红柿蛋汤", "番茄蛋汤"),
            AliasEntity("冬瓜骨头汤", "冬瓜排骨汤"),
            AliasEntity("紫菜汤", "紫菜蛋花汤"),
            AliasEntity("玉米骨头汤", "玉米排骨汤"),
            AliasEntity("炒饭", "蛋炒饭"),
            AliasEntity("扬州蛋炒饭", "扬州炒饭"),
            AliasEntity("拌面", "葱油拌面"),
            AliasEntity("红烧牛肉面", "牛肉面"),
            AliasEntity("咖喱鸡肉饭", "咖喱饭"),
            AliasEntity("水饺", "韭菜鸡蛋饺子"),
            AliasEntity("饺子", "韭菜鸡蛋饺子"),
            AliasEntity("包子", "鲜肉包"),
            AliasEntity("肉包", "鲜肉包"),
            AliasEntity("手擀面条", "手擀面"),
            AliasEntity("皮蛋拌豆腐", "皮蛋豆腐"),
            AliasEntity("凉拌黑木耳", "凉拌木耳"),
            AliasEntity("银耳汤", "银耳莲子羹"),
            AliasEntity("红豆汤", "红豆沙"),
            AliasEntity("芒果西米露", "杨枝甘露"),
            AliasEntity("酒酿小圆子", "酒酿圆子"),
            AliasEntity("意大利面", "番茄意面"),
            AliasEntity("番茄意大利面", "番茄意面"),
            AliasEntity("牛排", "黑椒牛排"),
            AliasEntity("凯撒色拉", "凯撒沙拉"),
            AliasEntity("蘑菇汤", "奶油蘑菇汤"),
            AliasEntity("汉堡", "芝士汉堡"),
            AliasEntity("煎饼", "煎饼果子"),
            AliasEntity("腊汁肉夹馍", "肉夹馍"),
            AliasEntity("炸鸡块", "炸鸡"),
            AliasEntity("章鱼烧", "章鱼小丸子"),
            AliasEntity("糖油粑粑", "糖油粑粑"),
        )
        database.aliasDao().insertAliases(aliases)
    }

    private fun Dish.toEntity(): DishEntity {
        return DishEntity(
            id = id,
            name = name,
            namePinyin = PinyinConverter.toPinyin(name),
            category = category,
            description = description,
            imageUrl = dishImageUrls[id],
            videoUrl = null,
            videoTitle = null,
            videoPlatform = null,
            baseServings = baseServings,
            timeMinutes = timeMinutes,
            difficulty = difficulty,
            nutritionNote = nutritionNote,
            nutritionKcal = nutritionPerServing.kcal,
            nutritionProtein = nutritionPerServing.protein,
            nutritionFat = nutritionPerServing.fat,
            nutritionCarbs = nutritionPerServing.carbs,
            source = source,
            createdAt = createdAt,
            model = model,
            prompt = prompt,
        )
    }

    private fun com.chanle.app.data.Ingredient.toEntity(dishId: String): IngredientEntity {
        return IngredientEntity(
            dishId = dishId,
            name = name,
            amount = amount,
            unit = unit,
            note = note,
            scalable = scalable,
        )
    }

    private fun String.toStepEntity(dishId: String, stepNumber: Int): StepEntity {
        return StepEntity(
            dishId = dishId,
            stepNumber = stepNumber,
            description = this,
        )
    }
}

data class MigrationResult(
    val total: Int,
    val processed: Int,
    val error: Throwable?,
) {
    val isSuccess: Boolean get() = error == null && processed == total
    val isPartial: Boolean get() = error == null && processed < total
    val isFailure: Boolean get() = error != null
}