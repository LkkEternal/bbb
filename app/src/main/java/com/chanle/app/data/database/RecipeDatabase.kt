package com.chanle.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DishEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        TipEntity::class,
        TagEntity::class,
        AliasEntity::class,
        DishFtsEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun dishDao(): DishDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
    abstract fun tipDao(): TipDao
    abstract fun tagDao(): TagDao
    abstract fun aliasDao(): AliasDao
    abstract fun dishFtsDao(): DishFtsDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}