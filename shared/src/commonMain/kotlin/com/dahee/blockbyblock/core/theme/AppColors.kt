package com.dahee.blockbyblock.core.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Brand Primary & Natural Warm Accents (Warm Home Cooking & Sage Forest)
    val Primary = Color(0xFF3D7A68)         // Sage Forest Green
    val PrimaryDark = Color(0xFF295648)     // Deep Forest Green
    val PrimaryLight = Color(0xFFE8F3EE)    // Soft Milk Sage Tint 100
    val Accent = Color(0xFFE07A5F)          // Warm Terracotta Brick
    val AccentLight = Color(0xFFFBECE8)     // Soft Terracotta Tint
    val BlockYellow = Color(0xFFF4D06F)     // Warm Honey Butter Yellow
    val BlockYellowLight = Color(0xFFFEF8E8) // Vanilla Oat Tint
    val Success = Color(0xFF5B9E7A)         // Fresh Herb Green
    val SuccessLight = Color(0xFFEAF5EE)    // Herb Tint
    val Danger = Color(0xFFD9534F)          // Warm Brick Red
    val DangerLight = Color(0xFFFCEEED)     // Rose Tint

    // Neutrals & Surfaces (Warm Ivory & Espresso Charcoal)
    val TextPrimary = Color(0xFF2C241E)     // Warm Espresso Charcoal
    val TextSecondary = Color(0xFF6B5E54)   // Warm Earth Mocha
    val TextMuted = Color(0xFFA89F91)       // Warm Oat Gray
    val Background = Color(0xFFFAF6F0)      // Warm Cozy Milk Ivory
    val Surface = Color(0xFFFFFFFF)         // Clean Pure Surface
    val SurfaceVariant = Color(0xFFF3EEE6)  // Warm Linen Biscuit
    val Border = Color(0xFFE5DDD3)          // Warm Oatmeal Border
    val BorderFocus = Color(0xFFA89F91)
    val Shadow = Color(0x2E2C241E)

    // Mold Silicone Natural Pastel Colors (Natural Warm Kitchen 7 Pastel Palette)
    val MoldSkyBlue = Color(0xFFA3C4D3)     // Warm Mist Blue
    val MoldMint = Color(0xFFA8D5BA)        // Sage Olive Mint
    val MoldCoralPink = Color(0xFFF5B7B1)   // Warm Blush Rose
    val MoldButterYellow = Color(0xFFF9E79F)// Warm Honey Butter Yellow
    val MoldLavender = Color(0xFFD7BDE2)    // Soft Berry Lilac
    val MoldPeach = Color(0xFFFAD7A0)       // Apricot Peach
    val MoldCoolGray = Color(0xFFD5DBDB)    // Warm Mineral Sand Gray

    val MoldColors = listOf(
        MoldSkyBlue,
        MoldMint,
        MoldCoralPink,
        MoldButterYellow,
        MoldLavender,
        MoldPeach,
        MoldCoolGray
    )

    fun hexToColor(hex: String, defaultColor: Color = MoldSkyBlue): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(colorInt or 0x00000000FF000000)
            } else {
                Color(colorInt)
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt().coerceIn(0, 255)
        val green = (color.green * 255).toInt().coerceIn(0, 255)
        val blue = (color.blue * 255).toInt().coerceIn(0, 255)
        val hexDigits = "0123456789ABCDEF"
        fun byteToHex(n: Int): String = "${hexDigits[(n shr 4) and 0xF]}${hexDigits[n and 0xF]}"
        return "#${byteToHex(red)}${byteToHex(green)}${byteToHex(blue)}"
    }
}
