package com.dahee.blockbyblock.data.remote

import com.dahee.blockbyblock.data.remote.dto.RefreshTokenRequest
import com.dahee.blockbyblock.data.remote.dto.TokenResponse
import com.dahee.blockbyblock.data.remote.error.ApiError
import com.dahee.blockbyblock.data.remote.error.ErrorCode
import com.dahee.blockbyblock.getPlatform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object ApiClient {
    private var customBaseUrl: String? = null

    var baseUrl: String
        get() = customBaseUrl ?: getPlatform().defaultBaseUrl
        set(value) {
            customBaseUrl = value
        }

    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private var toastHandler: ((message: String) -> Unit)? = null

    fun setToastHandler(handler: ((message: String) -> Unit)?) {
        toastHandler = handler
    }

    fun showToast(message: String) {
        toastHandler?.invoke(message)
    }

    fun setAuthFailureHandler(handler: ((reason: String?) -> Unit)?) {
        TokenStorage.setAuthFailureHandler(handler)
    }

    fun forceLogout(reason: String? = null) {
        TokenStorage.forceLogout(reason)
    }

    private val refreshMutex = Mutex()

    // Independent lightweight HTTP client for token refresh to avoid recursive interceptor loops
    val refreshClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    println("[HTTP] $message")
                }
            }
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val access = TokenStorage.getAccessToken()
                    val refresh = TokenStorage.getRefreshToken()
                    if (!access.isNullOrBlank()) {
                        BearerTokens(access, refresh ?: "")
                    } else null
                }
                refreshTokens {
                    val currentRefresh = TokenStorage.getRefreshToken()
                    if (currentRefresh.isNullOrBlank()) {
                        forceLogout("NO_REFRESH_TOKEN")
                        return@refreshTokens null
                    }

                    val errorBody = response.bodyAsText()
                    if (errorBody.contains(ErrorCode.TOKEN_REUSE_DETECTED)) {
                        forceLogout(ErrorCode.TOKEN_REUSE_DETECTED)
                        return@refreshTokens null
                    }

                    refreshMutex.withLock {
                        val latestAccess = TokenStorage.getAccessToken()
                        if (latestAccess != null && latestAccess != oldTokens?.accessToken) {
                            return@refreshTokens BearerTokens(latestAccess, TokenStorage.getRefreshToken() ?: "")
                        }

                        try {
                            val refreshRes = refreshClient.post(endpoint("api/v1/auth/refresh")) {
                                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                header(HttpHeaders.Authorization, "Bearer $currentRefresh")
                                setBody(RefreshTokenRequest(currentRefresh))
                            }
                            if (refreshRes.status.isSuccess()) {
                                val body = refreshRes.body<ApiResponse<TokenResponse>>()
                                TokenStorage.setTokens(body.data.accessToken, body.data.refreshToken)
                                BearerTokens(body.data.accessToken, body.data.refreshToken)
                            } else {
                                forceLogout("REFRESH_FAILED")
                                null
                            }
                        } catch (e: Throwable) {
                            forceLogout("REFRESH_FAILED")
                            null
                        }
                    }
                }
                sendWithoutRequest { request ->
                    val urlStr = request.url.toString()
                    !urlStr.contains("/auth/login") && !urlStr.contains("/auth/signup") && !urlStr.contains("/auth/social-login")
                }
            }
        }

        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
    }

    suspend fun parseError(response: HttpResponse): ApiError {
        val text = response.bodyAsText()
        val error = ApiError.fromHttpResponse(response.status.value, text)
        if (response.status.value == 429) {
            showToast("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
        } else if (response.status.value == 403) {
            showToast("접근 권한이 없습니다.")
        } else if (response.status.value >= 500) {
            showToast("일시적인 서버 오류가 발생했습니다.")
        }
        return error
    }

    fun endpoint(path: String): String {
        val base = baseUrl.trimEnd('/')
        val rel = path.trimStart('/')
        return "$base/$rel"
    }
}
