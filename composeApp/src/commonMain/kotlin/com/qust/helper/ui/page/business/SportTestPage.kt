package com.qust.helper.ui.page.business

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.entity.business.SportUpload
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.account.IpassLoginPage
import com.qust.helper.ui.page.rememberPageController
import com.qust.helper.ui.widget.ListDialog
import com.qust.helper.viewmodel.business.SportTestViewModel

object SportTestPage: BasePage<SportTestViewModel>("智慧体测", Drawables.School) {

	@Composable
	override fun getViewModel() = viewModel<SportTestViewModel>()

	@Composable
	override fun Content(viewModel: SportTestViewModel) {
		val pageController = rememberPageController()

		LaunchedEffect(Unit) {
			viewModel.queryYears()
		}

		LaunchedEffect(viewModel.needLogin) {
			if(viewModel.needLogin) {
				viewModel.needLogin = false
				pageController.startPage(IpassLoginPage)
			}
		}

		Column(modifier = Modifier.fillMaxSize()) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
			) {
				Text(text = "学年: ", style = MaterialTheme.typography.bodyMedium)
				var showYearPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
				TextButton(onClick = { showYearPicker = true }) {
					Text(text = viewModel.years.value.firstOrNull { it.value == viewModel.selectedYear }?.text ?: "选择学年")
				}
				if(showYearPicker) {
					ListDialog(
						title = "选择学年",
						items = viewModel.years.value.map { it.text }.toTypedArray(),
						onDismiss = { showYearPicker = false }
					) { items, index ->
						showYearPicker = false
						viewModel.years.value.getOrNull(index)?.let { viewModel.queryTasks(it.value) }
					}
				}
				Spacer(modifier = Modifier.weight(1F))
				TextButton(onClick = { viewModel.queryDetail(viewModel.selectedYear) }) {
					Text(text = "查看成绩明细")
				}
			}

			LazyColumn {
				items(viewModel.scores.value, key = { it.uploadId }) { item ->
					SportScoreCard(item)
				}
			}
		}
	}

	@Composable
	fun SportScoreCard(item: SportUpload) {
		Card(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		) {
			Column(modifier = Modifier.padding(12.dp)) {
				Text(text = item.projectName, style = MaterialTheme.typography.titleMedium)
				Text(text = "成绩: ${item.score}", style = MaterialTheme.typography.bodyMedium)
				Text(
					text = "测试时间: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(item.testTime))}",
					style = MaterialTheme.typography.bodySmall
				)
			}
		}
	}
}
