package com.qust.helper.next.ui.component.dialogs

import java.awt.Desktop
import java.io.File

internal actual fun getDownloadPath(): String =
    File(System.getProperty("user.home"), "Downloads").absolutePath

internal actual fun openFile(file: File) {
    require(file.isFile) { "文件不存在：${file.absolutePath}" }
    check(Desktop.isDesktopSupported()) { "当前系统不支持打开文件" }

    Desktop.getDesktop().open(file)
}
