package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isWeb: Boolean = false
    override val defaultLanguage: AppLanguage
        get() {
            val preferred = (NSLocale.preferredLanguages.firstOrNull() as? String)?.lowercase() ?: ""
            val current = NSLocale.currentLocale.languageCode.lowercase()
            return if (preferred.startsWith("ko") || current.startsWith("ko")) {
                AppLanguage.KO
            } else {
                AppLanguage.EN
            }
        }
}

actual fun getPlatform(): Platform = IOSPlatform()