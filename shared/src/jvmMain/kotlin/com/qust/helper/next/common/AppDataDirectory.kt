package com.qust.helper.next.common

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * JVM 端应用数据目录。
 *
 * 读取环境变量 `profile`：
 * - 未设置或空白：实际数据目录 `userDataRoot/com.qust.helper.next`
 * - 文件名：`userDataRoot` 下的同名目录
 * - 目录路径：直接使用该目录
 *
 * 目前只有数据库使用这个目录。
 */
fun appDataDirectory(): Path {
	val dir = resolveAppDataDirectory(userDataRoot(), System.getenv("profile"))
	Files.createDirectories(dir)
	return dir
}

fun appDatabasePath(): String = appDataDirectory().resolve("app_data").absolutePathString()

internal fun resolveAppDataDirectory(userDataRoot: Path, profile: String?): Path {
	val name = profile?.trim().orEmpty()
	if (name.isEmpty()) {
		return userDataRoot.resolve("com.qust.helper.next")
	}
	val path = try {
		Path.of(name)
	} catch (e: InvalidPathException) {
		throw IllegalArgumentException("环境变量 profile 不是合法路径: $profile", e)
	}
	if (isDirectoryProfile(name, path)) {
		return path.normalize()
	}
	val fileName = path.fileName?.toString().orEmpty()
	require(fileName.isNotEmpty() && fileName != "." && fileName != "..") {
		"环境变量 profile 不是合法文件名: $profile"
	}
	return userDataRoot.resolve(fileName)
}

private fun isDirectoryProfile(profile: String, path: Path): Boolean {
	return path.isAbsolute || profile.contains('/') || profile.contains('\\')
}

private fun userDataRoot(): Path {
	val os = System.getProperty("os.name").orEmpty().lowercase()
	val home = Path.of(System.getProperty("user.home"))
	return when {
		os.contains("win") -> {
			val appData = System.getenv("APPDATA")
			if (appData.isNullOrBlank()) home.resolve("AppData").resolve("Roaming") else Path.of(appData)
		}
		os.contains("mac") -> home.resolve("Library").resolve("Application Support")
		else -> {
			val xdg = System.getenv("XDG_DATA_HOME")
			if (xdg.isNullOrBlank()) home.resolve(".local").resolve("share") else Path.of(xdg)
		}
	}
}
