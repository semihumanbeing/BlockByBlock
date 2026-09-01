package com.dahee.blockbyblock

import android.os.Build
import com.dahee.blockbyblock.core.i18n.AppLanguage
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isWeb: Boolean = false
    override val defaultLanguage: AppLanguage
        get() = if (Locale.getDefault().language.startsWith("ko", ignoreCase = true)) {
            AppLanguage.KO
        } else {
            AppLanguage.EN
        }
}

actual fun getPlatform(): Platform = AndroidPlatform()