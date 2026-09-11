package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.NotificationListResponse
import com.dahee.blockbyblock.data.remote.dto.NotificationResponse
import com.dahee.blockbyblock.data.remote.dto.UnreadCountResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class NotificationApiService {

    suspend fun getNotifications(
        unreadOnly: Boolean = false,
        page: Int = 1,
        size: Int = 20
    ): Result<NotificationListResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/notifications")) {
            parameter("unreadOnly", unreadOnly)
            parameter("page", page)
            parameter("size", size)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<NotificationListResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun getUnreadCount(): Result<UnreadCountResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/notifications/unread-count"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<UnreadCountResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun markAsRead(id: Long): Result<NotificationResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/notifications/$id/read"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<NotificationResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun markAllAsRead(): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/notifications/read-all"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteNotification(id: Long): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/notifications/$id"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteAllNotifications(): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/notifications"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    private suspend fun parseError(response: HttpResponse): com.dahee.blockbyblock.data.remote.error.ApiError {
        return ApiClient.parseError(response)
    }
}
