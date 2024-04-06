package com.qust.helper.ui.appwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.qust.helper.R
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.widget.LessonTableView

private val countPreferenceKey = intPreferencesKey("widget-key")
private val countParamKey = ActionParameters.Key<Int>("widget-key")

class TermLesson : GlanceAppWidget() {

	override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		provideContent { GlanceTheme { LessonView() } }
	}

	override suspend fun onDelete(context: Context, glanceId: GlanceId) {
		super.onDelete(context, glanceId)
	}

	@Composable
	fun LessonView() {
//		val prefs = currentState<Preferences>()
//		val count = prefs[countPreferenceKey] ?: 1
		Row(
			modifier = GlanceModifier.fillMaxSize().padding(4.dp).background(ImageProvider(R.drawable.card_background)),
//				.clickable(actionStartActivity<MainActivity>()),
			horizontalAlignment = Alignment.Start,
			verticalAlignment = Alignment.CenterVertically
		){

			if(LessonTableRepository.currentTimeTable == 0) LessonTimeBar1()
			else LessonTimeBar2()

//			Column {
//				Text(
//					text = count.toString(),
//					modifier = GlanceModifier.fillMaxWidth(),
//					style = TextStyle(
//						textAlign = TextAlign.Center,
//						color = ColorProvider(Color.Blue),
//						fontSize = 50.sp
//					)
//				)
//				Button(text = "test", onClick = actionRunCallback<UpdateActionCallback>(parameters = actionParametersOf(countParamKey to (count + 1))))
//			}

		}
	}

	@Composable
	fun LessonTimeBar1() {
		Column(modifier = GlanceModifier.fillMaxHeight()) {
			repeat(LessonTableView.LESSON_TIME1[0].size) {
				Column(
					modifier = GlanceModifier.padding(4.dp),
					verticalAlignment =  Alignment.CenterVertically,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(text = (it + 1).toString())
					Text(text = LessonTableView.LESSON_TIME1[0][it], style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
					Text(text = LessonTableView.LESSON_TIME1[1][it], style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
				}
			}
		}
	}

	@Composable
	fun LessonTimeBar2() {
		Column(modifier = GlanceModifier.fillMaxHeight()) {
			repeat(LessonTableView.LESSON_TIME2[0].size) {
				Column(
					modifier = GlanceModifier.padding(4.dp),
					verticalAlignment =  Alignment.CenterVertically,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(text = (it + 1).toString())
					Text(text = LessonTableView.LESSON_TIME2[0][it], style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
					Text(text = LessonTableView.LESSON_TIME2[1][it], style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
				}
			}
		}
	}
}

class TermLessonReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = TermLesson()
}



class UpdateActionCallback : ActionCallback {
	override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
		val count = requireNotNull(parameters[countParamKey])

		updateAppWidgetState(
			context = context,
			definition = PreferencesGlanceStateDefinition,
			glanceId = glanceId
		){ preferences ->
			preferences.toMutablePreferences()
				.apply {
					this[countPreferenceKey] = count
				}
		}
		TermLesson().update(context, glanceId)
	}
}