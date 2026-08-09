package com.qust.helper.next.common

actual fun getBasePlatform(): BasePlatform = JVMPlatform()

class JVMPlatform: BasePlatform {
    override fun currentPlatform(): Platform.PlatformType = Platform.PlatformType.JVM

    override fun getMac(): String? = null

    override fun getDeviceID(): String = ""
}