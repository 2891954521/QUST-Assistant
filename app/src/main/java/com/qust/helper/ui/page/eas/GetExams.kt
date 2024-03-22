package com.qust.helper.ui.page.eas

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qust.helper.data.eas.Exam
import com.qust.helper.ui.common.Texts
import com.qust.helper.viewmodel.eas.GetExamsViewModel

object GetExams {

	@Composable
	fun GetExams(activity: ComponentActivity) {
		val viewModel: GetExamsViewModel by activity.viewModels()

		val exams by viewModel.exams

		EasQuery(
			viewModel = viewModel,
			items = exams,
			itemView = { ExamItem(it) },
			onYearPick = {
				viewModel.exams.value = viewModel.examData[it]
			}
		){
			viewModel.queryMarks { }
		}
	}

	@Composable
	fun ExamItem(exam: Exam){
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

				Texts.SingleLineText(
					text = exam.name,
					style = MaterialTheme.typography.titleMedium,
				)
				Row(modifier = Modifier.fillMaxWidth()) {
					Texts.SingleLineText(text = "考试地点: ")
					Texts.SingleLineText(text = exam.place, modifier = Modifier.weight(1F))
				}
				Row(modifier = Modifier.fillMaxWidth()){
					Texts.SingleLineText(text = "考试时间: ")
					Texts.SingleLineText(text = exam.time, modifier = Modifier.weight(1F))
				}
			}
		}
	}
}
