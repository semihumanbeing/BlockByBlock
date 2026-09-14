package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.HealthStatusData
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class HealthApiService {

    /**
     * Checks backend server and database health status without requiring authentication.
     * Endpoints: GET /health or GET /api/v1/health
     * Response: {"code":"SUCCESS","data":{"status":"UP","database":"UP"}}
     */
    suspend fun checkHealth(): Result<HealthStatusData> = runCatching {
        val response: HttpResponse = try {
            ApiClient.client.get(ApiClient.endpoint("api/v1/health"))
        } catch (_: Throwable) {
            ApiClient.client.get(ApiClient.endpoint("health"))
        }

        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<HealthStatusData>>()
            body.data
        } else {
            throw ApiClient.parseError(response)
        }
    }
}
