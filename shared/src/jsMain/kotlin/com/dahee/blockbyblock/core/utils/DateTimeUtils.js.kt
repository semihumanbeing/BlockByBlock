package com.dahee.blockbyblock.core.utils

import kotlin.js.Date

actual fun getCurrentDateIso(): String {
    val d = Date()
    val y = d.getFullYear()
    val m = d.getMonth() + 1
    val day = d.getDate()
    val mm = if (m < 10) "0$m" else "$m"
    val dd = if (day < 10) "0$day" else "$day"
    return "$y-$mm-$dd"
}

actual fun getCurrentEpochMillis(): Long = Date.now().toLong()
