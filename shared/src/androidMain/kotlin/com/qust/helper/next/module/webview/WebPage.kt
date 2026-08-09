package com.qust.helper.next.module.webview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.View
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.json.decodeToObject
import com.qust.helper.next.common.log.Logger


internal const val WEB_VIEW_ROUTE = "web_view_route"


@Composable
fun WebScreen(viewModel: WebPageViewModel, onBack: () -> Unit = { }) {
	val navController = rememberNavController()

	BackHandler(true) {
		viewModel.back()
	}

	LaunchedEffect(viewModel.onBack) {
		viewModel.handleBack(navController, onBack)
	}

	LaunchedEffect(viewModel.onHome) {
		if(viewModel.onHome == 0) return@LaunchedEffect
		navController.navigate("${WEB_VIEW_ROUTE}/${Uri.encode(viewModel.getHomeUrl())}", navOptions { launchSingleTop = false })
	}

	Box {
		LinearProgressIndicator(
			progress = { viewModel.progress },
			modifier = Modifier.fillMaxWidth().height(if(viewModel.progress == 100F) 0.dp else 5.dp),
			color = MaterialTheme.colorScheme.primary,
		)

		NavHost(
			navController = navController,
			startDestination = "${WEB_VIEW_ROUTE}/${Uri.encode(viewModel.startUrl)}",
			modifier = Modifier.fillMaxSize(),
			enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween()) },
			exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween()) },
			popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween()) },
			popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween()) },
		){
			composable("${WEB_VIEW_ROUTE}/{url}") { backStackEntry ->
				val url by remember { mutableStateOf(backStackEntry.arguments?.getString("url") ?: viewModel.startUrl) }
				val webViewClient by remember { mutableStateOf(MyWebViewClient(viewModel, navController, url)) }
				AndroidView(factory = { context -> viewModel.getWebView(context, url, webViewClient) })
			}
		}
	}
}

class MyWebViewClient(val viewModel: WebPageViewModel, val navController: NavController, val url: String): WebViewClient() {

	override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
		if(view == null || request == null) return false

		val requestUrl = request.url.toString()

		if(!URLUtil.isValidUrl(requestUrl)) {
			// 非 HTTP URL，唤起其他应用
			try { view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url)) } catch(_: Exception) { }
			return true
		}

		if(
			(Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !request.isRedirect) &&
			URLUtil.isNetworkUrl(requestUrl) && 
			requestUrl != url
		){
			viewModel.isNewPage = true
			viewModel.lastUrl = view.url ?: ""
			navController.navigate("$WEB_VIEW_ROUTE/${Uri.encode(requestUrl)}", navOptions { launchSingleTop = false })
			return true
		}else{
			// 重定向 / 非网络地址时，不进行处理
			return false
		}
	}

	override fun onPageFinished(view: WebView, url: String) {
		// 修改 viewport，允许缩放
		val js = """(function() {
			var vp = document.querySelector('meta[name=viewport]');
			if (vp) {
				vp.setAttribute('content', 'width=device-width, initial-scale=1.0, minimum-scale=0.2, maximum-scale=3.0, user-scalable=yes');
			} else {
				var m = document.createElement('meta');
				m.name = 'viewport';
				m.content = 'width=device-width, initial-scale=1.0, minimum-scale=0.2, maximum-scale=3.0, user-scalable=yes';
				document.head.appendChild(m);
			}
        })();""".trimIndent()
		view.evaluateJavascript(js, null)
		viewModel.onPageFinish(view, url)
	}

	override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) { handler?.proceed() }

}

abstract class WebPageViewModel {

	abstract val startUrl: String

	val webViews = ArrayDeque<WebView>()

	var onBack by mutableStateOf(false)
	var onHome by mutableIntStateOf(0)
	var progress by mutableFloatStateOf(0f)

	// 当前页是否是新页
	var isNewPage by mutableStateOf(true)
	// 打开新页时上一页的url
	var lastUrl by mutableStateOf("")

	val webViewJsRunner = WebViewJsRunner()

	private val webChromeClient = object : WebChromeClient() {
		override fun onProgressChanged(view: WebView, newProgress: Int) {
			super.onProgressChanged(view, newProgress)
			progress = (newProgress / 100f).coerceIn(0f, 1f)
			if(newProgress == 100) {
				// 移除打印时的默认边距
				view.loadUrl("javascript:window.print();")
			}
		}
	}


