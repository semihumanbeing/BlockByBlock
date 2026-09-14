package com.dahee.blockbyblock.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val status: IngredientStatus = IngredientStatus.STOCK,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

fun guessCategoryByName(name: String): IngredientCategory {
    val lower = name.lowercase()
    return when {
        listOf("닭", "소고기", "돼지", "연어", "새우", "참치", "고기", "beef", "chicken", "pork", "salmon", "shrimp", "fish").any { lower.contains(it) } ->
            IngredientCategory.MEAT_SEAFOOD
        listOf("양파", "브로콜리", "당근", "파프리카", "마늘", "대파", "버섯", "아보카도", "시금치", "블루베리", "onion", "carrot", "broccoli", "spinach", "vegetable").any { lower.contains(it) } ->
            IngredientCategory.VEGETABLE
        listOf("밥", "오트밀", "파스타", "면", "고구마", "감자", "쌀", "rice", "oat", "pasta", "potato", "bread").any { lower.contains(it) } ->
            IngredientCategory.GRAIN_CARB
        listOf("간장", "올리브유", "기름", "소스", "양념", "후추", "육수", "페스토", "퓨레", "sauce", "oil", "pepper", "broth", "pesto").any { lower.contains(it) } ->
            IngredientCategory.SAUCE_SEASONING
        listOf("계란", "달걀", "요거트", "치즈", "우유", "버터", "egg", "yogurt", "cheese", "milk").any { lower.contains(it) } ->
            IngredientCategory.DAIRY_EGG
        else -> IngredientCategory.OTHER
    }
}
