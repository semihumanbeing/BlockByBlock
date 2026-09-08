package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.DeviceResponse
import com.dahee.blockbyblock.data.remote.dto.RegisterDeviceRequest
import com.dahee.blockbyblock.data.remote.dto.SuccessResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class DeviceApiService {

    suspend fun registerDevice(
        fcmToken: String,
        deviceType: String,
        timezone: String? = null
    ): Result<DeviceResponse> = runCatching {
        val request = RegisterDeviceRequest(
            fcmToken = fcmToken,
            deviceType = deviceType,
            timezone = timezone
        )
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/devices")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<DeviceResponse>>()
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun unregisterDevice(fcmToken: String): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/devices")) {
            parameter("fcmToken", fcmToken)
        }
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
