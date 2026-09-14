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
    private const val DEFAULT_SERVER_URL = "https://api.blockbyblock-mealprep.com"
    private var customBaseUrl: String? = DEFAULT_SERVER_URL

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

    init {
        TokenStorage.setTokenUpdateListener { _, _ ->
            resetClient()
        }
    }

    private val refreshMutex = Mutex()

    // Independent lightweight HTTP client for token refresh to avoid recursive interceptor loops
    val refreshClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    private fun createHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(Logging) {
            level = LogLevel.NONE
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
                    !urlStr.contains("/auth/login") &&
                    !urlStr.contains("/auth/signup") &&
                    !urlStr.contains("/auth/social-login") &&
                    !urlStr.contains("/auth/password-reset") &&
                    !urlStr.contains("/health")
                }
            }
        }

        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            val token = TokenStorage.getAccessToken()
            val urlString = url.toString()
            val isAuthEndpoint = urlString.contains("/auth/login") ||
                                 urlString.contains("/auth/signup") ||
                                 urlString.contains("/auth/social-login") ||
                                 urlString.contains("/auth/password-reset") ||
                                 urlString.contains("/health")
            if (!token.isNullOrBlank() && !isAuthEndpoint) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    private var _client: HttpClient = createHttpClient()

    val client: HttpClient
        get() = _client

    fun resetClient() {
        try {
            _client.close()
        } catch (_: Throwable) {}
        _client = createHttpClient()
    }

    fun updateAuthTokens(access: String, refresh: String) {
        TokenStorage.setTokens(access, refresh)
    }

    fun clearAuthTokens() {
        TokenStorage.clearTokens()
    }

    suspend fun parseError(response: HttpResponse): ApiError {
        val text = response.bodyAsText()
        val error = ApiError.fromHttpResponse(response.status.value, text)
        val currentLang = TokenStorage.getUserLang()?.let {
            try { com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(it) } catch (_: Throwable) { null }
        } ?: getPlatform().defaultLanguage
        val strings = com.dahee.blockbyblock.core.i18n.getStrings(currentLang)

        if (response.status.value == 429) {
            if (error.code != ErrorCode.ACCOUNT_LOCKED && !error.isAccountLocked()) {
                showToast(strings.errorTooManyRequests)
            }
        } else if (response.status.value == 403) {
            showToast(strings.errorForbidden)
        } else if (response.status.value >= 500) {
            showToast(strings.errorInternalServer)
        }
        return error
    }

    fun endpoint(path: String): String {
        val base = baseUrl.trimEnd('/')
        val rel = path.trimStart('/')
        return "$base/$rel"
    }
}
