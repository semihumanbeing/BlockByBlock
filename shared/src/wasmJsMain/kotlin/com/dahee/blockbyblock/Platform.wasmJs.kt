package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import kotlinx.browser.localStorage

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { return window.navigator.language || ''; } catch (e) { return ''; } }")
private external fun wasmGetNavigatorLanguage(): String

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val isWeb: Boolean = true
    override val defaultLanguage: AppLanguage
        get() {
            val tz = try { com.dahee.blockbyblock.core.utils.getCurrentTimeZone() } catch (_: Throwable) { "" }
            val isKoreanTz = tz.isEmpty() || tz == "UTC" ||
                tz.contains("Seoul", ignoreCase = true) ||
                tz.contains("Pyongyang", ignoreCase = true) ||
                tz.contains("ROK", ignoreCase = true) ||
                tz.contains("KST", ignoreCase = true)

            val lang = try {
                wasmGetNavigatorLanguage().lowercase()
            } catch (_: Throwable) {
                "ko"
            }
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

actual fun getPlatform(): Platform = WasmPlatform()