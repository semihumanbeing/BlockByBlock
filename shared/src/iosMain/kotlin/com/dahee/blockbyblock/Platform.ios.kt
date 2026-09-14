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
            val tz = try { com.dahee.blockbyblock.core.utils.getCurrentTimeZone() } catch (_: Throwable) { "" }
            val isKoreanTz = tz.isEmpty() || tz == "UTC" ||
                tz.contains("Seoul", ignoreCase = true) ||
                tz.contains("Pyongyang", ignoreCase = true) ||
                tz.contains("ROK", ignoreCase = true) ||
                tz.contains("KST", ignoreCase = true)

            val preferred = (NSLocale.preferredLanguages.firstOrNull() as? String)?.lowercase() ?: ""
            val current = NSLocale.currentLocale.languageCode.lowercase()
            val isKoreanLang = preferred.startsWith("ko") || current.startsWith("ko")

            return if (isKoreanLang && isKoreanTz) {
                AppLanguage.KO
            } else {
                AppLanguage.EN
            }
        }
    override val defaultBaseUrl: String = "https://api.blockbyblock-mealprep.com"
    override val deviceType: String? = "IOS"

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