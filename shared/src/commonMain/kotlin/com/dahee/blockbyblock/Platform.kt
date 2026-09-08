package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage

interface Platform {
    val name: String
    val isWeb: Boolean
    val defaultLanguage: AppLanguage
    val defaultBaseUrl: String
    val deviceType: String? get() = null
    fun getPersistentString(key: String): String? = null
    fun setPersistentString(key: String, value: String?) {}
}

expect fun getPlatform(): Platform