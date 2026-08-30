package com.dahee.blockbyblock.domain.model

enum class IngredientStatus(
    val displayNameKo: String,
    val displayNameEn: String
) {
    IN_STOCK("보유중", "In Stock"),
    CONSUMED("소진됨", "Consumed"),
    SHOPPING_CART("장바구니", "Shopping Cart");

    fun displayName(isKorean: Boolean): String = if (isKorean) displayNameKo else displayNameEn
}
