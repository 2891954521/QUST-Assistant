package com.qust.helper.next.common

expect fun getBasePlatform(): BasePlatform

interface BasePlatform {

    fun currentPlatform(): Platform.PlatformType

    fun getMac(): String?

    fun getDeviceID(): String
}

object Platform: BasePlatform by getBasePlatform() {

    enum class PlatformType {
        ANDROID,
        IOS,
        JVM,
        NATIVE,
        WEB
    }

}