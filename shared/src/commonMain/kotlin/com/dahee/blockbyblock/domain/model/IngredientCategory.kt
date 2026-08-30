package com.dahee.blockbyblock.domain.model

enum class IngredientCategory(
    val nameKo: String,
    val nameEn: String
) {
    MEAT_SEAFOOD("육류 / 해산물", "Meat & Seafood"),
    VEGETABLE("채소 / 과일", "Vegetables & Fruits"),
    GRAIN_CARB("곡류 / 탄수화물", "Grains & Carbs"),
    SAUCE_SEASONING("소스 / 양념", "Sauce & Seasoning"),
    DAIRY_EGG("유제품 / 알류", "Dairy & Eggs"),
    OTHER("기타 재료", "Other Ingredients");

    val displayNameKo: String get() = nameKo
    val displayNameEn: String get() = nameEn

    fun displayName(isKorean: Boolean): String = if (isKorean) displayNameKo else displayNameEn
}
