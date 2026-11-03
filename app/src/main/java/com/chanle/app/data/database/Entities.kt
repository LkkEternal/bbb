package com.chanle.app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: String,
    val name: String,
    val namePinyin: String,
    val category: String,
    val description: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val videoTitle: String? = null,
    val videoPlatform: String? = null,
    val baseServings: Int = 2,
    val timeMinutes: Int = 30,
    val difficulty: String = "简单",
    val nutritionNote: String = "",
    val nutritionKcal: Int = 0,
    val nutritionProtein: Double = 0.0,
    val nutritionFat: Double = 0.0,
    val nutritionCarbs: Double = 0.0,
    val source: String = "curated",
    val createdAt: Long = 0L,
    val model: String = "",
    val prompt: String = "",
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId")]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: String,
    val name: String,
    val amount: Double? = null,
    val unit: String = "",
    val note: String = "",
    val scalable: Boolean = true,
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId")]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: String,
    val stepNumber: Int,
    val description: String,
)

@Entity(
    tableName = "tips",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId")]
)
data class TipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: String,
    val content: String,
)

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId")]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: String,
    val tagName: String,
)

@Entity(tableName = "aliases")
data class AliasEntity(
    @PrimaryKey val alias: String,
    val dishName: String,
)

@Fts4(contentEntity = DishEntity::class)
@Entity(tableName = "dishes_fts")
data class DishFtsEntity(
    val name: String,
    val namePinyin: String,
    val description: String,
)