package com.qust.helper.ui.page.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.Globe
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.account.EasLoginPage
import com.qust.helper.ui.page.account.IpassLoginPage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.viewmodel.web.WebViewModel

object WebPage {

	/**
	 * 教务系统网页
	 */
	val EasWebPage = object : BasePage<WebViewModel>("教务系统", Drawables.Globe) {

		@Composable
		override fun getViewModel() = viewModel<WebViewModel>(key = "EasWebPage") {
			WebViewModel(EasAccount, "https://${EasAccount.host}/jwglxt/xtgl/index_initMenu.html")
		}

		@Composable
		override fun Content(viewModel: WebViewModel) {
			WebScreen(viewModel, EasLoginPage)
		}
	}

	/**
	 * 智慧青科大网页
	 */
	val IpassWebPage = object : BasePage<WebViewModel>("智慧青科大", Drawables.Globe) {

		@Composable
		override fun getViewModel() = viewModel<WebViewModel>(key = "IpassWebPage") {
			WebViewModel(
				IPassAccount,
				"https://${IPassAccount.host}/https/77726476706e69737468656265737421f9b95089342426557a1dc7af96/tp_up/view?m=up#act=portal/viewhome"
			)
		}

		@Composable
		override fun Content(viewModel: WebViewModel) {
			WebScreen(viewModel, IpassLoginPage)
		}
	}

	@Composable
	private fun WebScreen(viewModel: WebViewModel, loginPage: BasePage<*>) {
		val pageController = rememberPageController()

		LaunchedEffect(Unit) {
			if (!viewModel.hasCheck) viewModel.checkAccountLogin()
		}

		Column(Modifier.fillMaxSize()) {
			LinearProgressIndicator(
				progress = { viewModel.progress },
				modifier = Modifier.fillMaxWidth().height(if (viewModel.progress >= 1f) 0.dp else 4.dp),
				color = MaterialTheme.colorScheme.primary,
			)

			when {
				viewModel.isLogin -> {
					WebViewContent(
						url = viewModel.startUrl,
						cookies = viewModel.cookies,
						onProgress = viewModel::updateProgress,
					)
				}

				viewModel.needLogin -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Button(onClick = {
							viewModel.resetCheck()
							pageController.startPage(loginPage)
						}) {
							Text("请先登录")
						}
					}
				}

				else -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Text("正在登录...")
					}
				}
			}
		}
	}
}
