package com.qust.helper.next.ui.page.eas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.eas.AcademicGroup
import com.qust.helper.next.entity.eas.AcademicInfo
import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.ui.business.eas.MarkDetailsUI
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.spinner.SpinnerWidget
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.eas.QueryAcademicViewModel


private val LESSON_TYPE = arrayOf(
	"", "在修", "未过", "未修", "已修",
	"校内被替代课程",
	"校内课程替代",
	"校内课程替代节点",
	"校外课程替换节点/校外认定课程",
	"校内被认定课程",
	"学业预警不审核课程",
)

@Preview
@Composable
private fun QueryAcademicPreview() = AppPreview(::QueryAcademicUI)

class QueryAcademicPage : AppPage<QueryAcademicViewModel>(
	title = "学业查询",
	viewModelClass = QueryAcademicViewModel::class,
) {
	@Composable
	override fun Content(viewModel: QueryAcademicViewModel) = QueryAcademicUI(viewModel)
}

@Composable
private fun QueryAcademicUI(viewModel: QueryAcademicViewModel) {
	Column {
		Row(
			modifier = Modifier.padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(text = "展示方式", modifier = Modifier.padding(8.dp), style = Theme.textStyles.body)

			Row(
				modifier = Modifier.clickable { viewModel.changeGroup() },
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = if(viewModel.showMode == 0) "课程类型" else "学期",
					modifier = Modifier.padding(8.dp),
					style = Theme.textStyles.caption,
					color = Theme.color.textSecondary,
				)
				Icon(
					imageVector = Icons.Rounded.ArrowDropDown,
					contentDescription = null,
					tint = Theme.color.textSecondary,
				)
			}

			Spacer(modifier = Modifier.fillMaxWidth().weight(1F))

			Row(
				modifier = Modifier.clickable { viewModel.queryData() },
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = "重新查询",
					modifier = Modifier.padding(8.dp),
					style = Theme.textStyles.caption,
					color = Theme.color.textSecondary,
				)
				Icon(
					imageVector = Icons.Rounded.Refresh,
					contentDescription = null,
					tint = Theme.color.onSurfaceVariant,
				)
			}
		}

		AcademicList(viewModel)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AcademicList(viewModel: QueryAcademicViewModel) {
	val groups = viewModel.lessonGroups
	LazyColumn {
		groups.forEachIndexed { i, dataItem ->
			stickyHeader {
				GroupUI(
					group = dataItem,
					onClick = { viewModel.clickGroup(i) },
					sortBy = { viewModel.sortBy(i, it) },
					sortType = { viewModel.sortType(i) },
				)
			}
			if(dataItem.isExpand) {
				items(dataItem.lessons.size) { row ->
					ItemUI(
						lessonInfo = dataItem.lessons[row],
						lessonMark = dataItem.lessonMarks[row],
						isExpanded = dataItem.isLessonExpand[row],
						onClick = { viewModel.clickLesson(i, row) },
					)
				}
			}
		}
	}
}

