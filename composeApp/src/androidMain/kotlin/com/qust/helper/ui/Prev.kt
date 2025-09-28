package com.qust.helper.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.lesson.Lesson
import com.qust.helper.entity.lesson.TimeTable
import com.qust.helper.model.SettingModel
import com.qust.helper.ui.widget.lesson.lessonEdit.LessonWeekPicker
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableInfo
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUI
import com.qust.helper.ui.widget.lesson.lessonTable.LessonTableUIState
import com.qust.helper.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

@Preview(backgroundColor = 0xFFE6E6E6, showSystemUi = false)
@Composable
fun TestLessonTable() {
	val uiState = remember {
		val ui = LessonTableUIState(
			MutableStateFlow(LessonTableInfo(
					timeTable = TimeTable.Companion.DEFAULT,
					startDay = DateUtils.today(),
					totalWeek = 1,
					currentWeek = 0,
					lessons = listOf(
						Lesson(
							colorLabel = 1,
							week = 1,
							startMinute = 480,
							endMinute = 600,
							name = "高数1",
							place = "明德",
							teacher = "老师"
						),

						Lesson(
							colorLabel = 1,
							week = 2,
							startMinute = 500,
							endMinute = 640,
							name = "高数2",
							place = "明德",
							teacher = "老师"
						),
					)
				)
			)
		)
		ui
	}
	LessonTableUI(uiState)
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestLessonTime() {
	val s = remember { mutableStateOf(0) }
	val e = remember { mutableStateOf("") }

	LessonWeekPicker("上课时间", s, Strings.ARRAY_WEEK_NAME)
}