package com.dahee.blockbyblock.data.remote

import com.dahee.blockbyblock.getPlatform
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
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

        defaultRequest {
            val fullUrl = if (url.toString().startsWith("http")) {
                url.toString()
            } else {
                "${baseUrl.trimEnd('/')}/${url.toString().trimStart('/')}"
            }
            // url is already built or relative
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            val token = TokenStorage.getAccessToken()
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    fun endpoint(path: String): String {
        val base = baseUrl.trimEnd('/')
        val rel = path.trimStart('/')
        return "$base/$rel"
    }
}
