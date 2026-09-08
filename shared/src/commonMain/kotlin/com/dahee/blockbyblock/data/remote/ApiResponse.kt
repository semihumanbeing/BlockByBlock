package com.dahee.blockbyblock.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T
)

@Serializable
data class FieldErrorDetail(
    val field: String,
    val value: String? = null,
    val reason: String
)

@Serializable
data class ApiErrorResponse(
    val code: String? = null,
    val message: String? = null,
    val errors: List<FieldErrorDetail>? = null,
    val status: Int? = null,
    val error: String? = null
)
