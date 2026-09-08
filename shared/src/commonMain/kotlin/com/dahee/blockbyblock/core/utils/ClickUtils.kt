package com.dahee.blockbyblock.core.utils

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

/**
 * Click throttling helper to prevent accidental double-taps and burst request storms in Compose.
 */
@Composable
fun rememberSingleClick(
    debounceIntervalMs: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableStateOf(0L) }
    return {
        val currentTime = getCurrentEpochMillis()
        if (currentTime - lastClickTime >= debounceIntervalMs) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * Modifier extension ensuring a clickable element ignores rapid repeated taps within [debounceIntervalMs].
 */
fun Modifier.singleClick(
    enabled: Boolean = true,
    debounceIntervalMs: Long = 500L,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val singleClickAction = rememberSingleClick(debounceIntervalMs = debounceIntervalMs, onClick = onClick)
    this.clickable(
        enabled = enabled,
        role = role,
        onClick = singleClickAction
    )
}

/**
 * Coroutine-safe guard to ensure an asynchronous operation is only executed once until completion.
 */
class SubmitGuard {
    private val _isSubmitting = mutableStateOf(false)
    val isSubmitting: Boolean get() = _isSubmitting.value

    fun <T> runGuarded(action: () -> T): T? {
        if (_isSubmitting.value) return null
        _isSubmitting.value = true
        return try {
            action()
        } finally {
            _isSubmitting.value = false
        }
    }

    suspend fun <T> runAsyncGuarded(action: suspend () -> T): T? {
        if (_isSubmitting.value) return null
        _isSubmitting.value = true
        return try {
            action()
        } finally {
            _isSubmitting.value = false
        }
    }
}
