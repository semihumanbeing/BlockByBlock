package com.dahee.blockbyblock.core.utils

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentDateIso(): String {
    val date = NSDate()
    val cal = NSCalendar.currentCalendar
    val comps = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = date
    )
    val y = comps.year
    val m = comps.month
    val d = comps.day
    val mm = if (m < 10) "0$m" else "$m"
    val dd = if (d < 10) "0$d" else "$d"
    return "$y-$mm-$dd"
}

actual fun getCurrentEpochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
