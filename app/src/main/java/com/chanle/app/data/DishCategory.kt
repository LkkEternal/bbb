package com.chanle.app.data

enum class DishCategory(
    val label: String,
    val symbol: String,
    val color: Long,
) {
    HOME("家常菜", "家", 0xFFD96C4F),
    SOUP("汤", "汤", 0xFF4F8A8B),
    STAPLE("主食", "饭", 0xFFC99A3E),
    PASTRY("面点", "面", 0xFFB57A4A),
    COLD("凉菜", "拌", 0xFF6B8E5A),
    DESSERT("甜品", "甜", 0xFFC97B9A),
    WESTERN("西餐", "西", 0xFF7A6FB0),
    SNACK("小吃", "小", 0xFFD08A3E);

    companion object {
        fun fromLabel(label: String): DishCategory? = entries.firstOrNull { it.label == label }

        fun labelOf(label: String): String = fromLabel(label)?.label ?: HOME.label
    }
}
