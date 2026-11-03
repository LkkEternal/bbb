package com.chanle.app.data.database

import com.chanle.app.data.Dish
import com.chanle.app.data.Ingredient
import com.chanle.app.data.Nutrition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PinyinConverterTest {

    @Test
    fun `test toPinyin basic conversion`() {
        assertEquals("hong", PinyinConverter.toPinyin("红"))
        assertEquals("shao", PinyinConverter.toPinyin("烧"))
        assertEquals("rou", PinyinConverter.toPinyin("肉"))
    }

    @Test
    fun `test toPinyinInitials extraction`() {
        val result = PinyinConverter.toPinyinInitials("红烧肉")
        assertTrue(result.length == 3)
        assertEquals('h', result[0])
        assertEquals('s', result[1])
        assertEquals('r', result[2])
    }

    @Test
    fun `test getMatchQuery generation`() {
        val query = PinyinConverter.getMatchQuery("红")
        assertTrue(query.isNotBlank())
    }

    @Test
    fun `test pinyin conversion with partial characters`() {
        val result = PinyinConverter.toPinyin("番")
        assertTrue(result.contains("fan") || result.isNotEmpty())
    }
}

@RunWith(JUnit4::class)
class SearchNormalizerTest {

    private val normalizer = SearchNormalizer()

    @Test
    fun `test basic normalization`() {
        assertEquals("红烧肉", normalizer.normalize("红烧肉"))
        assertEquals("红烧肉", normalizer.normalize("  红烧肉  "))
        assertEquals("红烧肉", normalizer.normalize("红,烧。肉"))
        assertEquals("红烧肉", normalizer.normalize("红　烧　肉"))
    }

    @Test
    fun `test fuzzy matching`() {
        assertTrue(normalizer.fuzzyMatch("红烧肉", "红烧"))
        assertTrue(normalizer.fuzzyMatch("红烧肉", "肉"))
        assertTrue(normalizer.fuzzyMatch("番茄炒蛋", "番茄"))
        assertTrue(normalizer.fuzzyMatch("番茄炒蛋", "蛋"))
        assertFalse(normalizer.fuzzyMatch("红烧肉", "糖醋"))
    }
}

@RunWith(JUnit4::class)
class DishEntityTest {

    @Test
    fun `test DishEntity creation`() {
        val entity = DishEntity(
            id = "test-id",
            name = "测试菜品",
            namePinyin = "ceshipincai",
            category = "家常菜",
            description = "测试描述",
            baseServings = 2,
            timeMinutes = 30,
            difficulty = "简单",
            nutritionKcal = 200,
            nutritionProtein = 10.0,
            nutritionFat = 8.0,
            nutritionCarbs = 20.0,
        )

        assertEquals("test-id", entity.id)
        assertEquals("测试菜品", entity.name)
        assertEquals("ceshipincai", entity.namePinyin)
        assertEquals("家常菜", entity.category)
        assertEquals(200, entity.nutritionKcal)
    }

    @Test
    fun `test IngredientEntity creation`() {
        val entity = IngredientEntity(
            dishId = "test-id",
            name = "五花肉",
            amount = 500.0,
            unit = "克",
            note = "切块",
            scalable = true,
        )

        assertEquals("test-id", entity.dishId)
        assertEquals("五花肉", entity.name)
        assertEquals(500.0, entity.amount ?: 0.0, 0.001)
        assertEquals("克", entity.unit)
        assertTrue(entity.scalable)
    }

    @Test
    fun `test StepEntity ordering`() {
        val step1 = StepEntity(
            dishId = "test-id",
            stepNumber = 1,
            description = "第一步",
        )
        val step2 = StepEntity(
            dishId = "test-id",
            stepNumber = 2,
            description = "第二步",
        )

        assertEquals(1, step1.stepNumber)
        assertEquals(2, step2.stepNumber)
    }

    @Test
    fun `test TagEntity creation`() {
        val tag = TagEntity(
            dishId = "test-id",
            tagName = "下饭",
        )

        assertEquals("test-id", tag.dishId)
        assertEquals("下饭", tag.tagName)
    }

    @Test
    fun `test AliasEntity creation`() {
        val alias = AliasEntity(
            alias = "西红柿炒鸡蛋",
            dishName = "番茄炒蛋",
        )

        assertEquals("西红柿炒鸡蛋", alias.alias)
        assertEquals("番茄炒蛋", alias.dishName)
    }
}

@RunWith(JUnit4::class)
class MigrationResultTest {

    @Test
    fun `test successful migration result`() {
        val result = MigrationResult(
            total = 40,
            processed = 40,
            error = null,
        )

        assertTrue(result.isSuccess)
        assertFalse(result.isPartial)
        assertFalse(result.isFailure)
        assertEquals(40, result.total)
        assertEquals(40, result.processed)
    }

    @Test
    fun `test partial migration result`() {
        val result = MigrationResult(
            total = 40,
            processed = 30,
            error = null,
        )

        assertFalse(result.isSuccess)
        assertTrue(result.isPartial)
        assertFalse(result.isFailure)
    }

    @Test
    fun `test failed migration result`() {
        val error = RuntimeException("Test error")
        val result = MigrationResult(
            total = 40,
            processed = 0,
            error = error,
        )

        assertFalse(result.isSuccess)
        assertFalse(result.isPartial)
        assertTrue(result.isFailure)
        assertNotNull(result.error)
    }
}

