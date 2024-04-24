package com.qust.helper.ui.page.app

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.qust.helper.data.Keys
import com.qust.helper.data.Page

object AppPage {

	val UserAgreementPage = Page(Keys.Page.UserAgreementPage, "用户协议") { _, padding, _ ->
		Box(modifier = Modifier.padding(padding)){
			AndroidView(
				factory = { context ->
					val webView = WebView(context)
					webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
					webView.setBackgroundColor(Color.TRANSPARENT)
					webView.isVerticalScrollBarEnabled = true
					webView.settings.also { webSettings ->
						webSettings.allowFileAccess = false
						webSettings.loadWithOverviewMode = true
						webSettings.setSupportZoom(true)
						webSettings.displayZoomControls = true
					}
					webView.loadUrl("file:///android_asset/userAgreement.html")
					webView
				}
			)
		}
	}

	val PolicyPage = Page(Keys.Page.PolicyPage,"隐私政策") { _, padding, _ ->
		Box(modifier = Modifier.padding(padding)){
			AndroidView(
				factory = { context ->
					val webView = WebView(context)
					webView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
					webView.setBackgroundColor(Color.TRANSPARENT)
					webView.isVerticalScrollBarEnabled = true
					webView.settings.also { webSettings ->
						webSettings.allowFileAccess = false
						webSettings.loadWithOverviewMode = true
						webSettings.setSupportZoom(true)
						webSettings.displayZoomControls = true
					}
					webView.loadUrl("file:///android_asset/policy.html")
					webView
				}
			)
		}
	}
}