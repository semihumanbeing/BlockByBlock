package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.TokenStorage
import com.dahee.blockbyblock.data.remote.dto.OnboardingResponse
import com.dahee.blockbyblock.data.remote.dto.SuccessResponse
import com.dahee.blockbyblock.data.remote.dto.UpdateOnboardingRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateProfileRequest
import com.dahee.blockbyblock.data.remote.dto.UserResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class UserApiService {

    suspend fun getMe(): Result<UserResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/users/me"))
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<UserResponse>>()
            TokenStorage.setUserInfo(body.data.id, body.data.lang)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/users/me/profile")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<UserResponse>>()
            TokenStorage.setUserInfo(body.data.id, body.data.lang)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateTimezone(timezone: String): Result<UserResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/users/me/timezone")) {
            setBody(com.dahee.blockbyblock.data.remote.dto.UpdateTimezoneRequest(timezone))
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<UserResponse>>()
            TokenStorage.setUserInfo(body.data.id, body.data.lang)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateOnboarding(request: UpdateOnboardingRequest): Result<OnboardingResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/users/me/onboarding")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<OnboardingResponse>>()
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun withdraw(): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/users/me"))
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
