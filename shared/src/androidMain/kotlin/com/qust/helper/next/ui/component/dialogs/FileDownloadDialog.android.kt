package com.qust.helper.next.ui.component.dialogs

import android.content.Intent
import android.os.Environment
import com.qust.helper.next.common.utils.AndroidUtils
import com.qust.helper.next.BaseApplication
import java.io.File

internal actual fun getDownloadPath() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

internal actual fun openFile(file: File) {
    BaseApplication.INSTANCE.startActivity(Intent(Intent.ACTION_VIEW).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setDataAndType(AndroidUtils.createUri(BaseApplication.INSTANCE, file), "application/*")
    })
}