package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.AppNotification
import com.dahee.blockbyblock.domain.model.NotificationPagedResult
import com.dahee.blockbyblock.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryNotificationRepository(
    initialNotifications: List<AppNotification> = emptyList()
) : NotificationRepository {

    private val notifications = initialNotifications.toMutableList()
    private val _unreadCount = MutableStateFlow(notifications.count { !it.isRead }.toLong())
    override val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    fun addNotification(notification: AppNotification) {
        notifications.add(0, notification)
        _unreadCount.value = notifications.count { !it.isRead }.toLong()
    }

    override suspend fun fetchUnreadCount(): Result<Long> {
        val count = notifications.count { !it.isRead }.toLong()
        _unreadCount.value = count
        return Result.success(count)
    }

    override suspend fun getNotifications(
        unreadOnly: Boolean,
        page: Int,
        size: Int
    ): Result<NotificationPagedResult> {
        val filtered = if (unreadOnly) notifications.filter { !it.isRead } else notifications
        val totalElements = filtered.size.toLong()
        val totalPages = ((filtered.size + size - 1) / size).coerceAtLeast(1)
        val fromIndex = ((page - 1) * size).coerceAtMost(filtered.size)
        val toIndex = (fromIndex + size).coerceAtMost(filtered.size)
        val pageItems = if (fromIndex < filtered.size) filtered.subList(fromIndex, toIndex) else emptyList()
        val hasNext = page < totalPages

        return Result.success(
            NotificationPagedResult(
                notifications = pageItems,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = hasNext
            )
        )
    }

    override suspend fun markAsRead(id: Long): Result<Boolean> {
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            _unreadCount.value = notifications.count { !it.isRead }.toLong()
        }
        return Result.success(true)
    }

    override suspend fun markAllAsRead(): Result<Boolean> {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
        _unreadCount.value = 0L
        return Result.success(true)
    }

    override suspend fun deleteNotification(id: Long, wasUnread: Boolean): Result<Boolean> {
        notifications.removeAll { it.id == id }
        _unreadCount.value = notifications.count { !it.isRead }.toLong()
        return Result.success(true)
    }

    override suspend fun deleteAllNotifications(): Result<Boolean> {
        notifications.clear()
        _unreadCount.value = 0L
        return Result.success(true)
    }
}
