package com.dahee.blockbyblock.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.notosans_kr_regular
import org.jetbrains.compose.resources.Font

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.Surface,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.Accent,
    onSecondary = AppColors.TextPrimary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    error = AppColors.Danger,
    onError = AppColors.Surface
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun getKoreanFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.notosans_kr_regular, weight = FontWeight.Normal),
        Font(Res.font.notosans_kr_regular, weight = FontWeight.Medium),
        Font(Res.font.notosans_kr_regular, weight = FontWeight.SemiBold),
        Font(Res.font.notosans_kr_regular, weight = FontWeight.Bold)
    )
}

@Composable
fun getAppTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(fontFamily = fontFamily, fontSize = 57.sp),
        displayMedium = TextStyle(fontFamily = fontFamily, fontSize = 45.sp),
        displaySmall = TextStyle(fontFamily = fontFamily, fontSize = 36.sp),
        headlineLarge = TextStyle(fontFamily = fontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold),
        headlineMedium = TextStyle(fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontFamily = fontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontFamily = fontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = fontFamily, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = fontFamily, fontSize = 12.sp),
        labelLarge = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    )
}

@Composable
fun BlockByBlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val fontFamily = getKoreanFontFamily()
    val typography = getAppTypography(fontFamily)

    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = AppShapes,
        typography = typography
    ) {
        ProvideTextStyle(value = TextStyle(fontFamily = fontFamily, color = AppColors.TextPrimary)) {
            content()
        }
    }
}
