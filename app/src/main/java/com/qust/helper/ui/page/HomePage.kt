package com.qust.helper.ui.page

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.qust.helper.ui.page.lesson.TermLesson

object HomePage {

	@Composable
	fun HomePage(activity: ComponentActivity){
		TermLesson.TermLesson(activity = activity)
	}
}