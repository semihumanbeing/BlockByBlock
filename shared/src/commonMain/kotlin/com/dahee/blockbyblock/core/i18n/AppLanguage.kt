package com.dahee.blockbyblock.core.i18n

enum class AppLanguage(val code: String, val displayName: String) {
    KO("ko", "한국어"),
    EN("en", "English");

    fun next(): AppLanguage = when (this) {
        KO -> EN
        EN -> KO
    }
}
