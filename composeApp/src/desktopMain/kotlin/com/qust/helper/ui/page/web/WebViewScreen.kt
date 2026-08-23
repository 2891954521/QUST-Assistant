package com.qust.helper.ui.page.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun WebViewContent(
	url: String,
	cookies: List<String>,
	onProgress: (Float) -> Unit,
) {
	Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		Text("网页浏览功能请在 Android 设备上使用")
	}
}

@Composable
actual fun AssetWebView(assetName: String) {
	Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		Text("该内容请在 Android 设备上查看")
	}
}
