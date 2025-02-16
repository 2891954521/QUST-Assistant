package com.qust.helper

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform