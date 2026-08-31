package com.dahee.blockbyblock.core.i18n

import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf { AppLanguage.KO }
val LocalStrings = compositionLocalOf<AppStrings> { KoStrings }

fun getStrings(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.KO -> KoStrings
    AppLanguage.EN -> EnStrings
}
