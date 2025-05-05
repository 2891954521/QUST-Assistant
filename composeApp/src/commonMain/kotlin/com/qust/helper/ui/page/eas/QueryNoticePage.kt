package com.qust.helper.ui.page.eas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.entity.eas.Notice
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.Notification
import com.qust.helper.ui.page.BasePage
import com.qust.helper.viewmodel.eas.QueryNoticeViewModel

object QueryNoticePage: BasePage<QueryNoticeViewModel>("教务通知", Drawables.Notification) {

	@Composable
	override fun getViewModel() = viewModel<QueryNoticeViewModel>()

	@Composable
	@OptIn(ExperimentalMaterialApi::class)
	override fun Content(viewModel: QueryNoticeViewModel) {
		val state = rememberPullRefreshState(refreshing = viewModel.refreshing, onRefresh = { viewModel.queryNotice() })
		LaunchedEffect(viewModel.hasRefresh){
			if(viewModel.notices.isEmpty() && !viewModel.hasRefresh && !viewModel.refreshing){ viewModel.queryNotice() }
		}

		Box(modifier = Modifier.fillMaxSize().pullRefresh(state)){
			LazyColumn(Modifier.fillMaxSize()){
				items(viewModel.notices.size) { index ->
					NoticeItem(viewModel.notices[index])
				}
			}
			PullRefreshIndicator(viewModel.refreshing, state, Modifier.align(Alignment.TopCenter))
		}
	}

	@Composable
	fun NoticeItem(notice: Notice){
		Card(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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

				Text(
					text = notice.time,
					style = MaterialTheme.typography.bodySmall,
					textAlign = TextAlign.End,
					modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
				)
			}
		}
	}
}