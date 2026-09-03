package com.dahee.blockbyblock.core.utils

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

@OptIn(ExperimentalWasmJsInterop::class)
private external class Date : JsAny {
    fun getFullYear(): Int
    fun getMonth(): Int
    fun getDate(): Int
    companion object {
        fun now(): Double
    }
}

actual fun getCurrentDateIso(): String {
    val d = Date()
    val y = d.getFullYear()
    val m = d.getMonth() + 1
    val day = d.getDate()
    val mm = if (m < 10) "0$m" else "$m"
    val dd = if (day < 10) "0$day" else "$day"
    return "$y-$mm-$dd"
}

actual fun getCurrentEpochMillis(): Long = Date.now().toLong()
