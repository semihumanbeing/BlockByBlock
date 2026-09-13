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
            val lang = try {
                wasmGetNavigatorLanguage().lowercase()
            } catch (_: Throwable) {
                "ko"
            }
            return if (lang.startsWith("ko")) AppLanguage.KO else AppLanguage.EN
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