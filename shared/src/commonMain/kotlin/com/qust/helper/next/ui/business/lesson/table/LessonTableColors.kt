package com.qust.helper.next.ui.business.lesson.table

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class LessonTableColors(
	val lessonBackground: List<Color>,
	val lessonText: List<Color>,
	val upcomingBackground: Color,
	val upcomingText: Color,
	val finishedBackground: Color,
	val finishedText: Color,
	val todayText: Color,
	val timeBarText: Color,
	val timeBarSecondaryText: Color,
	val gapLine: Color,
)
