package com.qust.helper.next.ui.component.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qust.helper.next.utils.FileUtils
import com.qust.helper.next.network.client.CommonHttpClient
import com.qust.helper.next.ui.component.BaseDialog
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


@Composable
fun FileDownloadDialog(state: FileDownloadUiState) {
    if (!state.visible) return

    BaseDialog {

        val animatedProgress by animateFloatAsState(
            label = "",
            targetValue = state.progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(text = state.title)

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.weight(1F),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Text(
                    text = state.progressText,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}


class FileDownloadUiState {

    var visible by mutableStateOf(false)

    var title by mutableStateOf("")

    var progress by mutableFloatStateOf(0F)

    var progressText by mutableStateOf("")

    @Suppress("DefaultLocale")
    suspend fun download(
        url: String,
        savePath: String? = null,
        fileName: String? = null,
        autoOpen: Boolean = true
    ): String? {
        var result: String? = null

        withContext(Dispatchers.IO) {
            try {
                visible = true

                val outputPath = if(savePath != null) File(savePath) else File(getDownloadPath())
                require(outputPath.exists() || outputPath.mkdirs()) {
                    "无法创建下载目录：${outputPath.absolutePath}"
                }
                require(outputPath.isDirectory) {
                    "下载路径不是目录：${outputPath.absolutePath}"
                }

                var outputFile = File(outputPath, fileName ?: url.substringAfterLast("/"))

                val suffix = FileUtils.getFileExt(outputFile)

                outputFile = FileUtils.getOnlyFileName(
                    file = FileUtils.removeExt(outputFile.absolutePath),
                    suffix = if(suffix.isNotEmpty()) ".$suffix" else ""
                )

                result = outputFile.absolutePath

                val response = CommonHttpClient.client.request(url)

                val length = response.contentLength()?.toFloat()

                response.bodyAsChannel().toInputStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        var pass = 0L
                        val buffer = ByteArray(4096)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            pass += bytes
                            bytes = input.read(buffer)

                            if (length != null && length > 0F) {
                                progress = (pass / length).coerceIn(0F, 1F)
                                progressText = String.format("%.2f %%", progress * 100)
                            } else {
                                progressText = "${pass / 1024 / 1024} MB"
                            }
                        }
                    }
                }

                if (autoOpen) openFile(outputFile)
            }finally {
                visible = false
            }
        }

        return result
    }
}

internal expect fun getDownloadPath(): String

internal expect fun openFile(file: File)