package com.qust.helper.ui.page.eas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.eas.Exam
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.LocalColor
import com.qust.helper.viewmodel.eas.QueryExamViewModel

object QueryExamPage: BasePage<QueryExamViewModel>("考试查询", Drawables.School) {

	@Composable
	override fun getViewModel() = viewModel<QueryExamViewModel>()

	@Composable
	override fun Content(viewModel: QueryExamViewModel) {

		BaseEasQueryUI(
			pickYear = viewModel.pickYear.value,
			onYearPick = {
				viewModel.pickYear.value = it
				viewModel.changeTerm(it)
	        },
			doQuery = { viewModel.query() },
		) {
			GetExamsUI(exams = viewModel.exams){
				viewModel.clearNew(it)
			}
		}
	}


	@Composable
	private fun GetExamsUI(exams: List<Exam>, onClickNew: (Int) -> Unit = {}){
		LazyColumn {
			items(exams.size) { index ->
				ExamItem(exams[index]){
					onClickNew(index)
				}
			}
		}
	}

	@Composable
	private fun ExamItem(exam: Exam, onClickNew: () -> Unit = {}){
		Box {
			Card(
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
				shape = RoundedCornerShape(8.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.background,
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			) {

				Column(
					modifier = Modifier.fillMaxWidth().clickable { if(exam.isNew == 1) onClickNew() }.padding(8.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(8.dp)
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

			if(exam.isNew == 1) {
				Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp, 4.dp).background(LocalColor.current.primary, RoundedCornerShape(8.dp))) {
					Text(text = Strings.TEXT_NEW, modifier = Modifier.padding(4.dp), color = LocalColor.current.onPrimary, fontSize = 10.sp)
				}
			}
		}
	}
}