package com.dahee.blockbyblock.data.remote.error

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.FieldErrorDetail

/**
 * Custom Exception encapsulating backend API error response.
 */
class ApiError(
    override val message: String,
    val code: String = ErrorCode.UNKNOWN_ERROR,
    val status: Int? = null,
    val errors: List<FieldErrorDetail>? = null,
    val originalResponse: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    fun hasFieldErrors(): Boolean = !errors.isNullOrEmpty()

    fun getFieldError(fieldName: String): FieldErrorDetail? {
        if (errors.isNullOrEmpty()) return null
        val normalized = fieldName.trim().lowercase()
        return errors.find { it.field.trim().lowercase() == normalized }
    }

    fun getFieldErrorMessage(fieldName: String): String? = getFieldError(fieldName)?.reason

    fun isErrorCode(expectedCode: String): Boolean = code == expectedCode

    fun isAuthError(): Boolean =
        status == 401 ||
        code == ErrorCode.AUTHENTICATION_FAILED ||
        code == ErrorCode.EXPIRED_TOKEN ||
        code == ErrorCode.INVALID_TOKEN ||
        code == ErrorCode.TOKEN_REUSE_DETECTED

    fun isTokenExpired(): Boolean = code == ErrorCode.EXPIRED_TOKEN

    fun isTokenReuseDetected(): Boolean = code == ErrorCode.TOKEN_REUSE_DETECTED

    fun isAccessDenied(): Boolean = status == 403 || code == ErrorCode.ACCESS_DENIED

    fun isValidationError(): Boolean =
        status == 400 || code == ErrorCode.INVALID_INPUT_VALUE || hasFieldErrors()

    fun isServerError(): Boolean =
        (status != null && status >= 500) || code == ErrorCode.INTERNAL_SERVER_ERROR

    fun isTooManyRequests(): Boolean =
        status == 429 || code == ErrorCode.TOO_MANY_REQUESTS

    fun extractFieldErrorMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        errors?.forEach { detail ->
            if (detail.field.isNotBlank() && detail.reason.isNotBlank()) {
                map[detail.field] = detail.reason
            }
        }
        return map
    }

    companion object {
        fun fromHttpResponse(status: Int, responseBodyText: String): ApiError {
            return try {
                val parsed = ApiClient.jsonConfig.decodeFromString<ApiErrorResponse>(responseBodyText)
                val code = parsed.code ?: if (status > 0) "HTTP_$status" else ErrorCode.UNKNOWN_ERROR
                val msg = parsed.message ?: parsed.error ?: "Request failed ($status)"
                ApiError(
                    message = msg,
                    code = code,
                    status = status,
                    errors = parsed.errors,
                    originalResponse = responseBodyText
                )
            } catch (_: Exception) {
                val fallbackMsg = if (responseBodyText.isNotBlank() && responseBodyText.length < 200) {
                    responseBodyText
                } else {
                    "서버 통신 오류가 발생했습니다. (상태 코드: $status)"
                }
                ApiError(
                    message = fallbackMsg,
                    code = if (status > 0) "HTTP_$status" else ErrorCode.UNKNOWN_ERROR,
                    status = status,
                    originalResponse = responseBodyText
                )
            }
        }

        fun fromThrowable(throwable: Throwable): ApiError {
            if (throwable is ApiError) return throwable
            val msg = throwable.message ?: "Unknown error"
            return if (msg.contains("timeout", ignoreCase = true)) {
                ApiError(
                    message = "서버 응답 시간이 초과되었습니다.",
                    code = ErrorCode.TIMEOUT_ERROR,
                    cause = throwable
                )
            } else if (msg.contains("connect", ignoreCase = true) || msg.contains("network", ignoreCase = true)) {
                ApiError(
                    message = "네트워크 연결 상태를 확인해주세요.",
                    code = ErrorCode.NETWORK_ERROR,
                    cause = throwable
                )
            } else {
                ApiError(
                    message = msg,
                    code = ErrorCode.UNKNOWN_ERROR,
                    cause = throwable
                )
            }
        }
    }
}

/**
 * Utility function to apply backend field validation errors to UI error state handlers.
 */
fun applyFormApiErrors(
    error: Throwable,
    onFieldError: (field: String, reason: String) -> Unit,
    onGeneralError: ((String) -> Unit)? = null
): Boolean {
    if (error is ApiError && error.hasFieldErrors()) {
        error.errors?.forEach { detail ->
            if (detail.field.isNotBlank() && detail.reason.isNotBlank()) {
                onFieldError(detail.field, detail.reason)
            }
        }
        return true
    }
    if (onGeneralError != null) {
        onGeneralError(error.message ?: "요청 처리 중 오류가 발생했습니다.")
        return true
    }
    return false
}
