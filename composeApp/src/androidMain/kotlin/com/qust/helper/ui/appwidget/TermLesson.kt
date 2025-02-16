package com.qust.helper.ui.appwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.data.lesson.Lesson
import com.qust.helper.data.lesson.LessonGroup
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.ui.LESSON_BACKGROUND_COLORS
import com.qust.helper.ui.LESSON_TEXT_COLORS
import com.qust.helper.ui.widget.LessonTableView
import kotlinx.serialization.Serializable
import java.util.Arrays
import java.util.Calendar
import java.util.Date

private val startDayPreference = longPreferencesKey("startDay")
private val showWeekPreference = intPreferencesKey("showWeek")

private val pageParamKey = ActionParameters.Key<Int>("page")


class TermLessonReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = TermLesson()
}


class TermLesson : GlanceAppWidget() {

	companion object {
		var lessonTableData: Array<Array<UpdateActionCallback.LessonHolder?>> = Array(7) { arrayOfNulls(10) }
	}

	override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

	override val sizeMode = SizeMode.Exact

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		provideContent { GlanceTheme { LessonView() } }
	}

	override suspend fun onDelete(context: Context, glanceId: GlanceId) {
		super.onDelete(context, glanceId)
	}

	@Composable
	fun LessonView() {
		val prefs = currentState<Preferences>()
		val currentWeek = prefs[showWeekPreference] ?: 0

		val size = LocalSize.current

		val cell = (size.height) / 23F
		val cellWidth = (size.width) / 8
		val cellHeight = cell * 2

		Column(
			modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.card_background)),
			horizontalAlignment = Alignment.Start,
			verticalAlignment = Alignment.CenterVertically
		){

			LessonStatusBar(week = currentWeek, height = cell * 1.5F)
			LessonDateBar(startDay = prefs[startDayPreference]?.let { Date(it) } ?: Date(), week = currentWeek, width = cellWidth, height = cell * 1.5F)

			Row(modifier = GlanceModifier.fillMaxSize()) {
				LessonTimeBar(if(LessonTableRepository.currentTimeTable == 0) LessonTableView.LESSON_TIME1 else LessonTableView.LESSON_TIME2, cellWidth, cellHeight)

				for(week in 0..6){
					Column(modifier = GlanceModifier.fillMaxHeight().width(cellWidth)) {
						var len = 0
						var needPlace = false
						var i = 0
						val max = lessonTableData[week].size
						while(i < max){
							val lessonHolder = lessonTableData[week][i]
							if(lessonHolder != null) {
								val lesson = lessonHolder.current(currentWeek)
								if(lesson != null){
									if(needPlace){
										Box(modifier = GlanceModifier.height(cellHeight * len).width(cellWidth)) { }
										len = 0; needPlace = false
									}
									Box(modifier = GlanceModifier.size(cellWidth, cellHeight * lesson.len).padding(2.dp)){
										Column(
											modifier = GlanceModifier.fillMaxSize().background(LESSON_BACKGROUND_COLORS[lesson.color]),
											horizontalAlignment = Alignment.CenterHorizontally,
											verticalAlignment = Alignment.Vertical.CenterVertically
										) {
											TextStyleProvider(TextStyle(color = ColorProvider(LESSON_TEXT_COLORS[lesson.color]), fontSize = 12.sp, textAlign = TextAlign.Center)){ textStyle ->
												Text(text = lesson.name, style = textStyle, maxLines = 3)
												if(cellHeight > 40.dp){
													Text(text = lesson.place, style = textStyle, maxLines = 2)
													Text(text = lesson.teacher, style = textStyle, maxLines = 1)
												}
											}
										}
									}
									i += lesson.len
								} else { i++; len++; needPlace = true }
							} else { i++; len++; needPlace = true }

						}
					}
				}
			}
		}
	}

	@Composable
	fun TextStyleProvider(textStyle: TextStyle, content: @Composable (TextStyle) -> Unit) { content(textStyle) }

	@Composable
	fun LessonStatusBar(week: Int, height: Dp) {
		Row(modifier = GlanceModifier.fillMaxWidth().height(height), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Vertical.CenterVertically) {
			Box(GlanceModifier.fillMaxHeight().clickable(actionRunCallback<UpdateActionCallback>(actionParametersOf(pageParamKey to (week - 1)))), contentAlignment = Alignment.Center) {
				Image(provider = ImageProvider(R.drawable.ic_arrow_left), contentDescription = null, modifier = GlanceModifier.padding(8.dp))
			}
			Text(text = "第 ${week + 1} 周", style = TextStyle(fontSize = 16.sp), modifier = GlanceModifier.padding(start = 8.dp, end = 8.dp))
			Box(GlanceModifier.fillMaxHeight().clickable(actionRunCallback<UpdateActionCallback>(actionParametersOf(pageParamKey to (week + 1)))), contentAlignment = Alignment.Center){
				Image(provider = ImageProvider(R.drawable.ic_arrow_right), contentDescription = null, modifier = GlanceModifier.padding(8.dp))
			}
		}
	}

	@Composable
	fun LessonDateBar(startDay: Date, week: Int, width: Dp, height: Dp) {
		val currentDay = Calendar.getInstance()
		val c = Calendar.getInstance().also { it.time = startDay }
		c[Calendar.WEEK_OF_YEAR] += week
		c[Calendar.DATE] -= (c[Calendar.DAY_OF_WEEK] - 2)

		val textColor = ColorProvider(LESSON_TEXT_COLORS[0])

		Row(modifier = GlanceModifier.fillMaxWidth().height(height).padding(start = width), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Vertical.CenterVertically) {
			if(height >= 25.dp) {
				repeat(Data.WEEK_STRING.size) {
					val color = if(currentDay[Calendar.DATE] == c[Calendar.DATE] && currentDay[Calendar.MONTH] == c[Calendar.MONTH]) textColor else GlanceTheme.colors.onSurfaceVariant
					Column(modifier = GlanceModifier.width(width).defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Vertical.CenterVertically) {
						Text(text = Data.WEEK_STRING[it], style = TextStyle(color = color, fontSize = 12.sp))
						Text(text = "${c[Calendar.MONTH] + 1}-${c[Calendar.DATE]}", style = TextStyle(color = color, fontSize = 12.sp))
					}
					c[Calendar.DATE] += 1
				}
			}else{
				repeat(Data.WEEK_STRING.size) {
					val color = if(currentDay[Calendar.DATE] == c[Calendar.DATE] && currentDay[Calendar.MONTH] == c[Calendar.MONTH]) textColor else GlanceTheme.colors.onSurfaceVariant
					Text(text = Data.WEEK_STRING[it], style = TextStyle(color = color, fontSize = 12.sp, textAlign = TextAlign.Center), modifier = GlanceModifier.width(width).defaultWeight())
					c[Calendar.DATE] += 1
				}
			}
		}
	}

	@Composable
	fun LessonTimeBar(time: Array<Array<String>>, width: Dp, height: Dp) {
		if(height > 45.dp){
			TextStyleProvider(TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)){ textStyle ->
				Column(modifier = GlanceModifier.fillMaxHeight().width(width),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					repeat(time[0].size) {
						Column(
							modifier = GlanceModifier.defaultWeight(),
							verticalAlignment =  Alignment.CenterVertically,
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Text(text = (it + 1).toString())
							Text(text = "${time[0][it]}\n${time[1][it]}", style = textStyle)
						}
					}
				}
			}
		}else{
			TextStyleProvider(TextStyle(textAlign = TextAlign.Center)) { textStyle ->
				Column(modifier = GlanceModifier.fillMaxHeight().width(width), horizontalAlignment = Alignment.CenterHorizontally) {
					repeat(time[0].size) {
						Text(text = (it + 1).toString(), style = textStyle, modifier = GlanceModifier.height(height).defaultWeight())
					}
				}
			}
		}
	}
}


