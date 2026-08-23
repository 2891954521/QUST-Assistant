package com.qust.helper.ui.page.web

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewContent(
	url: String,
	cookies: List<String>,
	onProgress: (Float) -> Unit,
) {
	AndroidView(
		modifier = Modifier.fillMaxSize(),
		factory = { context ->
			val webView = WebView(context)
			webView.setBackgroundColor(Color.TRANSPARENT)
			webView.isVerticalScrollBarEnabled = true
			webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			webView.settings.apply {
				allowFileAccess = true
				cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
				domStorageEnabled = true
				javaScriptEnabled = true
				loadWithOverviewMode = true
				setSupportZoom(true)
				displayZoomControls = true
				useWideViewPort = true
				mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
			}

			injectCookies(url, cookies)

			webView.webChromeClient = object : WebChromeClient() {
				override fun onProgressChanged(view: WebView, newProgress: Int) {
					super.onProgressChanged(view, newProgress)
					onProgress(newProgress / 100f)
				}
			}
			webView.webViewClient = MyWebViewClient(context)
			webView.loadUrl(url)
			webView
		}
	)
}

@Composable
actual fun AssetWebView(assetName: String) {
	AndroidView(
		modifier = Modifier.fillMaxSize(),
		factory = { context ->
			WebView(context).apply {
				setBackgroundColor(Color.TRANSPARENT)
				isVerticalScrollBarEnabled = true
				layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
				settings.apply {
					allowFileAccess = false
					loadWithOverviewMode = true
					setSupportZoom(true)
					displayZoomControls = true
				}
				loadUrl("file:///android_asset/$assetName")
			}
		}
	)
}

private fun injectCookies(url: String, cookies: List<String>) {
	val uri = Uri.parse(url)
	val scheme = uri.scheme ?: return
	val host = uri.host ?: return
	val domain = "$scheme://$host/"

	val cookieManager = CookieManager.getInstance()
	cookieManager.setAcceptCookie(true)
	cookieManager.removeAllCookies(null)
	for (cookie in cookies) {
		cookieManager.setCookie(domain, cookie)
	}
	cookieManager.flush()
}

private class MyWebViewClient(
	private val context: Context,
) : WebViewClient() {

	override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
		if (view == null || request == null) return false

		val requestUrl = request.url.toString()

		if (!URLUtil.isValidUrl(requestUrl)) {
			try {
				context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
			} catch (_: Exception) {
			}
			return true
		}

		return if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !request.isRedirect) && requestUrl != view.url) {
			view.loadUrl(requestUrl)
			true
		} else {
			false
		}
	}

	override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
		handler?.proceed()
	}
}
