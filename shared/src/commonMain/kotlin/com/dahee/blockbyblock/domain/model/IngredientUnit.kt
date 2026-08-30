package com.dahee.blockbyblock.domain.model

enum class IngredientUnit(
    val symbol: String,
    val displayNameKo: String,
    val displayNameEn: String
) {
    GRAM("g", "g (그램)", "g (grams)"),
    LBS("lbs", "lbs (파운드)", "lbs (pounds)"),
    PIECE("개", "개 (수량)", "pcs (pieces)"),
    ML("ml", "ml (밀리리터)", "ml (milliliters)");

    fun displayName(isKorean: Boolean): String = if (isKorean) displayNameKo else displayNameEn
}
