package com.dahee.blockbyblock.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T
)

@Serializable
data class ApiErrorResponse(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
)
