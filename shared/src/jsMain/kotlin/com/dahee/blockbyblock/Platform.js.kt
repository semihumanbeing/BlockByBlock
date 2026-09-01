package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import web.navigator.navigator

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
    override val isWeb: Boolean = true
    override val defaultLanguage: AppLanguage
        get() {
            val lang = navigator.language.lowercase()
            return if (lang.startsWith("ko")) AppLanguage.KO else AppLanguage.EN
        }
}

actual fun getPlatform(): Platform = JsPlatform()