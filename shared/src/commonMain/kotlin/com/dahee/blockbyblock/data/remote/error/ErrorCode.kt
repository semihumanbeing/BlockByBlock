package com.dahee.blockbyblock.data.remote.error

/**
 * Standard backend and client-side error codes.
 */
object ErrorCode {
    // 401: Unauthorized
    const val AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED"
    const val EXPIRED_TOKEN = "EXPIRED_TOKEN"
    const val INVALID_TOKEN = "INVALID_TOKEN"
    const val TOKEN_REUSE_DETECTED = "TOKEN_REUSE_DETECTED"

    // 403: Forbidden
    const val ACCESS_DENIED = "ACCESS_DENIED"

    // 400: Bad Request
    const val INVALID_INPUT_VALUE = "INVALID_INPUT_VALUE"
    const val INVALID_VERIFICATION_CODE = "INVALID_VERIFICATION_CODE"

    // 409: Conflict
    const val EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS"

    // 404: Not Found
    const val RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND"
    const val USER_NOT_FOUND = "USER_NOT_FOUND"
    const val BLOCK_NOT_FOUND = "BLOCK_NOT_FOUND"
    const val MEAL_NOT_FOUND = "MEAL_NOT_FOUND"

    // 405: Method Not Allowed
    const val METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED"

    // 500: Internal Server Error
    const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"

    // 429: Rate Limit & Account Locked
    const val TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS"
    const val ACCOUNT_LOCKED = "ACCOUNT_LOCKED"

    // Client-side synthesized errors
    const val TIMEOUT_ERROR = "TIMEOUT_ERROR"
    const val NETWORK_ERROR = "NETWORK_ERROR"
    const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
}
