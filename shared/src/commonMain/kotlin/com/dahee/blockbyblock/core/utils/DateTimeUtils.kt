package com.dahee.blockbyblock.core.utils

import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.AppStrings
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Parses an ISO-8601 string (assumed UTC from server, e.g. "2026-09-11T05:20:00Z") into an [Instant].
 * Robustly normalizes strings missing trailing 'Z'.
 */
fun parseUtcInstant(isoString: String?): Instant? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val trimmed = isoString.trim()
        val normalized = if (!trimmed.endsWith("Z", ignoreCase = true) &&
            !trimmed.contains("+") &&
            !trimmed.substringAfter("T", "").contains("-")
        ) {
            trimmed + "Z"
        } else {
            trimmed
        }
        Instant.parse(normalized)
    } catch (_: Throwable) {
        null
    }
}

/**
 * Converts a UTC ISO string to the device's local [LocalDateTime] using the specified [timeZone].
 */
fun toLocalLocalDateTime(
    isoString: String?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime? {
    val instant = parseUtcInstant(isoString) ?: return null
    return try {
        instant.toLocalDateTime(timeZone)
    } catch (_: Throwable) {
        null
    }
}

/**
 * Converts a UTC ISO string to the device's local [LocalDate] using the specified [timeZone].
 */
fun toLocalLocalDate(
    isoString: String?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDate? {
    return toLocalLocalDateTime(isoString, timeZone)?.date
}

/**
 * Parses an ISO-8601 UTC string to epoch milliseconds.
 */
fun parseIsoToEpochMillis(isoString: String): Long {
    return parseUtcInstant(isoString)?.toEpochMilliseconds() ?: 0L
}

/**
 * Returns current epoch milliseconds.
 */
fun getCurrentEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

/**
 * Returns current instant as [Instant].
 */
fun getCurrentInstant(): Instant = Instant.fromEpochMilliseconds(getCurrentEpochMillis())

/**
 * Returns the current date formatted as ISO "YYYY-MM-DD" in the user's local timezone.
 */
fun getCurrentDateIso(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    return try {
        val now = getCurrentInstant()
        now.toLocalDateTime(timeZone).date.toString()
    } catch (_: Throwable) {
        val now = getCurrentInstant()
        now.toLocalDateTime(TimeZone.UTC).date.toString()
    }
}

/**
 * Returns the current device system timezone identifier (e.g., "Asia/Seoul", "America/New_York").
 */
fun getCurrentTimeZone(): String {
    return try {
        TimeZone.currentSystemDefault().id
    } catch (_: Throwable) {
        "UTC"
    }
}

/**
 * Converts a UTC ISO timestamp (e.g. from server createdAt, readAt) to a local date string "YYYY-MM-DD".
 */
fun formatIsoToLocalDateString(
    isoString: String?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    if (isoString.isNullOrBlank()) return ""
    val localDate = toLocalLocalDate(isoString, timeZone)
    return localDate?.toString() ?: isoString.takeWhile { it != 'T' }
}

/**
 * Converts a UTC ISO timestamp to a user-friendly local date-time string "YYYY.MM.DD HH:mm".
 */
fun formatIsoToLocalDateTimeString(
    isoString: String?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val ldt = toLocalLocalDateTime(isoString, timeZone) ?: return ""
    val y = ldt.year
    @Suppress("DEPRECATION")
    val m = ldt.monthNumber.toString().padStart(2, '0')
    @Suppress("DEPRECATION")
    val d = ldt.dayOfMonth.toString().padStart(2, '0')
    val hh = ldt.hour.toString().padStart(2, '0')
    val mm = ldt.minute.toString().padStart(2, '0')
    return "$y.$m.$d $hh:$mm"
}

/**
 * Converts a UTC ISO timestamp to a localized date string (e.g. "2026년 9월 11일" or "Sep 11, 2026").
 */
fun formatIsoToLocalDisplayDate(
    isoString: String?,
    language: AppLanguage = AppLanguage.KO,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val ldt = toLocalLocalDateTime(isoString, timeZone) ?: return ""
    val y = ldt.year
    @Suppress("DEPRECATION")
    val m = ldt.monthNumber
    @Suppress("DEPRECATION")
    val d = ldt.dayOfMonth
    return if (language == AppLanguage.EN) {
        val monthShort = when (m) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "$m"
        }
        "$monthShort $d, $y"
    } else {
        "${y}년 ${m}월 ${d}일"
    }
}

/**
 * Converts a UTC ISO timestamp to local time "HH:mm".
 */
fun formatIsoToLocalTime(
    isoString: String?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val ldt = toLocalLocalDateTime(isoString, timeZone) ?: return ""
    val hh = ldt.hour.toString().padStart(2, '0')
    val mm = ldt.minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

/**
 * Formats relative time ("방금 전", "5분 전", "2시간 전", "어제", "N일 전", "YYYY.MM.DD")
 * converted to the user's local timezone.
 */
fun formatRelativeTime(
    isoString: String,
    strings: AppStrings,
    nowInstant: Instant = getCurrentInstant(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    if (isoString.isBlank()) return ""
    val instant = parseUtcInstant(isoString) ?: return ""
    val nowMillis = nowInstant.toEpochMilliseconds()
    val eventMillis = instant.toEpochMilliseconds()
    val diffMillis = (nowMillis - eventMillis).coerceAtLeast(0L)
    val diffMinutes = diffMillis / 60_000L

    if (diffMinutes < 1L) {
        return strings.notificationTimeJustNow
    }
    if (diffMinutes < 60L) {
        return strings.notificationTimeMinutesAgo(diffMinutes)
    }

    val eventLocal = instant.toLocalDateTime(timeZone)
    val nowLocal = nowInstant.toLocalDateTime(timeZone)
    val daysDiff = (nowLocal.date.toEpochDays().toInt() - eventLocal.date.toEpochDays().toInt()).coerceAtLeast(0)
    val diffHours = diffMinutes / 60L

    if (daysDiff == 0) {
        return strings.notificationTimeHoursAgo(diffHours)
    }
    if (daysDiff == 1) {
        return if (diffHours < 3L) strings.notificationTimeHoursAgo(diffHours) else strings.notificationTimeYesterday
    }
    if (daysDiff < 7) {
        return strings.notificationTimeDaysAgo(daysDiff.toLong())
    }

    val y = eventLocal.year
    @Suppress("DEPRECATION")
    val m = eventLocal.monthNumber.toString().padStart(2, '0')
    @Suppress("DEPRECATION")
    val d = eventLocal.dayOfMonth.toString().padStart(2, '0')
    return "$y.$m.$d"
}
