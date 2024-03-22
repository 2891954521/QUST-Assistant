package com.qust.helper.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qust.helper.data.Data
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.ui.common.Texts
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.viewmodel.LessonTableViewModel

object DailyLesson {

	@Composable
	fun DailyLesson(activity: ComponentActivity){
		val viewModel: LessonTableViewModel by activity.viewModels()
		val lessonTable by viewModel.lessonTable
		DailyLessonUI(
			lessonGroups = lessonTable.lessons[viewModel.dayOfWeek.intValue],
			currentWeek = viewModel.currentWeek.intValue
		)
	}

	@Composable
	fun DailyLessonUI(
		lessonGroups: Array<LessonGroup?>,
		currentWeek: Int = 1
	) {
//		var minute = Calendar.getInstance().let{
//			(it[Calendar.HOUR_OF_DAY] - 8) * 60 + it[Calendar.MINUTE]
//		}

		Column(
			modifier = Modifier.padding(8.dp)
		) {
			Texts.SingleLineText(text = "上午课程")

			var noLesson = true
			for(i in 0 .. 3) {
				val lesson = lessonGroups[i]?.getCurrentLesson(currentWeek)
				if(lesson != null) {
					noLesson = false
					LessonItem(count = i, lesson = lesson)
				}
			}
			if(noLesson) LessonItemFree(count = 0)

			Texts.SingleLineText(text = "下午课程")

			noLesson = true
			for(i in 4 .. 7) {
				val lesson = lessonGroups[i]?.getCurrentLesson(currentWeek)
				if(lesson != null) {
					noLesson = false
					LessonItem(count = i, lesson = lesson)
				}
			}
			if(noLesson) LessonItemFree(count = 4)

			Texts.SingleLineText(text = "晚上课程")

			noLesson = true
			for(i in 8 .. 9) {
				val lesson = lessonGroups[i]?.getCurrentLesson(currentWeek)
				if(lesson != null) {
					noLesson = false
					LessonItem(count = i, lesson = lesson)
				}
			}
			if(noLesson) LessonItemFree(count = 8)
		}
	}

	@Composable
	fun LessonItem(count: Int = 0, lesson: Lesson = Lesson(), ){
		Row(
			Modifier.padding(8.dp).height(intrinsicSize = IntrinsicSize.Min),
			verticalAlignment = Alignment.CenterVertically
		) {

			Text(
				text = "${Data.LESSON_TIME_TEXT[0][0][count]}\n${Data.LESSON_TIME_TEXT[0][1][count + lesson.len - 1]}",
				color = colorSecondaryText,
				modifier = Modifier.padding(8.dp)
			)

			LessonLabel(color = Color(TEXT_COLORS[lesson.color]))

			Column(modifier = Modifier.weight(1F).padding(8.dp)) {
				Texts.SingleLineTextNoPadding(text = lesson.name, style = MaterialTheme.typography.titleLarge)
				Texts.SingleLineTextNoPadding(
					text = if(lesson.teacher.isEmpty() || lesson.place.isEmpty()) "${lesson.place}${lesson.teacher}" else "${lesson.place} | ${lesson.teacher}",
					color = colorSecondaryText
				)
			}
		}
	}

	@Composable
	fun LessonItemFree(count: Int){
		Row(
			Modifier.padding(8.dp).height(intrinsicSize = IntrinsicSize.Min),
			verticalAlignment = Alignment.CenterVertically
		) {

			Text(
				text = "${Data.LESSON_TIME_TEXT[0][0][count]}\n${Data.LESSON_TIME_TEXT[0][1][count + 1]}",
				color = colorSecondaryText,
				modifier = Modifier.padding(8.dp)
			)

			LessonLabel(color = Color(TEXT_COLORS[0]))

			Column(modifier = Modifier.weight(1F).padding(8.dp)) {
				Texts.SingleLineTextNoPadding(
					text = "空闲",
					style = MaterialTheme.typography.titleLarge,
					color = colorSecondaryText
				)
				Texts.SingleLineTextNoPadding(
					text = ""
				)
			}
		}
	}

	@Composable
	fun LessonLabel(color: Color){
		Canvas(modifier = Modifier.fillMaxHeight().padding(8.dp).width(6.dp)){
			drawRoundRect(
				color = color,
				topLeft = Offset(0F, 0F),
				size = Size(size.width, size.height),
				cornerRadius = CornerRadius(size.height / 2F, size.height / 2F),
			)
		}
	}
}