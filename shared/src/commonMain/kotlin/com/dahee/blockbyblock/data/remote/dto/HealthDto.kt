package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthStatusData(
    val status: String,
    val database: String? = null,
    val timestamp: String? = null
)

@Serializable
data class HealthCheckResponse(
    val code: String? = null,
    val data: HealthStatusData
)
