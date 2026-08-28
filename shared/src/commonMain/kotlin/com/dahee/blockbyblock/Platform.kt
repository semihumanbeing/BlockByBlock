package com.dahee.blockbyblock

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform