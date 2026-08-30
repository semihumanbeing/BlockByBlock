package com.dahee.blockbyblock

interface Platform {
    val name: String
    val isWeb: Boolean
}

expect fun getPlatform(): Platform