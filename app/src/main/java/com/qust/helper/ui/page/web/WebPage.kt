package com.qust.helper.ui.page.web

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.Log
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
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.model.account.Account
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.model.account.NeedLoginException
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.eas.BaseEasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WebPage {

	val EasWebPage = Page(Keys.Page.EasWebPage, "教务系统", iconRes = R.drawable.ic_school, enableDrawer = false) { activity, padding, navController ->
		val viewModel by activity.viewModels<EasWebViewModel>()
		AppWidgets.CheckEasLogin(viewModel = viewModel, navController = navController)

		Box(modifier = Modifier.padding(padding)){
			WebScreen(viewModel = viewModel, mainController = navController, toast = activity.toast)
		}
	}

	val IpassWebPage = Page(Keys.Page.IpassWebPage, "智慧青科大", iconRes = R.drawable.ic_school, enableDrawer = false) { activity, padding, navController ->
		val viewModel by activity.viewModels<IpassWebViewModel>()
		LaunchedEffect(viewModel.needLogin){
			if(viewModel.needLogin) {
				navController.navigate(Keys.Page.VpnLoginPage)
				viewModel.needLogin = false
			}
		}
		Box(modifier = Modifier.padding(padding)){
			WebScreen(viewModel = viewModel, mainController = navController, toast = activity.toast)
		}
	}

	const val WEB_VIEW_ROUTE = "web_view_route"

	@Composable
	fun WebScreen(viewModel: WebPageViewModel, mainController: NavController, toast: Toast, onBack: () -> Unit = { mainController.popBackStack() }) {
		val navController = rememberNavController()

		LaunchedEffect(viewModel.hasCheck){
			if(!viewModel.hasCheck) viewModel.checkAccountLogin()
		}

		var isNewPage by remember { mutableStateOf(true) }
		val webViews = remember { ArrayDeque<WebView>() }

		BottomSheet(
			back = {
				if(webViews.size > 0){
					val webView = webViews.removeLast()
					if(webView.parent != null) (webView.parent as ViewGroup).removeView(webView)
					webView.removeAllViews()
					webView.destroy()

					if(navController.previousBackStackEntry != null){
						isNewPage = false
						navController.navigateUp()
					}else{
						onBack()
					}
				}else{
					onBack()
				}
			}, forward = { }, reload = {
				if(!viewModel.isLogin){
					viewModel.checkAccountLogin()
				}else{
					if(webViews.size > 0) webViews.last().reload()
				}
			}
		) { padding ->

			LinearProgressIndicator(
				progress = { viewModel.progress },
				modifier = Modifier.fillMaxWidth().height(if (viewModel.progress == 100F) 0.dp else 5.dp),
				color = MaterialTheme.colorScheme.primary,
			)

			if(viewModel.isLogin){
				AppWidgets.NavigationHost(navController = navController, modifier = Modifier.padding(padding), startDestination = "${WEB_VIEW_ROUTE}/${Uri.encode(viewModel.startUrl)}") {
					composable("${WEB_VIEW_ROUTE}/{url}") { backStackEntry ->
						val url by remember { mutableStateOf(backStackEntry.arguments?.getString("url") ?: viewModel.startUrl) }
						val myWebViewClient by remember { mutableStateOf(MyWebViewClient(url){
							isNewPage = true
							navController.navigate("$WEB_VIEW_ROUTE/${it}", navOptions { launchSingleTop = false })
						}) }

						AndroidView(
							factory = { context ->
								val webView: WebView
								if(isNewPage || webViews.size == 0){
									webView = viewModel.createWebView(context, myWebViewClient)
									if(URLUtil.isValidUrl(url) && !URLUtil.isValidUrl(webView.url)) {
										webView.loadUrl(url)
									}
									webViews.addLast(webView)
								}else{
									webView = webViews.last()
								}
								webView
							}
						)
					}
				}
			}

			AppWidgets.DialogBar(dialogText = viewModel.dialogText)
			toast.ToastContent(toastContent = viewModel.toastContent)
		}
	}

	class MyWebViewClient(val url: String, val onNewPage: (String) -> Unit): WebViewClient() {
		override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
			if(view == null || request == null) return false

			val requestUrl = request.url.toString()

			if(!URLUtil.isValidUrl(requestUrl)) {
				try {
					view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
				} catch(e: Exception) {
					Log.e(this.javaClass.name, e.message.toString())
				}
				return true
			}

			return if(
				(Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !request.isRedirect) &&
				URLUtil.isNetworkUrl(requestUrl) &&
				requestUrl != url
			) {
				onNewPage(Uri.encode(requestUrl))
				true
			}else{
				false
			}
		}

		override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) { handler?.proceed() }
	}

	class EasWebViewModel : WebPageViewModel{
		constructor(application: Application) : this(application, EASAccount.getInstance())
		private constructor(application: Application, account: Account) : super(application, account, "${account.scheme}://${account.host}/jwglxt/xtgl/index_initMenu.html")
	}

	class IpassWebViewModel : WebPageViewModel{
		constructor(application: Application) : this(application, IPassAccount.getInstance())
		private constructor(application: Application, account: Account) : super(application, account, "${account.scheme}://${account.host}/https/77726476706e69737468656265737421f9b95089342426557a1dc7af96/tp_up/view?m=up#act=portal/viewhome")
	}

	open class WebPageViewModel(application: Application, val account: Account, var startUrl: String): BaseEasViewModel(application){

		var isLogin by mutableStateOf(false)
		var hasCheck by mutableStateOf(false)
		var progress: Float by mutableFloatStateOf(0f)

		val webChromeClient = object : WebChromeClient() {
			override fun onProgressChanged(view: WebView, newProgress: Int) {
				super.onProgressChanged(view, newProgress)
				progress = (newProgress / 100f).coerceIn(0f, 1f)
			}
		}

		fun checkAccountLogin(){
			hasCheck = true
			viewModelScope.launch {
				showDialog("正在登录")
				withContext(Dispatchers.IO){
					try {
						account.checkLogin()
						val cookieManager = CookieManager.getInstance()
						cookieManager.removeAllCookies(null)
						cookieManager.setAcceptCookie(true)
						for(cookieString in account.getCookie()) {
							cookieManager.setCookie("${account.scheme}://${account.host}", cookieString.toString())
						}
						cookieManager.flush()
						isLogin = true
					}catch(e: NeedLoginException){
						toastWarning("请先登录")
						needLogin = true
					}catch(_: Exception){
						toastError("网络错误")
					}finally {
						clearDialog()
					}
				}
			}
		}

		fun createWebView(context: Context, webViewClient: WebViewClient): WebView{
			val webView = WebView(context)
			webView.setBackgroundColor(Color.TRANSPARENT)
			webView.isVerticalScrollBarEnabled = true
			webView.settings.also { webSettings ->
				webSettings.allowFileAccess = true
				webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
				webSettings.domStorageEnabled = true
				webSettings.javaScriptEnabled = true
				webSettings.loadWithOverviewMode = true
				webSettings.setSupportZoom(true)
				webSettings.displayZoomControls = true
				webSettings.useWideViewPort = true
				webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
			}
			CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

			webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			webView.webChromeClient = webChromeClient
			webView.webViewClient = webViewClient
			webView.setDownloadListener { downloadUrl, _, _, _, _ ->
				try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).addCategory(Intent.CATEGORY_BROWSABLE)) } catch(_: Exception) { }
			}

			return webView
		}
	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	private fun BottomSheet(back: () -> Unit = { }, forward: () -> Unit = { }, reload: () -> Unit = { }, content: @Composable (PaddingValues) -> Unit) {
		var sheetValue by rememberSaveable { mutableStateOf(SheetValue.PartiallyExpanded) }

		val bottomSheetState = rememberStandardBottomSheetState(
			initialValue = sheetValue,
			confirmValueChange = { sheetValue = it; true },
			skipHiddenState = false
		)

		val scope = rememberCoroutineScope()

		BackHandler(true) { back() }

		BottomSheetScaffold(
			sheetContent = {
				Column {
					Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface).height(50.dp)) {
						IconButton(onClick = { back() }, modifier = Modifier.weight(1f).fillMaxHeight()) {
							Icon(imageVector = Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = null)
						}
						IconButton(onClick = { forward() }, modifier = Modifier.weight(1f).fillMaxHeight(), enabled = false) {
							Icon(imageVector = Icons.AutoMirrored.Sharp.ArrowForward, contentDescription = null)
						}
						IconButton(onClick = { reload(); scope.launch { bottomSheetState.partialExpand() } }, modifier = Modifier.weight(1f).fillMaxHeight()) {
							Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
						}
						IconButton(onClick = { scope.launch { if (sheetValue == SheetValue.PartiallyExpanded) { bottomSheetState.expand() } else { bottomSheetState.partialExpand() } } }, modifier = Modifier.weight(1f).fillMaxHeight(), enabled = false) {
							Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
						}
					}
					Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface).height(50.dp)) {
						IconButton(onClick = { }, modifier = Modifier.weight(1f).fillMaxHeight()) {
							Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
						}
					}
				}
			},
			scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState),
			sheetPeekHeight = 50.dp,
			sheetDragHandle = null,
			sheetSwipeEnabled = false
		) { padding ->
			content(padding)
		}
	}
}