package com.dahee.blockbyblock.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosGoogleAuthProvider : GoogleAuthProvider {

    private class PresentationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
        override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): UIWindow {
            return UIApplication.sharedApplication.keyWindow
                ?: (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)
                ?: UIWindow()
        }
    }

    private val presentationContextProvider = PresentationContextProvider()

    override suspend fun signIn(): GoogleAuthResult = suspendCancellableCoroutine { continuation ->
        val nonce = NSUUID.UUID().UUIDString
        val redirectUri = "com.dahee.blockbyblock:/oauth2redirect"
        val authUrlString = "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id=${GoogleAuthConfig.WEB_CLIENT_ID}" +
            "&response_type=id_token" +
            "&scope=openid%20email%20profile" +
            "&redirect_uri=${redirectUri}" +
            "&nonce=$nonce"

        val authUrl = NSURL.URLWithString(authUrlString)
        if (authUrl == null) {
            continuation.resume(GoogleAuthResult.Failure("Invalid Google Auth URL"))
            return@suspendCancellableCoroutine
        }

        val session = ASWebAuthenticationSession(
            uRL = authUrl,
            callbackURLScheme = "com.dahee.blockbyblock",
            completionHandler = { callbackUrl: NSURL?, error: NSError? ->
                if (error != null) {
                    if (error.domain == ASWebAuthenticationSessionErrorDomain && error.code == 1L) {
                        // 1L = ASWebAuthenticationSessionErrorCodeCanceledLogin
                        continuation.resume(GoogleAuthResult.Cancelled)
                    } else {
                        continuation.resume(GoogleAuthResult.Failure(error.localizedDescription))
                    }
                    return@ASWebAuthenticationSession
                }

                if (callbackUrl != null) {
                    val urlStr = callbackUrl.absoluteString ?: ""
                    val idToken = extractIdToken(urlStr)
                    if (idToken != null) {
                        continuation.resume(GoogleAuthResult.Success(idToken))
                    } else {
                        continuation.resume(GoogleAuthResult.Failure("ID Token이 응답 URL에 포함되어 있지 않습니다."))
                    }
                } else {
                    continuation.resume(GoogleAuthResult.Failure("로그인 응답 URL을 수신하지 못했습니다."))
                }
            }
        )

        session.presentationContextProvider = presentationContextProvider
        val started = session.start()
        if (!started) {
            continuation.resume(GoogleAuthResult.Failure("인증 세션을 시작할 수 없습니다."))
        }
    }

    private fun extractIdToken(url: String): String? {
        val fragment = url.substringAfter("#", "").ifEmpty { url.substringAfter("?", "") }
        val params = fragment.split("&")
        for (param in params) {
            val parts = param.split("=")
            if (parts.size == 2 && parts[0] == "id_token") {
                return parts[1]
            }
        }
        return null
    }
}

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    return remember { IosGoogleAuthProvider() }
}
