package com.qust.helper.ui.page.eas

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.data.eas.Notice
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.Texts
import com.qust.helper.ui.widget.Toast
import com.qust.helper.viewmodel.eas.GetNoticeViewModel

object GetNotice {

	val GetNoticePage = Page(Keys.Page.GetNoticePage, "教务通知", iconRes = R.drawable.ic_notification) { activity, padding, navController ->
		val viewModel by activity.viewModels<GetNoticeViewModel>()
		GetNotice(padding, viewModel, activity.toast, navController)
	}

	@Composable
	fun GetNotice(padding: PaddingValues, viewModel: GetNoticeViewModel, toast: Toast, navController: NavController){
		AppWidgets.CheckEasLogin(viewModel = viewModel, navController = navController)

		Box(modifier = Modifier.padding(padding)){
			GetNoticeUI(
				hasRefresh = viewModel.hasRefresh,
				refreshing = viewModel.refreshing,
				notices = viewModel.notices.value,
				query = { viewModel.queryNotice() }
			)
		}
		toast.ToastContent(viewModel.toastContent)
	}

	@Composable
	@OptIn(ExperimentalMaterialApi::class)
	fun GetNoticeUI(hasRefresh: Boolean, refreshing: Boolean, notices: Array<Notice>, query: () -> Unit){
		val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = { query() })

		LaunchedEffect(hasRefresh){
			if(notices.isEmpty() && !hasRefresh && !refreshing){ query() }
		}

		Box(modifier = Modifier.fillMaxSize().pullRefresh(state)){
			LazyColumn(Modifier.fillMaxSize()){
				items(notices.size) { index ->
					NoticeItem(notices[index])
				}
			}
			PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
		}
	}

	@Composable
	fun NoticeItem(notice: Notice){
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.background,
			),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {

			Column(modifier = Modifier.fillMaxWidth()) {

				Text(
					modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
					text = notice.content,
					style = MaterialTheme.typography.bodyMedium,
				)

				Texts.SingleLineText(
					text = notice.time,
					style = MaterialTheme.typography.bodySmall,
					textAlign = TextAlign.End,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}