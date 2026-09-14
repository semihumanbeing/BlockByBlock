package com.dahee.blockbyblock

import android.content.Context
import android.os.Build
import com.dahee.blockbyblock.core.i18n.AppLanguage
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import android.content.res.Resources
import com.dahee.blockbyblock.core.utils.getCurrentTimeZone

object AndroidAppContextHolder {
    var context: Context? = null
}

class AndroidPlatform : Platform {
    companion object {
        private val inMemoryPrefs = ConcurrentHashMap<String, String>()
    }

    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isWeb: Boolean = false
    override val defaultLanguage: AppLanguage
        get() {
            // 1. Detect timezone for overseas access (e.g. Europe/Madrid, America/New_York)
            val tz = try { getCurrentTimeZone() } catch (_: Throwable) { "" }
            val isKoreanTz = tz.isEmpty() || tz == "UTC" ||
                tz.contains("Seoul", ignoreCase = true) ||
                tz.contains("Pyongyang", ignoreCase = true) ||
                tz.contains("ROK", ignoreCase = true) ||
                tz.contains("KST", ignoreCase = true)

            // 2. Detect system language preference from Android configuration
            val ctx = AndroidAppContextHolder.context
            val systemLocale = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ctx?.resources?.configuration?.locales?.get(0)
                        ?: Resources.getSystem().configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    ctx?.resources?.configuration?.locale
                        ?: Resources.getSystem().configuration.locale
                }
            } catch (_: Throwable) {
                Locale.getDefault()
            }
            val lang = systemLocale?.language ?: Locale.getDefault().language
            val isKoreanLang = lang.startsWith("ko", ignoreCase = true)

            // If user is in an overseas timezone (e.g. Europe/Madrid) or device language is non-Korean, default to EN
            return if (isKoreanLang && isKoreanTz) {
                AppLanguage.KO
            } else {
                AppLanguage.EN
            }
        }
    override val defaultBaseUrl: String = "https://api.blockbyblock-mealprep.com"
    override val deviceType: String? = "ANDROID"

    override fun getPersistentString(key: String): String? {
        val ctx = AndroidAppContextHolder.context
        return if (ctx != null) {
            ctx.getSharedPreferences("bbb_prefs", Context.MODE_PRIVATE).getString(key, null)
        } else {
            inMemoryPrefs[key]
        }
    }

    override fun setPersistentString(key: String, value: String?) {
        val ctx = AndroidAppContextHolder.context
        if (value != null) {
            inMemoryPrefs[key] = value
            ctx?.getSharedPreferences("bbb_prefs", Context.MODE_PRIVATE)
                ?.edit()?.putString(key, value)?.apply()
        } else {
            inMemoryPrefs.remove(key)
            ctx?.getSharedPreferences("bbb_prefs", Context.MODE_PRIVATE)
                ?.edit()?.remove(key)?.apply()
        }
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()