package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import kotlinx.browser.localStorage

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val isWeb: Boolean = true
    override val defaultLanguage: AppLanguage
        get() = AppLanguage.KO
    override val defaultBaseUrl: String = "http://168.110.30.132:8000"

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