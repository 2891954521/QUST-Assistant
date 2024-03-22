package com.qust.helper.ui.page.eas

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.data.eas.Notice
import com.qust.helper.ui.common.Texts
import com.qust.helper.viewmodel.eas.GetNoticeViewModel

object GetNotice {

	@Composable
	fun GetNotice(activity: ComponentActivity){
		val viewModel: GetNoticeViewModel by activity.viewModels()
		GetNoticeUI(notices = viewModel.notices.value) { refreshing ->
			refreshing.value = true
			viewModel.queryNotice {
				refreshing.value = false
			}
		}

	}

	@Composable
	@OptIn(ExperimentalMaterialApi::class)
	fun GetNoticeUI(notices: Array<Notice>, query: (MutableState<Boolean>) -> Unit){
		val refreshing = remember { mutableStateOf(false) }
		var hasRefresh by remember { mutableStateOf(false) }

		val state = rememberPullRefreshState(refreshing = refreshing.value, onRefresh = { query(refreshing) })

		if(notices.isEmpty() && !hasRefresh){ hasRefresh = true; query(refreshing) }

		Box(modifier = Modifier.fillMaxSize().pullRefresh(state)){
			LazyColumn(Modifier.fillMaxSize()){
				items(notices.size) { index ->
					NoticeItem(notices[index])
				}
			}
			PullRefreshIndicator(refreshing.value, state, Modifier.align(Alignment.TopCenter))
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