package com.qust.helper.next.ui.page.eas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.eas.Notice
import com.qust.helper.next.module.eas.NoticeModel
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.list.PagedListAdapter
import com.qust.helper.next.ui.component.list.SuperListUI
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.BaseViewModel


@Preview
@Composable
private fun QueryNoticePreview() = AppPreview(::QueryNoticeUI)

class QueryNoticePage : AppPage<QueryNoticeViewModel>(
	title = "教务通知",
	viewModelClass = QueryNoticeViewModel::class,
) {
	@Composable
	override fun Content(viewModel: QueryNoticeViewModel) = QueryNoticeUI(viewModel)
}

@Composable
private fun QueryNoticeUI(viewModel: QueryNoticeViewModel) {
	SuperListUI(
		adapter = viewModel.notices,
		modifier = Modifier.fillMaxSize(),
	) { _, notice ->
		NoticeItem(notice)
	}
}

@Composable
private fun NoticeItem(notice: Notice) {
	Card(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = Theme.color.background),
		elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
	) {
		Column(modifier = Modifier.fillMaxWidth()) {
			Text(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
				text = notice.content,
				style = Theme.textStyles.body,
			)

			Text(
				text = notice.time,
				style = Theme.textStyles.caption,
				color = Theme.color.textSecondary,
				textAlign = TextAlign.End,
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
			)
		}
	}
}

class QueryNoticeViewModel : BaseViewModel() {

	val notices = PagedListAdapter(this, defaultPageSize = 20) { page, pageSize ->
		NoticeModel.queryNotice(page, pageSize)
	}

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		notices.refresh()
	}
}
