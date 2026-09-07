package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarType: String,
    val onboardingCompleted: Boolean,
    val lang: String = "KO"
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val avatarType: String,
    val lang: String? = null
)

@Serializable
data class UpdateOnboardingRequest(
    val onboardingCompleted: Boolean
)

@Serializable
data class OnboardingResponse(
    val onboardingCompleted: Boolean
)