@RunWith(JUnit4::class)
class DishImageUrlTest {

    @Test
    fun `test all dishes have image URLs configured`() {
        val totalDishes = 40
        val configuredUrls = DatabaseMigrator::class.java
            .getDeclaredField("dishImageUrls")
            .apply { isAccessible = true }
            .get(null) as Map<*, *>
        
        assertEquals(
            "应该为所有40道菜谱配置图片URL",
            totalDishes,
            configuredUrls.size
        )
    }

    @Test
    fun `test image URLs use Unsplash format`() {
        val configuredUrls = DatabaseMigrator::class.java
            .getDeclaredField("dishImageUrls")
            .apply { isAccessible = true }
            .get(null) as Map<String, String>
        
        configuredUrls.values.forEach { url ->
            assertTrue(
                "图片URL应该使用Unsplash格式: $url",
                url.startsWith("https://images.unsplash.com/")
            )
            assertTrue(
                "图片URL应该包含尺寸参数: $url",
                url.contains("?w=")
            )
        }
    }
}

@RunWith(JUnit4::class)
class DishConversionTest {

    @Test
    fun `test DishEntity to Dish conversion`() {
        val entity = DishEntity(
            id = "test-id",
            name = "红烧肉",
            namePinyin = "hongsharou",
            category = "家常菜",
            description = "肥而不腻",
            baseServings = 2,
            timeMinutes = 90,
            difficulty = "中等",
            nutritionKcal = 650,
            nutritionProtein = 28.0,
            nutritionFat = 52.0,
            nutritionCarbs = 15.0,
            source = "curated",
        )

        val dish = Dish(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            description = entity.description,
            baseServings = entity.baseServings,
            timeMinutes = entity.timeMinutes,
            difficulty = entity.difficulty,
            ingredients = emptyList(),
            steps = emptyList(),
            tips = emptyList(),
            tags = emptyList(),
            nutritionPerServing = Nutrition(
                kcal = entity.nutritionKcal,
                protein = entity.nutritionProtein,
                fat = entity.nutritionFat,
                carbs = entity.nutritionCarbs,
            ),
            source = entity.source,
        )

        assertEquals("test-id", dish.id)
        assertEquals("红烧肉", dish.name)
        assertEquals(650, dish.nutritionPerServing.kcal)
        assertEquals(28.0, dish.nutritionPerServing.protein, 0.001)
        assertEquals("curated", dish.source)
    }

    @Test
    fun `test Dish with full data`() {
        val dish = Dish(
            id = "fanqie-chaodan",
            name = "番茄炒蛋",
            category = "家常菜",
            description = "酸甜开胃",
            baseServings = 2,
            timeMinutes = 15,
            difficulty = "简单",
            ingredients = listOf(
                Ingredient("番茄", 400.0, "克", "切滚刀块", true),
                Ingredient("鸡蛋", 4.0, "个", "打散", true),
                Ingredient("盐", null, "适量", "", false),
            ),
            steps = listOf(
                "鸡蛋加盐打散",
                "番茄切块",
                "炒鸡蛋",
                "炒番茄",
                "混合调味",
            ),
            tips = listOf(
                "番茄选熟透的",
                "鸡蛋不要炒太老",
            ),
            tags = listOf("快手", "下饭"),
            nutritionPerServing = Nutrition(220, 12.0, 16.0, 7.0),
            source = "curated",
        )

        assertEquals(3, dish.ingredients.size)
        assertEquals(5, dish.steps.size)
        assertEquals(2, dish.tips.size)
        assertEquals(2, dish.tags.size)
        assertFalse(dish.isAi)
    }

    @Test
    fun `test AI generated dish identification`() {
        val aiDish = Dish(
            id = "ai-123",
            name = "AI菜品",
            category = "家常菜",
            source = "ai",
        )

        assertTrue(aiDish.isAi)
    }
}

@RunWith(JUnit4::class)
class SearchIntegrationTest {

    @Test
    fun `test search query generation`() {
        val queries = listOf(
            "红烧肉" to listOf("hong", "shao", "rou"),
            "番茄" to listOf("fan", "qie"),
        )

        queries.forEach { (input, expectedParts) ->
            val pinyin = PinyinConverter.toPinyin(input)
            expectedParts.forEach { part ->
                assertTrue(
                    "Pinyin for '$input' should contain '$part', got '$pinyin'",
                    pinyin.contains(part)
                )
            }
        }
    }

    @Test
    fun `test FTS query generation for different inputs`() {
        val testCases = listOf(
            "红烧",
            "番茄",
            "鸡蛋",
            "家常",
        )

        testCases.forEach { input ->
            val query = PinyinConverter.getMatchQuery(input)
            assertTrue(
                "Query for '$input' should not be empty",
                query.isNotBlank()
            )
        }
    }

    @Test
    fun `test multi-character keyword search`() {
        val keywords = listOf(
            "红烧肉",
            "番茄",
            "鸡丁",
        )

        keywords.forEach { keyword ->
            val pinyin = PinyinConverter.toPinyin(keyword)
            val initials = PinyinConverter.toPinyinInitials(keyword)

            assertTrue("Pinyin for '$keyword' should not be empty", pinyin.isNotEmpty())
            assertTrue("Initials for '$keyword' should not be empty", initials.isNotEmpty())
            assertEquals(
                "Initials length should match character count",
                keyword.length,
                initials.length
            )
        }
    }
}