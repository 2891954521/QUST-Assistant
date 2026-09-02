package com.qust.helper.next.ui.page.eas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.entity.lesson.LessonTableInfo
import com.qust.helper.next.module.eas.LessonModel
import com.qust.helper.next.network.entity.lesson.LessonTableQueryResult
import com.qust.helper.next.repository.LessonTableRepository
import com.qust.helper.next.repository.SettingRepository
import com.qust.helper.next.ui.business.eas.EasQueryLayout
import com.qust.helper.next.ui.business.lesson.LessonTableUI
import com.qust.helper.next.ui.business.lesson.LessonTableUIState
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.ListItemPicker
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.eas.BaseEasViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


@Preview
@Composable
private fun QueryLessonTablePreview() = AppPreview(::QueryLessonTableUI)

class QueryLessonTablePage : AppPage<QueryLessonTableViewModel>(
	title = "课表查询",
	viewModelClass = QueryLessonTableViewModel::class,
) {
	@Composable
	override fun Content(viewModel: QueryLessonTableViewModel) = QueryLessonTableUI(viewModel)
}

@Composable
private fun QueryLessonTableUI(viewModel: QueryLessonTableViewModel) {
	Box(modifier = Modifier.fillMaxSize()) {
		EasQueryLayout(
			pickYear = viewModel.pickYear,
			onYearPick = { viewModel.pickYear = it },
			doQuery = { viewModel.queryLesson() },
			searchBar = {
				ListItemPicker(
					value = Strings.ARRAY_QUERY_LESSON_TYPE[viewModel.pickType],
					list = Strings.ARRAY_QUERY_LESSON_TYPE,
					onValueChange = { i, _ -> viewModel.pickType = i },
					horizontalPadding = 8.dp,
				)
			},
		) {
			GetLessonTableUI(
				termText = viewModel.termText,
				lessonTable = viewModel.lessonUIState,
			)
		}

		if(viewModel.needSave) {
			FloatingActionButton(
				onClick = { viewModel.saveLesson() },
				modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
			) {
				Icon(Icons.Filled.Done, contentDescription = Strings.TEXT_SAVE_LESSON_TABLE)
			}
		}
	}
}

@Composable
private fun GetLessonTableUI(
	termText: String = "",
	lessonTable: LessonTableUIState,
) {
	Text(
		text = termText,
		color = Theme.color.textSecondary,
		style = Theme.textStyles.caption,
		modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp),
	)

	LessonTableUI(lessonTable)
}

class QueryLessonTableViewModel : BaseEasViewModel() {

	private val lessonTableInfo = MutableStateFlow(
		LessonTableInfo(
			timeTable = TimeTable.DEFAULT,
			startDay = SettingRepository.startDay,
			currentWeek = 0,
			totalWeek = 1,
		)
	)

	val lessonUIState = LessonTableUIState(lessonTableInfo)

	var termText by mutableStateOf("")

	var needSave by mutableStateOf(false)

	var pickType by mutableIntStateOf(0)

	private var queryResult: LessonTableQueryResult? = null

	fun queryLesson() {
		loading("查询中") {
			val picker = getPickTerm()
			val result = LessonModel.queryLessonTable(picker.first, picker.second)

			val error = result.error
			if(error != null) {
				toastError(error)
				return@loading
			}

			termText = result.termText
			needSave = true
			queryResult = result

			val lessons = result.lessons
			if(lessons != null) {
				lessonTableInfo.update {
					it.copy(
						totalWeek = result.totalWeek,
						lessons = lessons,
					)
				}
			}

			toastSuccess("获取课表成功！")
		}
	}

	fun saveLesson() {
		val result = queryResult
		if(result == null) {
			toastWarning("请先查询课表")
			return
		}

		loading("保存中") {
			LessonTableRepository.saveLessonTable(result)
			needSave = false
			toastSuccess("保存课表成功")
		}
	}
}
