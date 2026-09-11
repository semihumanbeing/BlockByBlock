package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.data.remote.service.NotificationApiService
import com.dahee.blockbyblock.domain.model.AppNotification
import com.dahee.blockbyblock.domain.model.NotificationPagedResult
import com.dahee.blockbyblock.domain.model.toDomain
import com.dahee.blockbyblock.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetworkNotificationRepository(
    private val apiService: NotificationApiService = NotificationApiService()
) : NotificationRepository {

    private val _unreadCount = MutableStateFlow(0L)
    override val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    override suspend fun fetchUnreadCount(): Result<Long> {
        val result = apiService.getUnreadCount()
        return result.map { response ->
            val count = response.actualCount
            _unreadCount.value = count
            count
        }
    }

    override suspend fun getNotifications(
        unreadOnly: Boolean,
        page: Int,
        size: Int
    ): Result<NotificationPagedResult> {
        val result = apiService.getNotifications(unreadOnly = unreadOnly, page = page, size = size)
        return result.map { listRes ->
            if (listRes.unreadCount != null) {
                _unreadCount.value = listRes.unreadCount.toLong()
            }
            NotificationPagedResult(
                notifications = listRes.actualContent.map { it.toDomain() },
                page = listRes.page,
                size = listRes.size,
                totalElements = listRes.actualTotalElements,
                totalPages = listRes.totalPages,
                hasNext = listRes.actualHasNext
            )
        }
    }

    override suspend fun markAsRead(id: Long): Result<Boolean> {
        val result = apiService.markAsRead(id)
        return result.map {
            _unreadCount.update { (it - 1).coerceAtLeast(0L) }
            true
        }
    }

    override suspend fun markAllAsRead(): Result<Boolean> {
        val result = apiService.markAllAsRead()
        return result.map {
            _unreadCount.value = 0L
            true
        }
    }

    override suspend fun deleteNotification(id: Long, wasUnread: Boolean): Result<Boolean> {
        val result = apiService.deleteNotification(id)
        return result.map {
            if (wasUnread) {
                _unreadCount.update { (it - 1).coerceAtLeast(0L) }
            }
            true
        }
    }

    override suspend fun deleteAllNotifications(): Result<Boolean> {
        val result = apiService.deleteAllNotifications()
        return result.map {
            _unreadCount.value = 0L
            true
        }
    }
}
