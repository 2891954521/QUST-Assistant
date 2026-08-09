package com.qust.helper.next.common.utils

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.view.WindowManager
import androidx.core.content.FileProvider
import com.qust.helper.next.common.log.Logger
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serializable

/**
 * 统一处理与 Android 版本关联的逻辑
 */
object AndroidUtils {

	inline fun <reified T : Serializable> getSerializableExtra(intent: Intent, key: String): T? {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getSerializableExtra(key, T::class.java)
		}else{
			intent.getSerializableExtra(key) as? T
		}
	}

	inline fun <reified T : Parcelable> getParcelableExtra(intent: Intent, key: String): T? {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableExtra(key, T::class.java)
		}else{
			intent.getParcelableExtra(key)
		}
	}

	inline fun <reified T> getParcelableArrayExtra(intent: Intent, key: String): Array<T> {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableArrayExtra(key, T::class.java) ?: emptyArray()
		}else{
			val tmp = intent.getParcelableArrayExtra(key) ?: emptyArray()
			Array(tmp.size){ tmp[it] as T }
		}
	}

	/**
	 * 获取设备的指令集
	 */
	fun getAbi(): String? {
		val supportAbi = arrayOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")

		try {
			val process = Runtime.getRuntime().exec("getprop")
			val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

			var line = bufferedReader.readLine()

			do {
				if(line.contains("ro.product.cpu.abi")) {
					// 按优先级顺序检查，确保 x86_64 在 x86 之前被检查
					for(i in supportAbi){
						if(line.contains(i)){
							bufferedReader.close()
							return i
						}
					}
					break
				}
				line = bufferedReader.readLine()
			} while(line != null)

			Logger.w("不支持的指令集: $line")

			bufferedReader.close()

		} catch(_: IOException) { }

		return null
	}

	/**
	 * 创建 Intent 唤起其他应用需要用的文件 Uri
	 */
	fun createUri(context: Context, file: File): Uri{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			return FileProvider.getUriForFile(context, context.packageName, file)
		}else{
			return Uri.fromFile(file)
		}
	}

	/**
	 * 获取屏幕大小
	 * @return (width, height)
	 */
	fun getScreenSize(context: Context): Pair<Int, Int> {
		val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val point = Point()
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			wm.defaultDisplay.getRealSize(point)
		} else {
			wm.defaultDisplay.getSize(point)
		}
		return Pair(point.x, point.y)
	}
}