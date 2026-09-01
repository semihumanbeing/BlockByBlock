package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val isWeb: Boolean = true
    override val defaultLanguage: AppLanguage
        get() = AppLanguage.KO
}

actual fun getPlatform(): Platform = WasmPlatform()