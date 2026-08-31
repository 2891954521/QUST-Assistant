package com.qust.helper.next.ui.page.eas

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.entity.eas.Exam
import com.qust.helper.next.module.eas.ExamModel
import com.qust.helper.next.repository.ExamRepository
import com.qust.helper.next.ui.business.eas.EasQueryLayout
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.eas.BaseEasViewModel


@Preview
@Composable
private fun QueryExamPreview() = AppPreview(::QueryExamUI)

class QueryExamPage : AppPage<QueryExamViewModel>(title = "考试查询", viewModelClass = QueryExamViewModel::class) {
	@Composable
	override fun Content(viewModel: QueryExamViewModel) = QueryExamUI(viewModel)
}

@Composable
private fun QueryExamUI(viewModel: QueryExamViewModel) {
	EasQueryLayout(
		pickYear = viewModel.pickYear,
		onYearPick = {
			viewModel.pickYear = it
			viewModel.changeTerm(it)
		},
		doQuery = { viewModel.query() },
	) {
		GetExamsUI(exams = viewModel.exams) {
			viewModel.clearNew(it)
		}
	}
}


@Composable
private fun GetExamsUI(exams: List<Exam>, onClickNew: (Int) -> Unit = {}) {
	LazyColumn {
		items(exams.size) { index ->
			ExamItem(exams[index]) {
				onClickNew(index)
			}
		}
	}
}

@Composable
private fun ExamItem(exam: Exam, onClickNew: () -> Unit = {}) {
	Box {
		Card(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			shape = RoundedCornerShape(8.dp),
			colors = CardDefaults.cardColors(containerColor = Theme.color.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {

			Column(
				modifier = Modifier.fillMaxWidth().clickable { if(exam.isNew == 1) onClickNew() }.padding(8.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = exam.name,
					style = Theme.textStyles.bodyStrong,
				)
				Row(modifier = Modifier.fillMaxWidth()) {
					Text(text = "考试地点: ")
					Text(text = exam.place, modifier = Modifier.weight(1F))
				}
				Row(modifier = Modifier.fillMaxWidth()) {
					Text(text = "考试时间: ")
					Text(text = exam.time, modifier = Modifier.weight(1F))
				}
			}
		}

		if(exam.isNew == 1) {
			Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp, 4.dp).background(Theme.color.primary, RoundedCornerShape(8.dp))) {
				Text(text = Strings.TEXT_NEW, modifier = Modifier.padding(4.dp), color = Theme.color.onPrimary, fontSize = 10.sp)
			}
		}
	}
}


class QueryExamViewModel : BaseEasViewModel() {

	val exams = mutableStateListOf<Exam>()

	private val examData: MutableList<List<Exam>?> = MutableList(Strings.ARRAY_TERM_NAME.size) { null }

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		changeTerm(pickYear)
	}

	fun query() {
		loading("查询中") {
			val term = pickYear
			val picker = getPickTerm()
			val resultData = ExamModel.queryExam(term, picker.first, picker.second)

			val origData = examData[term] ?: ExamRepository.getExamsByTerm(term)

			if(origData.isEmpty()) {
				ExamRepository.insertAll(resultData)
				examData[term] = null
				loadTerm(term)
				toastSuccess("查询完成")
				return@loading
			}

			val origin = origData.toSet()
			val result = resultData.toSet()

			val new = result subtract origin
			val update = origin intersect result
			if(new.isNotEmpty() || update.isNotEmpty()) {
				ExamRepository.updateAll(update.toList())
				ExamRepository.insertAll(new.toList())
				examData[term] = null
				loadTerm(term)
				toastSuccess("新查询到 ${new.size} 门考试")
			} else {
				toastSuccess("未查询到新考试")
			}
		}
	}

	fun changeTerm(index: Int) {
		val data = examData[index]
		if(data == null) {
			runInBackground {
				loadTerm(index)
			}
		} else {
			exams.clear()
			exams.addAll(data)
		}
	}

	fun clearNew(index: Int) {
		val exam = exams.getOrNull(index) ?: return
		if(exam.isNew != 1) return

		exams[index] = exam.copy(isNew = 0)
		examData[pickYear] = exams.toList()
		runInBackground {
			ExamRepository.setRead(exam.id)
		}
	}

	private suspend fun loadTerm(index: Int) {
		val data = ExamRepository.getExamsByTerm(index)
		examData[index] = data
		exams.clear()
		exams.addAll(data)
	}
}
