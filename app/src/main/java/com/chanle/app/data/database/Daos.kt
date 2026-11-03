package com.chanle.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes ORDER BY name ASC")
    fun getAllDishes(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes ORDER BY name ASC")
    suspend fun getAllDishesList(): List<DishEntity>

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getDishById(id: String): DishEntity?

    @Query("SELECT * FROM dishes WHERE name = :name LIMIT 1")
    suspend fun getDishByName(name: String): DishEntity?

    @Query("SELECT * FROM dishes WHERE category = :category ORDER BY name ASC")
    fun getDishesByCategory(category: String): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id IN (:ids)")
    suspend fun getDishesByIds(ids: List<String>): List<DishEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: DishEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(dishes: List<DishEntity>)

    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun deleteDish(id: String)

    @Query("DELETE FROM dishes")
    suspend fun deleteAllDishes()

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun getDishCount(): Int

    @Query("SELECT COUNT(*) FROM dishes")
    fun getDishCountFlow(): Flow<Int>
}

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE dishId = :dishId ORDER BY id ASC")
    suspend fun getIngredientsByDishId(dishId: String): List<IngredientEntity>

    @Query("SELECT DISTINCT name FROM ingredients ORDER BY name ASC")
    suspend fun getAllIngredientNames(): List<String>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :name || '%'")
    suspend fun searchIngredients(name: String): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE dishId = :dishId")
    suspend fun deleteIngredientsByDishId(dishId: String)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE dishId = :dishId ORDER BY stepNumber ASC")
    suspend fun getStepsByDishId(dishId: String): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: StepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM steps WHERE dishId = :dishId")
    suspend fun deleteStepsByDishId(dishId: String)
}

@Dao
interface TipDao {
    @Query("SELECT * FROM tips WHERE dishId = :dishId")
    suspend fun getTipsByDishId(dishId: String): List<TipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: TipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<TipEntity>)

    @Query("DELETE FROM tips WHERE dishId = :dishId")
    suspend fun deleteTipsByDishId(dishId: String)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE dishId = :dishId")
    suspend fun getTagsByDishId(dishId: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE tagName = :tagName")
    suspend fun getDishesByTag(tagName: String): List<TagEntity>

    @Query("SELECT DISTINCT tagName FROM tags ORDER BY tagName ASC")
    suspend fun getAllTags(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags WHERE dishId = :dishId")
    suspend fun deleteTagsByDishId(dishId: String)
}

@Dao
interface AliasDao {
    @Query("SELECT * FROM aliases")
    suspend fun getAllAliases(): List<AliasEntity>

    @Query("SELECT * FROM aliases WHERE alias = :alias LIMIT 1")
    suspend fun getAlias(alias: String): AliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: AliasEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAliases(aliases: List<AliasEntity>)
}

@Dao
interface DishFtsDao {
    @Query("SELECT dishes.* FROM dishes JOIN dishes_fts ON dishes.id = dishes_fts.rowid WHERE dishes_fts MATCH :query ORDER BY name ASC")
    suspend fun searchDishes(query: String): List<DishEntity>

    @Query("SELECT COUNT(*) FROM dishes_fts WHERE dishes_fts MATCH :query")
    suspend fun countSearchResults(query: String): Int
}