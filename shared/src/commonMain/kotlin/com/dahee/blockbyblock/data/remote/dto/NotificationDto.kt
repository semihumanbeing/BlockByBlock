package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: Long,
    val type: String? = null,
    val notificationType: String? = null,
    val title: String = "",
    val content: String? = null,
    val body: String? = null,
    val isRead: Boolean = false,
    val targetId: Long? = null,
    val readAt: String? = null,
    val createdAt: String = ""
) {
    val actualType: String
        get() = type ?: notificationType ?: ""

    val actualContent: String
        get() = content ?: body ?: ""
}

@Serializable
data class NotificationListResponse(
    val content: List<NotificationResponse> = emptyList(),
    val items: List<NotificationResponse> = emptyList(),
    val page: Int = 1,
    val size: Int = 20,
    val totalElements: Long? = null,
    val totalCount: Long? = null,
    val totalPages: Int = 1,
    val hasNext: Boolean? = null,
    val unreadCount: Int? = null
) {
    val actualContent: List<NotificationResponse>
        get() = if (content.isNotEmpty()) content else items

    val actualTotalElements: Long
        get() = totalElements ?: totalCount ?: 0L

    val actualHasNext: Boolean
        get() = hasNext ?: (page < totalPages)
}

@Serializable
data class UnreadCountResponse(
    val count: Long? = null,
    val unreadCount: Long? = null
) {
    val actualCount: Long
        get() = count ?: unreadCount ?: 0L
}
