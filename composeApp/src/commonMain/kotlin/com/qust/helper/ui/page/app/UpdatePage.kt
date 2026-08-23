package com.qust.helper.ui.page.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.widget.ProgressDialog
import com.qust.helper.ui.widget.components.ListItemPicker
import com.qust.helper.viewmodel.app.UpdateViewModel
import kotlin.math.roundToInt

object UpdatePage: BasePage<UpdateViewModel>("检查更新", Icons.Default.Info) {

	val updateChannel = listOf("Gitee", "GitHub")

	@Composable
	override fun getViewModel() = viewModel<UpdateViewModel>()

	@Composable
	override fun Content(viewModel: UpdateViewModel) {
		var channel by remember { mutableIntStateOf(0) }

		Column(
			modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(text = viewModel.version, Modifier.padding(8.dp))

			Row(
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(text = "更新渠道")
				ListItemPicker(
					value = updateChannel[channel],
					list = updateChannel,
					onValueChange = { index, _ -> channel = index }
				)
				Button(onClick = { viewModel.checkUpdate(channel) }) {
					Text(text = "检查更新")
				}
			}

			if(viewModel.apkUrl.isNotEmpty()){
				Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
					val uriHandler = LocalUriHandler.current
					val annotatedString = buildAnnotatedString {
						append("下载地址: ")
						pushStringAnnotation(tag = "url", annotation = viewModel.apkUrl)
						withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(viewModel.apkUrl) }
						pop()
					}
					ClickableText(text = annotatedString, onClick = { offset ->
						annotatedString.getStringAnnotations(tag = "url", start = offset, end = offset).firstOrNull()?.let {
							uriHandler.openUri(it.item)
						}
					}, modifier = Modifier.weight(1F))

					Button(onClick = { viewModel.downloadApk() }) {
						Text(text = "更新")
					}
				}
			}

			Text(text = "更新日志: \n${viewModel.versionCode}\n${viewModel.updateInfo}", modifier = Modifier.fillMaxWidth().padding(16.dp))
		}

		if(viewModel.progress >= 0F){
			ProgressDialog(title = "正在下载", progress = viewModel.progress, progressText = "${(viewModel.progress * 100).roundToInt()}/100")
		}
	}
}
