package com.dahee.blockbyblock.data.datasource

import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.IngredientCategory

/**
 * Predefined master catalog database of ingredients.
 */
object MasterIngredientCatalog {
    val items: List<CatalogIngredient> = listOf(
        // Meat & Seafood
        CatalogIngredient("cat_1", "닭가슴살", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_2", "닭안심", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_3", "다진 소고기", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_4", "소고기 우둔살", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_5", "소고기 차돌박이", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_6", "돼지고기 삼겹살", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_7", "돼지고기 목살", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_8", "다진 돼지고기", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_9", "연어 필렛", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_10", "칵테일 새우", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_11", "참치캔", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_12", "고등어", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_13", "오징어", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("cat_14", "베이컨", IngredientCategory.MEAT_SEAFOOD),

        // Vegetables & Fruits
        CatalogIngredient("cat_15", "양파", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_16", "대파", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_17", "다진 마늘", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_18", "당근", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_19", "브로콜리", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_20", "파프리카", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_21", "팽이버섯", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_22", "새송이버섯", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_23", "표고버섯", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_24", "시금치", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_25", "아보카도", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_26", "방울토마토", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_27", "감자", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_28", "고구마", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_29", "양배추", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_30", "애호박", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_31", "오이", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_32", "블루베리", IngredientCategory.VEGETABLE),
        CatalogIngredient("cat_33", "바나나", IngredientCategory.VEGETABLE),

        // Grains & Carbs
        CatalogIngredient("cat_34", "백미 (쌀)", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_35", "현미밥", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_36", "오트밀", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_37", "통밀 파스타면", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_38", "식빵 / 통밀빵", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_39", "두부면", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("cat_40", "떡볶이 떡", IngredientCategory.GRAIN_CARB),

        // Dairy & Eggs
        CatalogIngredient("cat_41", "계란 (달걀)", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_42", "무가당 그릭 요거트", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_43", "우유", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_44", "아몬드 브리즈", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_45", "슬라이스 치즈", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_46", "모짜렐라 치즈", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("cat_47", "무염 버터", IngredientCategory.DAIRY_EGG),

        // Sauce & Seasoning
        CatalogIngredient("cat_48", "진간장", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_49", "고추장", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_50", "된장", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_51", "올리브유", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_52", "참기름", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_53", "굴소스", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_54", "토마토 퓨레 / 파스타소스", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_55", "바질 페스토", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_56", "사골 육수", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_57", "소금 / 후추", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("cat_58", "알룰로스 / 스테비아", IngredientCategory.SAUCE_SEASONING),

        // Other
        CatalogIngredient("cat_59", "두부", IngredientCategory.OTHER),
        CatalogIngredient("cat_60", "배추김치", IngredientCategory.OTHER),
        CatalogIngredient("cat_61", "견과류 믹스", IngredientCategory.OTHER),
        CatalogIngredient("cat_62", "프로틴 파우더", IngredientCategory.OTHER)
    )

    val englishItems: List<CatalogIngredient> = listOf(
        // Meat & Seafood
        CatalogIngredient("encat_1", "Chicken Breast", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_2", "Chicken Thigh", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_3", "Ground Turkey", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_4", "Turkey Breast", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_5", "Lean Ground Beef", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_6", "Flank Steak", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_7", "Salmon Fillet", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_8", "Canned Tuna", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_9", "Jumbo Shrimp", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_10", "Cod / White Fish", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_11", "Turkey Bacon", IngredientCategory.MEAT_SEAFOOD),
        CatalogIngredient("encat_12", "Pork Tenderloin", IngredientCategory.MEAT_SEAFOOD),

        // Vegetables & Greens
        CatalogIngredient("encat_13", "Baby Spinach", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_14", "Baby Kale", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_15", "Broccoli Florets", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_16", "Asparagus", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_17", "Brussels Sprouts", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_18", "Bell Peppers", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_19", "Zucchini", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_20", "Cauliflower Rice", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_21", "Portobello Mushrooms", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_22", "Avocado", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_23", "Cherry Tomatoes", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_24", "Sweet Potato", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_25", "Baby Carrots", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_26", "Red Onion", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_27", "Cucumber", IngredientCategory.VEGETABLE),
        CatalogIngredient("encat_28", "Blueberries", IngredientCategory.VEGETABLE),

        // Grains & Carbs
        CatalogIngredient("encat_29", "Rolled Oats", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_30", "Quinoa", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_31", "Brown Rice", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_32", "Jasmine Rice", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_33", "Whole Wheat Pasta", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_34", "Sourdough Bread", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_35", "Whole Grain Bagel", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_36", "Chickpeas (Garbanzo)", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_37", "Black Beans", IngredientCategory.GRAIN_CARB),
        CatalogIngredient("encat_38", "Lentils", IngredientCategory.GRAIN_CARB),

        // Dairy & Eggs
        CatalogIngredient("encat_39", "Whole Eggs", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_40", "Liquid Egg Whites", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_41", "Plain Greek Yogurt", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_42", "Cottage Cheese", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_43", "Almond Milk", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_44", "Oat Milk", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_45", "Cheddar Cheese", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_46", "Feta Cheese", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_47", "Parmesan Cheese", IngredientCategory.DAIRY_EGG),
        CatalogIngredient("encat_48", "Unsalted Butter", IngredientCategory.DAIRY_EGG),

        // Sauces & Seasonings
        CatalogIngredient("encat_49", "Extra Virgin Olive Oil", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_50", "Balsamic Vinegar", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_51", "Dijon Mustard", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_52", "Sriracha Hot Sauce", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_53", "Low-Sodium Soy Sauce", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_54", "Tomato Paste", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_55", "Guacamole", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_56", "Classic Hummus", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_57", "Basil Pesto", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_58", "Salt & Pepper", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_59", "Garlic Powder", IngredientCategory.SAUCE_SEASONING),
        CatalogIngredient("encat_60", "Maple Syrup", IngredientCategory.SAUCE_SEASONING),

        // Other
        CatalogIngredient("encat_61", "Firm Tofu", IngredientCategory.OTHER),
        CatalogIngredient("encat_62", "Tempeh", IngredientCategory.OTHER),
        CatalogIngredient("encat_63", "Peanut Butter", IngredientCategory.OTHER),
        CatalogIngredient("encat_64", "Almond Butter", IngredientCategory.OTHER),
        CatalogIngredient("encat_65", "Whey Protein Powder", IngredientCategory.OTHER),
        CatalogIngredient("encat_66", "Plant Protein Powder", IngredientCategory.OTHER),
        CatalogIngredient("encat_67", "Mixed Nuts", IngredientCategory.OTHER),
        CatalogIngredient("encat_68", "Chia Seeds", IngredientCategory.OTHER),
        CatalogIngredient("encat_69", "Flaxseed", IngredientCategory.OTHER)
    )

    fun search(query: String, category: IngredientCategory? = null, lang: String = "KO"): List<CatalogIngredient> {
        val trimmed = query.trim()
        val targetList = if (lang.equals("EN", ignoreCase = true)) englishItems else items
        return targetList.filter { item ->
            val matchQuery = trimmed.isBlank() ||
                    item.name.contains(trimmed, ignoreCase = true) ||
                    item.category.displayNameKo.contains(trimmed, ignoreCase = true) ||
                    item.category.displayNameEn.contains(trimmed, ignoreCase = true)

            val matchCategory = category == null || item.category == category
            matchQuery && matchCategory
        }
    }
}
