package com.dahee.blockbyblock

import android.content.Context
import android.os.Build
import com.dahee.blockbyblock.core.i18n.AppLanguage
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

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
        get() = if (Locale.getDefault().language.startsWith("ko", ignoreCase = true)) {
            AppLanguage.KO
        } else {
            AppLanguage.EN
        }
    override val defaultBaseUrl: String = "http://10.0.2.2:8000"
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