package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String? = null
)

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val idToken: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class SignUpResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

@Serializable
data class SuccessResponse(
    val success: Boolean
)
