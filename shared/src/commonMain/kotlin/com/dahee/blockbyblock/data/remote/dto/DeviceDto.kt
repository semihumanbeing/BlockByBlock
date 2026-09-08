package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val fcmToken: String,
    val deviceType: String,
    val timezone: String? = null
)

@Serializable
data class DeviceResponse(
    val id: Long,
    val deviceType: String,
    val isActive: Boolean,
    val updatedAt: String? = null
)
