package com.chanle.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chanle.app.data.database.DishRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private val Context.chanLeDataStore by preferencesDataStore(name = "chanle_prefs")

class RecipeRepository(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    private val favoritesKey = stringSetPreferencesKey("favorite_ids")
    private val mineFile = File(appContext.filesDir, "my_recipes.json")
    private var dishRepository: DishRepository? = null

    fun setDishRepository(repo: DishRepository) {
        dishRepository = repo
    }

    val favorites: Flow<Set<String>> = appContext.chanLeDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs[favoritesKey] ?: emptySet() }

    suspend fun loadCurated(): List<Dish> {
        val repo = dishRepository
        if (repo != null) {
            val dishes = repo.getAllDishes()
            if (dishes.isNotEmpty()) {
                return dishes
            }
        }

        val raw = appContext.assets.open("recipes.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<RecipeFile>(raw).dishes
    }

    suspend fun loadMine(): List<Dish> {
        if (!mineFile.exists()) return emptyList()
        return runCatching {
            val raw = mineFile.readText()
            json.decodeFromString<RecipeFile>(raw).dishes
        }.getOrDefault(emptyList())
    }

    suspend fun saveMine(dishes: List<Dish>) {
        val payload = json.encodeToString(RecipeFile(version = 1, dishes = dishes))
        val temp = File(appContext.filesDir, "my_recipes.json.tmp")
        temp.writeText(payload)
        if (!temp.renameTo(mineFile)) {
            mineFile.writeText(payload)
            temp.delete()
        }
    }

    suspend fun toggleFavorite(id: String) {
        appContext.chanLeDataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (id in current) current - id else current + id
        }
    }

    fun search(dishes: List<Dish>, query: String): List<Dish> {
        val q = normalize(query)
        if (q.isBlank()) return dishes
        return dishes.filter { dish ->
            normalize(dish.name).contains(q) ||
                normalize(dish.description).contains(q) ||
                dish.tags.any { normalize(it).contains(q) } ||
                dish.ingredients.any { normalize(it.name).contains(q) }
        }
    }

    fun matchLocal(dishes: List<Dish>, input: String): Dish? {
        val normalized = normalize(input)
        if (normalized.isBlank()) return null
        dishes.firstOrNull { normalize(it.name) == normalized }?.let { return it }
        val alias = aliases[normalized] ?: return null
        return dishes.firstOrNull { normalize(it.name) == normalize(alias) }
    }

    fun normalize(value: String): String = value
        .trim()
        .lowercase()
        .replace(Regex("[\\s\\u3000]+"), "")
        .replace(Regex("[，,。.、！!？?：:；;（）()\\[\\]【】]"), "")

    private companion object {
        val aliases: Map<String, String> = mapOf(
            "西红柿炒鸡蛋" to "番茄炒蛋",
            "西红柿炒蛋" to "番茄炒蛋",
            "番茄炒鸡蛋" to "番茄炒蛋",
            "宫爆鸡丁" to "宫保鸡丁",
            "红烧五花肉" to "红烧肉",
            "水煮肉片" to "鱼香肉丝",
            "麻婆豆腐" to "麻婆豆腐",
            "番茄蛋花汤" to "番茄蛋汤",
            "西红柿蛋汤" to "番茄蛋汤",
            "冬瓜骨头汤" to "冬瓜排骨汤",
            "紫菜汤" to "紫菜蛋花汤",
            "玉米骨头汤" to "玉米排骨汤",
            "炒饭" to "蛋炒饭",
            "扬州蛋炒饭" to "扬州炒饭",
            "拌面" to "葱油拌面",
            "红烧牛肉面" to "牛肉面",
            "咖喱鸡肉饭" to "咖喱饭",
            "水饺" to "韭菜鸡蛋饺子",
            "饺子" to "韭菜鸡蛋饺子",
            "包子" to "鲜肉包",
            "肉包" to "鲜肉包",
            "手擀面条" to "手擀面",
            "皮蛋拌豆腐" to "皮蛋豆腐",
            "凉拌黑木耳" to "凉拌木耳",
            "银耳汤" to "银耳莲子羹",
            "红豆汤" to "红豆沙",
            "芒果西米露" to "杨枝甘露",
            "酒酿小圆子" to "酒酿圆子",
            "意大利面" to "番茄意面",
            "番茄意大利面" to "番茄意面",
            "牛排" to "黑椒牛排",
            "凯撒色拉" to "凯撒沙拉",
            "蘑菇汤" to "奶油蘑菇汤",
            "汉堡" to "芝士汉堡",
            "煎饼" to "煎饼果子",
            "腊汁肉夹馍" to "肉夹馍",
            "炸鸡块" to "炸鸡",
            "章鱼烧" to "章鱼小丸子",
            "糖油粑粑" to "糖油粑粑",
        )
    }
}