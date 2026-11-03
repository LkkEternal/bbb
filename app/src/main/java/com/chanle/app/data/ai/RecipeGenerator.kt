package com.chanle.app.data.ai

import com.chanle.app.data.Dish

class RecipeGenerator(
    private val client: AiClient,
    private val settings: AiSettings,
) {
    suspend fun generate(name: String): Result<Dish> {
        val raw = client.generateRecipe(name).getOrElse { return Result.failure(it) }
        return RecipeParser.parse(raw, settings.config.value.modelName, name)
    }
}
