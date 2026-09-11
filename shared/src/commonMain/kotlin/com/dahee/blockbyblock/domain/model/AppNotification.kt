package com.dahee.blockbyblock.domain.model

import com.dahee.blockbyblock.core.utils.formatIsoToLocalDateTimeString
import com.dahee.blockbyblock.core.utils.toLocalLocalDateTime
import com.dahee.blockbyblock.data.remote.dto.NotificationResponse
import kotlinx.datetime.LocalDateTime

enum class NotificationType {
    EXPIRING_BLOCK,
    BLOCK_EXPIRED,
    NOTICE,
    SYSTEM,
    UNKNOWN
}

data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val targetId: Long? = null,
    val createdAt: String = "",
    val readAt: String? = null
) {
    val localCreatedAt: LocalDateTime?
        get() = toLocalLocalDateTime(createdAt)

    val formattedLocalCreatedAt: String
        get() = formatIsoToLocalDateTimeString(createdAt)

    val localReadAt: LocalDateTime?
        get() = toLocalLocalDateTime(readAt)

    val formattedLocalReadAt: String
        get() = formatIsoToLocalDateTimeString(readAt)
}

fun NotificationResponse.toDomain(): AppNotification {
    val rawType = actualType
    val typeEnum = try {
        NotificationType.valueOf(rawType)
    } catch (_: Throwable) {
        NotificationType.UNKNOWN
    }
    return AppNotification(
        id = id,
        type = typeEnum,
        title = title,
        content = actualContent,
        isRead = isRead,
        targetId = targetId,
        createdAt = createdAt,
        readAt = readAt
    )
}

data class NotificationPagedResult(
    val notifications: List<AppNotification>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean
)
