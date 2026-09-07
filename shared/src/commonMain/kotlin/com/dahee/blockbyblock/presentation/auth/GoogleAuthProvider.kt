package com.dahee.blockbyblock.presentation.auth

import androidx.compose.runtime.Composable

object GoogleAuthConfig {
    const val WEB_CLIENT_ID = "232594695076-po18jgun7bqc8rdqqajoqgpf17lhbqrt.apps.googleusercontent.com"
}

sealed interface GoogleAuthResult {
    data class Success(val idToken: String) : GoogleAuthResult
    data object Cancelled : GoogleAuthResult
    data class Failure(val message: String, val cause: Throwable? = null) : GoogleAuthResult
}

interface GoogleAuthProvider {
    suspend fun signIn(): GoogleAuthResult
}

@Composable
expect fun rememberGoogleAuthProvider(): GoogleAuthProvider
