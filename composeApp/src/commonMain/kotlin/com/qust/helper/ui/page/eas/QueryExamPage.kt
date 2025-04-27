package com.qust.helper.ui.page.eas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.entity.eas.Exam
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.viewmodel.eas.QueryExamViewModel

object QueryExamPage: BasePage<QueryExamViewModel>("考试查询", Drawables.School) {

	@Composable
	override fun getViewModel() = viewModel<QueryExamViewModel>()

	@Composable
	override fun Content(viewModel: QueryExamViewModel) {

		BaseEasQueryUI(
			pickYear = viewModel.pickYear.value,
			onYearPick = { viewModel.pickYear.value = it },
			doQuery = { viewModel.query() },
		) {
			GetExamsUI(
				exams = viewModel.exams,
			)
		}
	}


	@Composable
	private fun GetExamsUI(exams: List<Exam>){
		LazyColumn {
			items(exams.size) { index ->
				ExamItem(exams[index])
			}
		}
	}

	@Composable
	private fun ExamItem(exam: Exam){
		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
			shape = RoundedCornerShape(8.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.background,
			),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {

			Column(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {

				Text(
					text = exam.name,
					style = MaterialTheme.typography.titleMedium,
				)
				Row(modifier = Modifier.fillMaxWidth()) {
					Text(text = "考试地点: ")
					Text(text = exam.place, modifier = Modifier.weight(1F))
				}
				Row(modifier = Modifier.fillMaxWidth()){
					Text(text = "考试时间: ")
					Text(text = exam.time, modifier = Modifier.weight(1F))
				}
			}
		}
	}
}