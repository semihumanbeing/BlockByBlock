package com.dahee.blockbyblock.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dahee.blockbyblock.data.remote.TokenStorage
import com.dahee.blockbyblock.domain.model.AppNotification
import com.dahee.blockbyblock.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val unreadOnly: Boolean = false,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Long = 0L,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNext: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val customScope: CoroutineScope? = null,
    private val isAuthValid: () -> Boolean = { TokenStorage.isAuthenticated && !TokenStorage.getAccessToken().isNullOrBlank() }
) : ViewModel() {

    private val scope: CoroutineScope
        get() = customScope ?: viewModelScope

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.unreadCount.collect { count ->
                if (isAuthValid()) {
                    _uiState.update { it.copy(unreadCount = count) }
                } else {
                    _uiState.update { it.copy(unreadCount = 0L) }
                }
            }
        }
        if (isAuthValid()) {
            fetchUnreadCount()
            refresh(showLoading = true)
        }
    }

    fun fetchUnreadCount() {
        if (!isAuthValid()) return
        scope.launch {
            repository.fetchUnreadCount()
        }
    }


    fun setFilter(unreadOnly: Boolean) {
        if (_uiState.value.unreadOnly == unreadOnly) return
        _uiState.update { it.copy(unreadOnly = unreadOnly) }
        if (isAuthValid()) {
            refresh(showLoading = true)
        }
    }

    fun refresh(showLoading: Boolean = false) {
        if (!isAuthValid()) {
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            return
        }
        scope.launch {
            _uiState.update {
                it.copy(
                    isLoading = showLoading,
                    isRefreshing = !showLoading,
                    errorMessage = null
                )
            }
            val filter = _uiState.value.unreadOnly
            val result = repository.getNotifications(unreadOnly = filter, page = 1, size = 20)
            result.onSuccess { paged ->
                _uiState.update {
                    it.copy(
                        notifications = paged.notifications,
                        currentPage = 1,
                        totalPages = paged.totalPages,
                        hasNext = paged.hasNext,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = err.message
                    )
                }
            }
            repository.fetchUnreadCount()
        }
    }

    fun loadMore() {
        if (!isAuthValid()) return
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasNext) return
        scope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1
            val result = repository.getNotifications(unreadOnly = state.unreadOnly, page = nextPage, size = 20)
            result.onSuccess { paged ->
                _uiState.update { current ->
                    val existingIds = current.notifications.map { it.id }.toSet()
                    val newUnique = paged.notifications.filter { it.id !in existingIds }
                    current.copy(
                        notifications = current.notifications + newUnique,
                        currentPage = nextPage,
                        totalPages = paged.totalPages,
                        hasNext = paged.hasNext,
                        isLoadingMore = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun markAsRead(notification: AppNotification) {
        if (!isAuthValid() || notification.isRead) return
        _uiState.update { current ->
            val updated = current.notifications.map {
                if (it.id == notification.id) it.copy(isRead = true) else it
            }
            val filtered = if (current.unreadOnly) updated.filter { !it.isRead } else updated
            current.copy(
                notifications = filtered,
                unreadCount = (current.unreadCount - 1).coerceAtLeast(0L)
            )
        }
        scope.launch {
            repository.markAsRead(notification.id)
        }
    }

    fun markAllAsRead() {
        if (!isAuthValid()) return
        val hadUnread = _uiState.value.unreadCount > 0 || _uiState.value.notifications.any { !it.isRead }
        if (!hadUnread) return
        _uiState.update { current ->
            val updated = if (current.unreadOnly) emptyList() else current.notifications.map { it.copy(isRead = true) }
            current.copy(
                notifications = updated,
                unreadCount = 0L
            )
        }
        scope.launch {
            val result = repository.markAllAsRead()
            if (result.isFailure) {
                refresh()
            }
        }
    }

    fun deleteNotification(notification: AppNotification) {
        if (!isAuthValid()) return
        _uiState.update { current ->
            val updated = current.notifications.filter { it.id != notification.id }
            val countDelta = if (!notification.isRead) 1L else 0L
            current.copy(
                notifications = updated,
                unreadCount = (current.unreadCount - countDelta).coerceAtLeast(0L)
            )
        }
        scope.launch {
            repository.deleteNotification(notification.id, wasUnread = !notification.isRead)
        }
    }

    fun deleteAllNotifications() {
        if (!isAuthValid()) return
        _uiState.update { current ->
            current.copy(
                notifications = emptyList(),
                unreadCount = 0L
            )
        }
        scope.launch {
            repository.deleteAllNotifications()
        }
    }

    fun reset() {
        _uiState.value = NotificationUiState()
    }
}
