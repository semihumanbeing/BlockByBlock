package com.dahee.blockbyblock.core.utils

/**
 * Client-side sanitization utility to prevent XSS payloads and normalize user input.
 */
object InputSanitizer {

    private val SCRIPT_REGEX = Regex("<script\\b[^<]*(?:(?!<\\/script>)<[^<]*)*<\\/script>", RegexOption.IGNORE_CASE)
    private val IFRAME_REGEX = Regex("<iframe\\b[^<]*(?:(?!<\\/iframe>)<[^<]*)*<\\/iframe>", RegexOption.IGNORE_CASE)
    private val JAVASCRIPT_PROTOCOL_REGEX = Regex("javascript:[^\"']*", RegexOption.IGNORE_CASE)
    private val INLINE_EVENT_REGEX = Regex("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE)

    /**
     * Sanitizes user input string:
     * - Strips malicious script/iframe tags and inline event handlers
     * - Trims leading and trailing whitespaces
     * - Enforces maximum length constraints
     */
    fun sanitize(
        input: String,
        maxLength: Int = 500,
        allowNewlines: Boolean = true
    ): String {
        if (input.isBlank()) return ""

        var sanitized = input
        sanitized = SCRIPT_REGEX.replace(sanitized, "")
        sanitized = IFRAME_REGEX.replace(sanitized, "")
        sanitized = JAVASCRIPT_PROTOCOL_REGEX.replace(sanitized, "")
        sanitized = INLINE_EVENT_REGEX.replace(sanitized, "")

        if (!allowNewlines) {
            sanitized = sanitized.replace("\r", " ").replace("\n", " ")
        }

        sanitized = sanitized.trim()

        return if (sanitized.length > maxLength) {
            sanitized.substring(0, maxLength)
        } else {
            sanitized
        }
    }
}
