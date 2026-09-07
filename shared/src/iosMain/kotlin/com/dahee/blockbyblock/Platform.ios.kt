package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
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
    override val defaultBaseUrl: String = "http://localhost:8000"

    override fun getPersistentString(key: String): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(key)
    }

    override fun setPersistentString(key: String, value: String?) {
        if (value != null) {
            NSUserDefaults.standardUserDefaults.setObject(value, key)
        } else {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        }
    }
}

actual fun getPlatform(): Platform = IOSPlatform()