package com.dahee.blockbyblock.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class IosGoogleAuthProvider : GoogleAuthProvider {

    private class PresentationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
        override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): UIWindow {
            val scenes = UIApplication.sharedApplication.connectedScenes
            for (scene in scenes) {
                val windowScene = scene as? UIWindowScene ?: continue
                if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                    val window = windowScene.windows
                        .mapNotNull { it as? UIWindow }
                        .firstOrNull { it.isKeyWindow() }
                        ?: (windowScene.windows.firstOrNull() as? UIWindow)
                    if (window != null) return window
                }
            }
            @Suppress("DEPRECATION")
            return UIApplication.sharedApplication.keyWindow
                ?: (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)
                ?: UIWindow()
        }
    }

    private val presentationContextProvider = PresentationContextProvider()

    override suspend fun signIn(): GoogleAuthResult {
        return try {
            val (codeVerifier, codeChallenge) = generatePkcePair()
            val state = NSUUID.UUID().UUIDString

            val authUrlString = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=${GoogleAuthConfig.IOS_CLIENT_ID}" +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&redirect_uri=${GoogleAuthConfig.IOS_REDIRECT_URI}" +
                "&code_challenge=$codeChallenge" +
                "&code_challenge_method=S256" +
                "&state=$state"

            val authUrl = NSURL.URLWithString(authUrlString)
                ?: return GoogleAuthResult.Failure("유효하지 않은 Google 인증 URL입니다.")

            val authCode = requestAuthCode(authUrl, state)
            exchangeCodeForIdToken(authCode, codeVerifier)
        } catch (e: CancellationException) {
            GoogleAuthResult.Cancelled
        } catch (e: Throwable) {
            GoogleAuthResult.Failure(e.message ?: "Google 로그인 실패", e)
        }
    }

    private suspend fun requestAuthCode(authUrl: NSURL, expectedState: String): String =
        suspendCancellableCoroutine { continuation ->
            val session = ASWebAuthenticationSession(
                uRL = authUrl,
                callbackURLScheme = GoogleAuthConfig.IOS_REVERSED_CLIENT_ID,
                completionHandler = { callbackUrl: NSURL?, error: NSError? ->
                    if (error != null) {
                        if (error.domain == ASWebAuthenticationSessionErrorDomain && error.code == 1L) {
                            continuation.resumeWith(kotlin.Result.failure(CancellationException("Login cancelled")))
                        } else {
                            continuation.resumeWith(kotlin.Result.failure(Exception(error.localizedDescription)))
                        }
                        return@ASWebAuthenticationSession
                    }

                    if (callbackUrl != null) {
                        val urlStr = callbackUrl.absoluteString ?: ""
                        val code = extractQueryParam(urlStr, "code")
                        val returnedState = extractQueryParam(urlStr, "state")

                        if (code == null) {
                            val errorParam = extractQueryParam(urlStr, "error")
                            val message = if (errorParam != null) "Google 인증 에러: $errorParam" else "인증 코드가 응답에 포함되어 있지 않습니다."
                            continuation.resumeWith(kotlin.Result.failure(Exception(message)))
                            return@ASWebAuthenticationSession
                        }

                        if (returnedState != null && returnedState != expectedState) {
                            continuation.resumeWith(kotlin.Result.failure(Exception("OAuth state 불일치 (CSRF 방어)")))
                            return@ASWebAuthenticationSession
                        }

                        continuation.resumeWith(kotlin.Result.success(code))
                    } else {
                        continuation.resumeWith(kotlin.Result.failure(Exception("로그인 응답 URL을 수신하지 못했습니다.")))
                    }
                }
            )

            session.presentationContextProvider = presentationContextProvider
            session.prefersEphemeralWebBrowserSession = false

            val started = session.start()
            if (!started) {
                continuation.resumeWith(kotlin.Result.failure(Exception("인증 세션을 시작할 수 없습니다.")))
            }
        }

    private suspend fun exchangeCodeForIdToken(code: String, codeVerifier: String): GoogleAuthResult {
        val client = HttpClient(Darwin)
        return try {
            val response: HttpResponse = client.post("https://oauth2.googleapis.com/token") {
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("client_id", GoogleAuthConfig.IOS_CLIENT_ID)
                            append("code", code)
                            append("code_verifier", codeVerifier)
                            append("grant_type", "authorization_code")
                            append("redirect_uri", GoogleAuthConfig.IOS_REDIRECT_URI)
                        }
                    )
                )
            }

            val responseBody = response.bodyAsText()
            if (response.status.isSuccess()) {
                val json = Json.parseToJsonElement(responseBody).jsonObject
                val idToken = json["id_token"]?.jsonPrimitive?.content
                if (idToken != null) {
                    GoogleAuthResult.Success(idToken)
                } else {
                    GoogleAuthResult.Failure("응답에 id_token이 존재하지 않습니다.")
                }
            } else {
                GoogleAuthResult.Failure("토큰 교환 실패: $responseBody")
            }
        } finally {
            client.close()
        }
    }

    @OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
    private fun generatePkcePair(): Pair<String, String> {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
        val verifier = (1..64).map { allowedChars.random() }.joinToString("")
        val verifierBytes = verifier.encodeToByteArray()
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)

        verifierBytes.usePinned { pinnedData ->
            hash.usePinned { pinnedHash ->
                CC_SHA256(
                    pinnedData.addressOf(0),
                    verifierBytes.size.toUInt(),
                    pinnedHash.addressOf(0).reinterpret()
                )
            }
        }

        val challenge = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(hash)
        return Pair(verifier, challenge)
    }

    private fun extractQueryParam(url: String, key: String): String? {
        val query = url.substringAfter("?", "").substringBefore("#")
        if (query.isEmpty()) return null
        val params = query.split("&")
        for (param in params) {
            val parts = param.split("=")
            if (parts.size >= 2 && parts[0] == key) {
                return parts.subList(1, parts.size).joinToString("=")
            }
        }
        return null
    }
}

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    return remember { IosGoogleAuthProvider() }
}
