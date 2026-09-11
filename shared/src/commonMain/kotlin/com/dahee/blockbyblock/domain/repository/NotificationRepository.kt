package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.NotificationPagedResult
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val unreadCount: StateFlow<Long>
    suspend fun fetchUnreadCount(): Result<Long>
    suspend fun getNotifications(unreadOnly: Boolean = false, page: Int = 1, size: Int = 20): Result<NotificationPagedResult>
    suspend fun markAsRead(id: Long): Result<Boolean>
    suspend fun markAllAsRead(): Result<Boolean>
    suspend fun deleteNotification(id: Long, wasUnread: Boolean = false): Result<Boolean>
    suspend fun deleteAllNotifications(): Result<Boolean>
}