	/**
	 * 处理返回事件
	 */
	fun handleBack(navController: NavController, defaultCallback: () -> Unit = { }) {
		if(!onBack) return

		if(webViews.size <= 1) {
			defaultCallback()
			onBack = false
			return 
		}

		val webView = webViews.removeLast()
		if(webView.parent != null) (webView.parent as ViewGroup).removeView(webView)
		webView.removeAllViews()
		webView.destroy()
		if(navController.previousBackStackEntry != null){
			isNewPage = false
			navController.navigateUp()
		}else{
			defaultCallback()
		}

		onBack = false
	}

	fun back(){
		onBack = true
	}

	fun refresh(){
		if(webViews.isNotEmpty()) webViews.last().reload()
	}

	fun home(){
		onHome += 1
	}

	fun print(printManager: PrintManager){
		val printAdapter: PrintDocumentAdapter = webViews.last().createPrintDocumentAdapter("Print")
		val builder = PrintAttributes.Builder()
		builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
		printManager.print("网页打印", printAdapter, builder.build())
	}


	/**
	 * 获取或新建WebView
	 */
	fun getWebView(context: Context, url: String, webViewClient: WebViewClient): WebView {
		val webView: WebView
		if(isNewPage || webViews.isEmpty()) {
			webView = createWebView(context, webViewClient)
			if(URLUtil.isValidUrl(url) && !URLUtil.isValidUrl(webView.url)) {
				Logger.i("loadUrl: $url, referer: $lastUrl")

				webView.loadUrl(url, buildMap {
					if(lastUrl.isNotEmpty()){
						try {
							val uri = lastUrl.toUri()
							if(uri.port == -1 || uri.port == 80 || uri.port == 443) {
								put("Referer", "${uri.scheme}://${uri.host}")
							}else{
								put("Referer", "${uri.scheme}://${uri.host}:${uri.port}")
							}
						} catch (_: Exception) { }
					}
				})
			}
			webViews.addLast(webView)
		} else {
			webView = webViews.last()
		}
		return webView
	}

	/**
	 * 调用js方法
	 */
	@Throws(Exception::class)
	suspend inline fun <reified T> callJs(method: String, vararg params: Any?): T {
		val result = webViewJsRunner.execJs(webViews.last(), method, params) ?: throw UserDisplayException("返还值异常", NullPointerException("${method}(${params.joinToString(", ")}) result is null"))
		return result.decodeToObject()
	}

	/**
	 * 回首页的URL
	 */
	open fun getHomeUrl(): String {
		return startUrl
	}

	/**
	 * 页面加载完成的回调
	 */
	open fun onPageFinish(webView: WebView, url: String){

	}

	private fun createWebView(context: Context, webViewClient: WebViewClient): WebView {
		val webView = WebView(context)
		webView.setBackgroundColor(Color.TRANSPARENT)
		webView.isVerticalScrollBarEnabled = true
		webView.settings.also { webSettings ->
			webSettings.javaScriptEnabled = true

			// 设置视口以适配桌面网页
			webSettings.useWideViewPort = true
			webSettings.loadWithOverviewMode = true

			// 启用缩放
			webSettings.setSupportZoom(true)
			webSettings.builtInZoomControls = true
			webSettings.displayZoomControls = false
            webSettings.defaultZoom = WebSettings.ZoomDensity.FAR

			// 启用 DOM storage
			webSettings.domStorageEnabled = true

			// 强制加载桌面版UA
			webSettings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

			// 启用缓存
			webSettings.cacheMode = WebSettings.LOAD_DEFAULT

			// 支持通过JS打开新窗口
			webSettings.javaScriptCanOpenWindowsAutomatically = true

			webSettings.saveFormData = true
			webSettings.allowFileAccess = true
			webSettings.setGeolocationEnabled(true)
			webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
			webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
			webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
			webSettings.defaultTextEncodingName = "utf-8"
		}
		CookieManager.getInstance().setAcceptCookie(true)
		// CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

		webView.setInitialScale(25)
		webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) //禁用硬件加速
		webView.webChromeClient = webChromeClient
		webView.webViewClient = webViewClient
		webView.setDownloadListener { downloadUrl, _, _, _, _ ->
			try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).addCategory(Intent.CATEGORY_BROWSABLE)) } catch(_: Exception) { }
		}
		webViewJsRunner.injectJsRunner(webView)
		return webView
	}

}
