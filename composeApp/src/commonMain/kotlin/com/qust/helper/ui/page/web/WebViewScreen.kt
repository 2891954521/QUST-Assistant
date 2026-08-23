package com.qust.helper.ui.page.web

import androidx.compose.runtime.Composable

/**
 * 网页浏览 WebView（Android 上渲染真实 WebView，Desktop 显示占位）
 *
 * @param url 起始 URL
 * @param cookies 需要注入的 Cookie，格式 "name=value"
 * @param onProgress 加载进度回调，0f ~ 1f
 */
@Composable
expect fun WebViewContent(
	url: String,
	cookies: List<String>,
	onProgress: (Float) -> Unit,
)

/**
 * 加载 android assets 目录中的 HTML（用户协议/隐私政策）
 */
@Composable
expect fun AssetWebView(assetName: String)
