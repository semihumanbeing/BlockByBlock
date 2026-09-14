package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import web.navigator.navigator
import web.storage.localStorage

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
    override val isWeb: Boolean = true
    override val defaultLanguage: AppLanguage
        get() {
            val tz = try { com.dahee.blockbyblock.core.utils.getCurrentTimeZone() } catch (_: Throwable) { "" }
            val isKoreanTz = tz.isEmpty() || tz == "UTC" ||
                tz.contains("Seoul", ignoreCase = true) ||
                tz.contains("Pyongyang", ignoreCase = true) ||
                tz.contains("ROK", ignoreCase = true) ||
                tz.contains("KST", ignoreCase = true)

            val lang = navigator.language.lowercase()
            val isKoreanLang = lang.startsWith("ko")
            return if (isKoreanLang && isKoreanTz) AppLanguage.KO else AppLanguage.EN
        }
    override val defaultBaseUrl: String = "https://api.blockbyblock-mealprep.com"

    override fun getPersistentString(key: String): String? {
        return try {
            localStorage.getItem(key)
        } catch (_: Throwable) {
            null
        }
    }

    override fun setPersistentString(key: String, value: String?) {
        try {
            if (value != null) {
                localStorage.setItem(key, value)
            } else {
                localStorage.removeItem(key)
            }
        } catch (_: Throwable) {}
    }
}

actual fun getPlatform(): Platform = JsPlatform()