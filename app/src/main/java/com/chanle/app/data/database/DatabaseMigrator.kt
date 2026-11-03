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

    private val dishImageUrls = mapOf(
        "hongshao-rou" to "https://images.unsplash.com/photo-1623689046286-5776368a0cc2?w=800",
        "fanqie-chaodan" to "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?w=800",
        "gongbao-jiding" to "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800",
        "yuxiang-rousi" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800",
        "mapo-doufu" to "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800",
        "fanqie-dantang" to "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800",
        "donggua-paigutang" to "https://images.unsplash.com/photo-1547592180-85f173990554?w=800",
        "zicai-danhuatang" to "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
        "yumi-paigutang" to "https://images.unsplash.com/photo-1506484381205-f7945b7e64e0?w=800",
        "suanlatang" to "https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=800",
        "danchaofan" to "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800",
        "yangzhou-chaofan" to "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=800",
        "congyou-banmian" to "https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=800",
        "niuroumian" to "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800",
        "gali-fan" to "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=800",
        "mantou" to "https://images.unsplash.com/photo-1587899891549-3f79a320046f?w=800",
        "xianroubao" to "https://images.unsplash.com/photo-1496116218417-1a781b1c423c?w=800",
        "shouganmian" to "https://images.unsplash.com/photo-1617093727343-374698b1b08d?w=800",
        "congyoubing" to "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800",
        "jiucai-jiaozi" to "https://images.unsplash.com/photo-1496116218417-1a781b1c423c?w=800",
        "liangban-huanggua" to "https://images.unsplash.com/photo-1596591606975-97ee5cef3a1e?w=800",
        "koushuiji" to "https://images.unsplash.com/photo-1532543491484-4377950e37f9?w=800",
        "pidandoufu" to "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
        "liangban-muer" to "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800",
        "fuqi-feipian" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
        "yiner-lianzigeng" to "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800",
        "hongdousha" to "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800",
        "shuangpinai" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=800",
        "yangzhi-ganlu" to "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800",
        "jiuniang-yuanzi" to "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800",
        "fanqie-yimian" to "https://images.unsplash.com/photo-1551462147-ff29053bfc14?w=800",
        "heijiao-niupai" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
        "caesar-shala" to "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=800",
        "naiyou-mogutang" to "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800",
        "zhishi-hanbao" to "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800",
        "jianbing-guozi" to "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=800",
        "roujiamo" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800",
        "zhaji" to "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800",
        "zhangyu-xiaowanzi" to "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800",
        "tangyou-baba" to "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800",
    )

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
                insertDishWithRelations(database, dish)
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