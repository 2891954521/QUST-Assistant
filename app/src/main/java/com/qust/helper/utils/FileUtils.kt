package com.qust.helper.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.Locale

object FileUtils {

	private val HEX_CODE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')


	fun writeFile(f: String, string: String) {
		writeFile(File(f), string)
	}

	fun writeFile(f: File, string: String?) {
		try {
			val w = FileWriter(f)
			w.write(string)
			w.flush()
			w.close()
		} catch(ignored: IOException) { }
	}

	fun readFile(str: String): String {
		return readFile(File(str))
	}

	fun readFile(f: File): String {
		if(!f.exists()) return ""
		val sb = StringBuilder()
		try {
			val b = FileReader(f)
			val c = CharArray(1024)
			var len = 0
			while(b.read(c).also { len = it } != -1) sb.appendRange(c, 0, len)
		} catch(ignored: IOException) { }
		return sb.toString()
	}

	fun appendFile(file: File, s: String) {
		var raf: RandomAccessFile? = null
		try {
			if(!file.exists()) file.createNewFile()
			raf = RandomAccessFile(file, "rw")
			raf.seek(raf.length())
			raf.write(s.toByteArray())
		} catch(ignored: IOException) {
		} finally {
			try { raf?.close() } catch(ignored: IOException) { }
		}
	}

	fun appendFileNewLine(file: File, s: String) {
		var raf: RandomAccessFile? = null
		try {
			if(!file.exists()) file.createNewFile()
			raf = RandomAccessFile(file, "rw")
			raf.seek(raf.length())
			raf.write(s.toByteArray())
			raf.writeChar('\n'.code)
		} catch(ignored: IOException) {
		} finally {
			try { raf?.close() } catch(ignored: IOException) { }
		}
	}

	fun copyFile(input: InputStream, targetPath: String) {
		copyFile(input, File(targetPath))
	}

	fun copyFile(input: InputStream, targetPath: File) {
		try {
			FileOutputStream(targetPath).use { fos ->
				val buffer = ByteArray(1024)
				var byteCount: Int
				while(input.read(buffer).also { byteCount = it } != -1) {
					fos.write(buffer, 0, byteCount)
				}
				fos.flush()
				input.close()
			}
		} catch(ignore: Exception) { }
	}

	/**
	 * 从assets目录下拷贝文件
	 */
	fun copyFileFromAssets(context: Context, assetsFilePath: String, targetFileFullPath: String) {
		val assestsFileInputStream: InputStream
		try {
			assestsFileInputStream = context.assets.open(assetsFilePath)
			copyFile(assestsFileInputStream, targetFileFullPath)
		} catch(_: IOException) { }
	}

	/**
	 * 从文件读取序列化后的数据
	 * @param file  文件
	 * @return      反序列化后的数据
	 */
	@Throws(Exception::class)
	fun loadData(file: File): Any {
		return if(file.exists()) {
			ObjectInputStream(FileInputStream(file)).use {
				it.readObject()
			}
		} else throw FileNotFoundException()
	}

	/**
	 * 将获取到的数据序列化存储
	 * @param file  文件
	 * @param o     数据对象
	 */
	@Throws(IOException::class)
	fun saveData(file: File?, o: Any?) {
		ObjectOutputStream(FileOutputStream(file)).use {
			it.writeObject(o)
			it.flush()
		}
	}

	// 获取不重命文件名
	fun getOnlyFileName(file: String, suffix: String): File {
		var f = File(file + suffix)
		if(!f.exists()) return f
		var count = 1
		while(true) {
			f = File("$file($count)$suffix")
			if(!f.exists()) return f else count++
		}
	}

	// 获取后缀名
	fun getFileExt(file: File): String {
		return getFileExt(file.name)
	}

	//
	fun getFileExt(fileName: String): String {
		val pos = fileName.lastIndexOf(".")
		return if(pos == -1) "" else fileName.substring(pos + 1).lowercase(Locale.getDefault())
	}

	// 移除后缀名
	fun removeExt(s: String): String {
		val index = s.lastIndexOf(".")
		return if(index == -1) s else s.substring(0, index)
	}

	// 计算文件的大小
	fun getFileSize(fileS: Long): String {
		val df = DecimalFormat("#.00")
		return if(fileS < 1024) {
			df.format(fileS.toDouble()) + "B"
		} else if(fileS < 1048576) {
			df.format(fileS.toDouble() / 1024) + "K"
		} else if(fileS < 1073741824) {
			df.format(fileS.toDouble() / 1048576) + "M"
		} else {
			df.format(fileS.toDouble() / 1073741824) + "G"
		}
	}

	/**
	 * 获取一个文件的md5值(可处理大文件)
	 */
	fun getMD5(file: File?): String {
		val md5 = MessageDigest.getInstance("MD5")
		FileInputStream(file).use { stream ->
			val buffer = ByteArray(8192)
			var length: Int
			while(stream.read(buffer).also { length = it } != -1) {
				md5.update(buffer, 0, length)
			}
			val data = md5.digest()
			val r = StringBuilder(data.size * 2)
			for(b in data) {
				r.append(HEX_CODE[b.toInt() shr 4 and 0xF])
				r.append(HEX_CODE[b.toInt() and 0xF])
			}
			return r.toString()
		}
	}
}
