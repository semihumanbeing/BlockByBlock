package com.dahee.blockbyblock.domain.model

enum class IngredientStatus(
    val displayNameKo: String,
    val displayNameEn: String
) {
    STOCK("보유중", "In Stock"),
    OUT_OF_STOCK("소진됨", "Out of Stock"),
    CART("장바구니", "Shopping Cart");

    fun displayName(isKorean: Boolean): String = if (isKorean) displayNameKo else displayNameEn
}
