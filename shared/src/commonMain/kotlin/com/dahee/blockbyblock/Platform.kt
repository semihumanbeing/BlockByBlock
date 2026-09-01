package com.dahee.blockbyblock

import com.dahee.blockbyblock.core.i18n.AppLanguage

interface Platform {
    val name: String
    val isWeb: Boolean
    val defaultLanguage: AppLanguage
}

expect fun getPlatform(): Platform