@Composable
private fun GroupUI(
	group: AcademicGroupUIState,
	onClick: () -> Unit = {},
	sortBy: (Int) -> Unit = {},
	sortType: () -> Unit = {},
) {
	val lessonGroup = group.groupInfo
	val requireCredits = lessonGroup.requireCredits.takeIf { it > 0F } ?: 1F

	Card(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = Theme.color.background),
		elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
	) {
		Box(Modifier.clickable(onClick = onClick)) {
			Column(Modifier.padding(8.dp)) {
				Row(
					modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(text = lessonGroup.type, style = Theme.textStyles.bodyStrong)

					Column {
						Text(
							text = "共 ${lessonGroup.totalCounts} 门，通过 ${lessonGroup.passedCounts} 门",
							style = Theme.textStyles.caption,
							color = Theme.color.textSecondary,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.End,
						)
						Text(
							text = "要求学分 ${lessonGroup.requireCredits}，已修 ${lessonGroup.obtainedCredits}",
							style = Theme.textStyles.caption,
							color = Theme.color.textSecondary,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.End,
						)
					}
				}

				Spacer(modifier = Modifier.height(8.dp))

				TripleProgressBar(
					redValue = ((lessonGroup.obtainedCredits + lessonGroup.creditNotEarned) / requireCredits).coerceAtMost(1F),
					greenValue = (lessonGroup.obtainedCredits / requireCredits).coerceAtMost(1F),
				)

				if(group.isExpand) {
					Row(
						modifier = Modifier.padding(start = 8.dp, top = 4.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(text = "排序方式", style = Theme.textStyles.caption)

						SpinnerWidget(
							data = listOf("成绩排序", "学分排序", "修读状态"),
							onSelect = { i, _ -> sortBy(i) },
						) {
							Row(
								modifier = Modifier.padding(8.dp).clickable { isFlyoutVisible = true },
								verticalAlignment = Alignment.CenterVertically,
							) {
								Text(
									text = when(group.sortBy) {
										0 -> "成绩排序"
										1 -> "学分排序"
										2 -> "修读状态"
										else -> ""
									},
									style = Theme.textStyles.caption,
									color = Theme.color.textSecondary,
									modifier = Modifier.padding(start = 8.dp),
								)
								Icon(
									imageVector = Icons.Rounded.ArrowDropDown,
									contentDescription = null,
									tint = Theme.color.textSecondary,
								)
							}
						}

						Row(
							modifier = Modifier.padding(8.dp).clickable { sortType() },
							verticalAlignment = Alignment.CenterVertically,
						) {
							Text(
								text = if(group.sortType == 1) "升序" else "降序",
								style = Theme.textStyles.caption,
								color = Theme.color.textSecondary,
								modifier = Modifier.padding(start = 8.dp),
							)
							Icon(
								imageVector = Icons.Rounded.ArrowDropDown,
								modifier = Modifier.rotate(if(group.sortType == 1) 0F else 180F),
								contentDescription = null,
								tint = Theme.color.textSecondary,
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ItemUI(
	lessonInfo: AcademicInfo,
	lessonMark: Mark?,
	isExpanded: Boolean,
	onClick: () -> Unit,
) {
	Card(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = Theme.color.background),
		elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
	) {
		Column(
			modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 4.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Row(Modifier.height(IntrinsicSize.Min)) {
				Text(
					text = lessonInfo.name,
					modifier = Modifier.weight(1F),
					style = Theme.textStyles.bodyStrong,
					textAlign = TextAlign.Start,
				)
				Text(
					text = if(lessonInfo.status == 4) {
						"成绩: ${lessonInfo.mark}"
					} else {
						LESSON_TYPE.getOrElse(lessonInfo.status) { "" }
					},
					modifier = Modifier.fillMaxHeight(),
					textAlign = TextAlign.Center,
				)
			}

			Row {
				Text(
					text = "${lessonInfo.category} | ${lessonInfo.content}",
					style = Theme.textStyles.caption,
					color = Theme.color.textSecondary,
				)

				Spacer(modifier = Modifier.fillMaxWidth().weight(1F))

				Text(
					text = "学分: ${lessonInfo.credit}",
					style = Theme.textStyles.caption,
					color = Theme.color.textSecondary,
				)
			}

			AnimatedVisibility(visible = isExpanded && lessonMark != null) {
				lessonMark?.let { MarkDetailsUI(mark = it) }
			}
		}
	}
}

@Composable
private fun TripleProgressBar(
	redValue: Float = 0F,
	greenValue: Float = 0F,
	height: Dp = 5.dp,
	redColor: Color = Theme.color.error,
	greenColor: Color = Color(0xFF1DE9B6),
) {
	Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
		drawRoundRect(
			color = Color.Gray,
			topLeft = Offset(0F, 0F),
			size = Size(size.width, size.height),
			cornerRadius = CornerRadius(size.height / 2F),
		)
		if(redValue > 0F) {
			drawRoundRect(
				color = redColor,
				topLeft = Offset(0F, 0F),
				size = Size(size.width * redValue, size.height),
				cornerRadius = CornerRadius(size.height / 2F),
			)
		}
		if(greenValue > 0F) {
			drawRoundRect(
				color = greenColor,
				topLeft = Offset(0F, 0F),
				size = Size(size.width * greenValue, size.height),
				cornerRadius = CornerRadius(size.height / 2F),
			)
		}
	}
}

class AcademicGroupUIState(val groupInfo: AcademicGroup) {
	var sortBy = 0
	var hasQuery = false

	var isExpand by mutableStateOf(false)
	var sortType by mutableIntStateOf(-1)

	private val _lessons: MutableState<List<AcademicInfo>> = mutableStateOf(emptyList())

	var isLessonExpand = lessons.map { false }.toMutableStateList()
	var lessonMarks: Array<Mark?> = emptyArray()

	var lessons: List<AcademicInfo>
		get() = _lessons.value
		set(value) {
			isLessonExpand = value.map { false }.toMutableStateList()
			lessonMarks = Array(value.size) { null }
			_lessons.value = value
		}
}

