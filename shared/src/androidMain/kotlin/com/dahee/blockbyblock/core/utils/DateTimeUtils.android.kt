package com.dahee.blockbyblock.core.utils

import java.util.Calendar

actual fun getCurrentDateIso(): String {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val mm = if (m < 10) "0$m" else "$m"
    val dd = if (d < 10) "0$d" else "$d"
    return "$y-$mm-$dd"
}

actual fun getCurrentEpochMillis(): Long = System.currentTimeMillis()
