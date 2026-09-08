package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.TokenStorage
import com.dahee.blockbyblock.data.remote.dto.LoginRequest
import com.dahee.blockbyblock.data.remote.dto.LoginResponse
import com.dahee.blockbyblock.data.remote.dto.RefreshTokenRequest
import com.dahee.blockbyblock.data.remote.dto.SignUpRequest
import com.dahee.blockbyblock.data.remote.dto.SignUpResponse
import com.dahee.blockbyblock.data.remote.dto.SocialLoginRequest
import com.dahee.blockbyblock.data.remote.dto.SuccessResponse
import com.dahee.blockbyblock.data.remote.dto.TokenResponse
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class AuthApiService {

    suspend fun login(request: LoginRequest): Result<LoginResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/auth/login")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<LoginResponse>>()
            TokenStorage.setTokens(body.data.accessToken, body.data.refreshToken)
            TokenStorage.setUserInfo(body.data.user.id, body.data.user.lang)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun signUp(request: SignUpRequest): Result<SignUpResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/auth/signup")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<SignUpResponse>>()
            TokenStorage.setTokens(body.data.accessToken, body.data.refreshToken)
            TokenStorage.setUserInfo(body.data.userId, "KO")
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun socialLogin(request: SocialLoginRequest): Result<LoginResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/auth/social-login")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<LoginResponse>>()
            TokenStorage.setTokens(body.data.accessToken, body.data.refreshToken)
            TokenStorage.setUserInfo(body.data.user.id, body.data.user.lang)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<TokenResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/auth/refresh")) {
            setBody(RefreshTokenRequest(refreshToken))
        }
        if (response.status.isSuccess()) {
            val body = response.body<ApiResponse<TokenResponse>>()
            TokenStorage.setTokens(body.data.accessToken, body.data.refreshToken)
            body.data
        } else {
            throw parseError(response)
        }
    }

    suspend fun logout(): Result<Boolean> = runCatching {
        val refreshToken = TokenStorage.getRefreshToken() ?: ""
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/auth/logout")) {
            setBody(RefreshTokenRequest(refreshToken))
        }
        TokenStorage.clearTokens()
        response.status.isSuccess()
    }

    private suspend fun parseError(response: HttpResponse): com.dahee.blockbyblock.data.remote.error.ApiError {
        return ApiClient.parseError(response)
    }
}
