package com.dahee.blockbyblock.data.datasource

import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientUnit

/**
 * Predefined master catalog database of ingredients.
 */
object MasterIngredientCatalog {
    val items: List<CatalogIngredient> = listOf(
        // Meat & Seafood
        CatalogIngredient("cat_1", "닭가슴살", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_2", "닭안심", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_3", "다진 소고기", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_4", "소고기 우둔살", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_5", "소고기 차돌박이", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_6", "돼지고기 삼겹살", IngredientUnit.GRAM, 300.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_7", "돼지고기 목살", IngredientUnit.GRAM, 300.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_8", "다진 돼지고기", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_9", "연어 필렛", IngredientUnit.PIECE, 2.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_10", "칵테일 새우", IngredientUnit.GRAM, 200.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_11", "참치캔", IngredientUnit.PIECE, 2.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_12", "고등어", IngredientUnit.PIECE, 2.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_13", "오징어", IngredientUnit.PIECE, 1.0, IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_14", "베이컨", IngredientUnit.GRAM, 150.0, IngredientCategory.MEAT_SEAFOOD),

        // Vegetables & Fruits
        CatalogIngredient("cat_15", "양파", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_16", "대파", IngredientUnit.GRAM, 150.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_17", "다진 마늘", IngredientUnit.GRAM, 100.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_18", "당근", IngredientUnit.PIECE, 1.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_19", "브로콜리", IngredientUnit.GRAM, 200.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_20", "파프리카", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_21", "팽이버섯", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_22", "새송이버섯", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_23", "표고버섯", IngredientUnit.GRAM, 100.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_24", "시금치", IngredientUnit.GRAM, 200.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_25", "아보카도", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_26", "방울토마토", IngredientUnit.GRAM, 300.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_27", "감자", IngredientUnit.PIECE, 3.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_28", "고구마", IngredientUnit.PIECE, 3.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_29", "양배추", IngredientUnit.GRAM, 300.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_30", "애호박", IngredientUnit.PIECE, 1.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_31", "오이", IngredientUnit.PIECE, 2.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_32", "블루베리", IngredientUnit.GRAM, 200.0, IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_33", "바나나", IngredientUnit.PIECE, 3.0, IngredientCategory.VEGETABLE),

        // Grains & Carbs
        CatalogIngredient("cat_34", "백미 (쌀)", IngredientUnit.GRAM, 500.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_35", "현미밥", IngredientUnit.PIECE, 3.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_36", "오트밀", IngredientUnit.GRAM, 300.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_37", "통밀 파스타면", IngredientUnit.GRAM, 300.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_38", "식빵 / 통밀빵", IngredientUnit.PIECE, 4.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_39", "두부면", IngredientUnit.PIECE, 2.0, IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_40", "떡볶이 떡", IngredientUnit.GRAM, 300.0, IngredientCategory.GRAIN_CARB),

        // Dairy & Eggs
        CatalogIngredient("cat_41", "계란 (달걀)", IngredientUnit.PIECE, 10.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_42", "무가당 그릭 요거트", IngredientUnit.GRAM, 300.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_43", "우유", IngredientUnit.ML, 500.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_44", "아몬드 브리즈", IngredientUnit.ML, 500.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_45", "슬라이스 치즈", IngredientUnit.PIECE, 5.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_46", "모짜렐라 치즈", IngredientUnit.GRAM, 200.0, IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_47", "무염 버터", IngredientUnit.GRAM, 100.0, IngredientCategory.DAIRY_EGG),

        // Sauce & Seasoning
        CatalogIngredient("cat_48", "진간장", IngredientUnit.ML, 300.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_49", "고추장", IngredientUnit.GRAM, 200.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_50", "된장", IngredientUnit.GRAM, 200.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_51", "올리브유", IngredientUnit.ML, 300.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_52", "참기름", IngredientUnit.ML, 150.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_53", "굴소스", IngredientUnit.ML, 150.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_54", "토마토 퓨레 / 파스타소스", IngredientUnit.ML, 300.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_55", "바질 페스토", IngredientUnit.GRAM, 150.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_56", "사골 육수", IngredientUnit.ML, 500.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_57", "소금 / 후추", IngredientUnit.GRAM, 50.0, IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_58", "알룰로스 / 스테비아", IngredientUnit.ML, 200.0, IngredientCategory.SAUCE_SEASONING),

        // Other
        CatalogIngredient("cat_59", "두부", IngredientUnit.PIECE, 1.0, IngredientCategory.OTHER),
        CatalogIngredient("cat_60", "배추김치", IngredientUnit.GRAM, 300.0, IngredientCategory.OTHER),
        CatalogIngredient("cat_61", "견과류 믹스", IngredientUnit.GRAM, 150.0, IngredientCategory.OTHER),
        CatalogIngredient("cat_62", "프로틴 파우더", IngredientUnit.GRAM, 200.0, IngredientCategory.OTHER)
    )

    fun search(query: String, category: IngredientCategory? = null): List<CatalogIngredient> {
        val trimmed = query.trim()
        return items.filter { item ->
            val matchQuery = trimmed.isBlank() ||
                    item.name.contains(trimmed, ignoreCase = true) ||
                    item.category.displayNameKo.contains(trimmed, ignoreCase = true) ||
                    item.category.displayNameEn.contains(trimmed, ignoreCase = true)

            val matchCategory = category == null || item.category == category
            matchQuery && matchCategory
        }
    }
}
