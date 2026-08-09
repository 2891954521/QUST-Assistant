package com.qust.helper.next.common

import android.os.Build
import android.provider.Settings
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.utils.CodeUtils
import com.qust.helper.next.BaseApplication
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

actual fun getBasePlatform(): BasePlatform = AndroidPlatform()

class AndroidPlatform: BasePlatform {

    override fun currentPlatform(): Platform.PlatformType = Platform.PlatformType.ANDROID

    override fun getMac(): String {
        return execCmd("cat /sys/class/net/eth0/address")
    }

    override fun getDeviceID(): String {
        return CodeUtils.md5(buildString {
            append(Settings.System.getString(BaseApplication.INSTANCE.contentResolver, Settings.Secure.ANDROID_ID))
            append(Build.BRAND)
            append(Build.DEVICE)
            append(Build.MODEL)
            append(Build.PRODUCT)
            append(Build.ID)
            append(Build.HARDWARE)
            append(Build.MANUFACTURER)
            append(Build.FINGERPRINT)
            append(Build.VERSION.SDK_INT)
        })
    }

    fun execCmd(command: String): String {
        var result = ""
        try {
            val mProcess = Runtime.getRuntime().exec(command)
            val mReader = BufferedReader(InputStreamReader(mProcess.inputStream))
            val mRespBuff = StringBuffer()

            val buff = CharArray(1024)
            var ch: Int
            while((mReader.read(buff).also { ch = it }) != -1) {
                mRespBuff.append(buff, 0, ch)
            }
            mReader.close()
            result = mRespBuff.toString()
        } catch(e: IOException) {
            Logger.e(e)
        }
        return result
    }
}