class UpdateActionCallback : ActionCallback {
	override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
		val page = parameters[pageParamKey] ?: 0

		val lessonTable = LessonTableRepository.lessonTable
		val lessonGroups = lessonTable.lessons
		val lessons: Array<Array<LessonHolder?>> = Array(lessonGroups.size) { arrayOfNulls(lessonGroups[0].size) }
		for(dayOfWeek in lessonGroups.indices) {
			for(timeSlot in lessonGroups[0].indices) {
				lessonGroups[dayOfWeek][timeSlot]?.let { lessonGroup ->
					lessons[dayOfWeek][timeSlot] = LessonHolder(lessonGroup, lessonTable.totalWeek)
				}
			}
		}
		TermLesson.lessonTableData = lessons

		updateAppWidgetState(context = context, definition = PreferencesGlanceStateDefinition, glanceId = glanceId){ preferences ->
			preferences.toMutablePreferences().apply {
				this[showWeekPreference] = page.coerceAtLeast(0).coerceAtMost(lessonTable.totalWeek - 1)
				this[startDayPreference] = lessonTable.startDay.time
			}
		}
		TermLesson().update(context, glanceId)
	}

	@Serializable
	class LessonData(
		val name: String,
		val place: String,
		val teacher: String,
		val len: Int,
		val color: Int,
	){
		constructor(lesson: Lesson) : this(
			name = lesson.name,
			place = lesson.place,
			teacher = lesson.teacher,
			len = lesson.len,
			color = lesson.color
		)
	}

	class LessonHolder(lessonGroup: LessonGroup, totalWeek: Int) {

		/**
		 * 某一周的这个时间点同时有几节课
		 */
		var lessonCount = IntArray(totalWeek)

		/**
		 * 当前正在展示的是第几节课，从0开始
		 */
		var index = IntArray(totalWeek).also { Arrays.fill(it, -1) }

		/**
		 * 某一周的这个时间点同时有课的课
		 * 5 = 101，表示当第1，3课会上，第2课不会上
		 */
		private var lessonTime = IntArray(totalWeek)

		private var lessonData: Array<LessonData>

		init {
			var offset = 1
			val array = ArrayList<LessonData>(lessonGroup.lessons.size)
			lessonGroup.lessons.forEachIndexed { i, lesson ->
				array.add(LessonData(lesson))
				var week = 1L
				for(j in 0 until totalWeek) {
					if(lesson.week and week > 0) {
						if(index[j] == -1) index[j] = i
						lessonTime[j] = lessonTime[j] or offset
						lessonCount[j]++
					}
					week = week shl 1
				}
				offset = offset shl 1
			}
			lessonData = array.toTypedArray()
		}

		fun current(week: Int): LessonData? {
			return if(lessonCount[week] == 0) null else lessonData[index[week]]
		}
	}
}


