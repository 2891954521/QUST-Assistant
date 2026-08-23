package com.qust.helper.ui.page.eas

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.entity.eas.AcademicInfo
import com.qust.helper.entity.eas.Mark
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.utils.HtmlUtils
import com.qust.helper.ui.widget.components.IconText
import com.qust.helper.ui.widget.components.TripleProgressBar
import com.qust.helper.viewmodel.eas.AcademicGroupUIState
import com.qust.helper.viewmodel.eas.AcademicUIEvent
import com.qust.helper.viewmodel.eas.AcademicUIState
import com.qust.helper.viewmodel.eas.QueryAcademicViewModel

object QueryAcademicPage: BasePage<QueryAcademicViewModel>("学业查询", Drawables.School) {

	val LESSON_TYPE = arrayOf(
		"", "在修", "未过", "未修", "已修",
		"校内被替代课程",
		"校内课程替代",
		"校内课程替代节点",
		"校外课程替换节点/校外认定课程",
		"校内被认定课程",
		"学业预警不审核课程"
	)

	@Composable
	override fun getViewModel(): QueryAcademicViewModel = viewModel<QueryAcademicViewModel>()

	@Composable
	override fun Content(viewModel: QueryAcademicViewModel) {
		val uiState = viewModel.uiState
		val uiEvent = viewModel

		Column {
			Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {

				Text(text = "展示方式", modifier = Modifier.padding(8.dp))

				Row(modifier = Modifier.clickable { uiEvent.changeGroup() }, verticalAlignment = Alignment.CenterVertically){
					Text(text = if(uiState.showMode == 0) "课程类型" else "学期", modifier = Modifier.padding(8.dp), color = colorSecondaryText)
					Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = colorSecondaryText)
				}

				Spacer(modifier = Modifier.fillMaxWidth().weight(1F))

				Row(modifier = Modifier.clickable { uiEvent.queryData() }, verticalAlignment = Alignment.CenterVertically){
					Text(text = "重新查询", modifier = Modifier.padding(8.dp), color = colorSecondaryText)
					Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
				}
			}

			AcademicList(uiState, uiEvent)
		}
	}

	@Composable
	@OptIn(ExperimentalFoundationApi::class)
	private fun AcademicList(uiState: AcademicUIState, uiEvent: AcademicUIEvent){
		LazyColumn {
			uiState.lessonGroups.forEachIndexed { i, dataItem ->
				stickyHeader {
					GroupUI(dataItem,
						onClick = { uiEvent.clickGroup(i) },
						sortBy = { uiEvent.sortBy(i, it) },
						sortType = { uiEvent.sortType(i) }
					)
				}
				if(dataItem.isExpand) {
					items(dataItem.lessons.size) { row ->
						ItemUI(
							dataItem.lessons[row],
							dataItem.lessonMarks[row],
							dataItem.isLessonExpand[row]
						) { uiEvent.clickLesson(i, row) }
					}
				}
			}
		}
	}

	@Composable
	fun GroupUI(group: AcademicGroupUIState, onClick: () -> Unit = { }, sortBy: (Int) -> Unit = { }, sortType: () -> Unit = { }){
		val lessonGroup = group.groupInfo
		var showPop by remember { mutableStateOf(false) }

		Card(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Box(Modifier.clickable(onClick = onClick)) {
				Column(Modifier.padding(8.dp)) {

					Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
						Text(text = lessonGroup.type, style = MaterialTheme.typography.titleMedium, modifier = Modifier)

						Column {
							Text(
								text = "共 ${lessonGroup.totalCounts} 门，通过 ${lessonGroup.passedCounts} 门",
								style = MaterialTheme.typography.bodySmall,
								color = colorSecondaryText,
								modifier = Modifier.fillMaxWidth(),
								textAlign = TextAlign.End
							)

							Text(
								text = "要求学分 ${lessonGroup.requireCredits}，已修 ${lessonGroup.obtainedCredits}",
								style = MaterialTheme.typography.bodySmall,
								color = colorSecondaryText,
								modifier = Modifier.fillMaxWidth(),
								textAlign = TextAlign.End
							)
						}
					}

					Spacer(modifier = Modifier.height(8.dp))

					TripleProgressBar(
						redValue = ((lessonGroup.obtainedCredits + lessonGroup.creditNotEarned) / lessonGroup.requireCredits).coerceAtMost(1F),
						greenValue = (lessonGroup.obtainedCredits / lessonGroup.requireCredits).coerceAtMost(1F)
					)

					if(group.isExpand){
						Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
							Text(text = "排序方式", style = MaterialTheme.typography.bodySmall)

							Box{
								IconText(
									text = when(group.sortBy){ 0 -> "成绩排序"; 1 -> "学分排序"; 2 -> "修读状态"; else -> "" },
									icon = Icons.Rounded.ArrowDropDown,
									modifier = Modifier.padding(start = 8.dp).clickable { showPop = true }
								)

								DropdownMenu(expanded = showPop, onDismissRequest = { showPop = false }) {
									Box(Modifier.clickable{ showPop = false; sortBy(0) }){
										Text(text = "成绩排序", modifier = Modifier.padding(16.dp, 8.dp))
									}
									Box(Modifier.clickable{ showPop = false; sortBy(1) }){
										Text(text = "学分排序", modifier = Modifier.padding(16.dp, 8.dp))
									}
									Box(Modifier.clickable{ showPop = false; sortBy(2) }){
										Text(text = "修读状态", modifier = Modifier.padding(16.dp, 8.dp))
									}
								}
							}

							IconText(
								text = if(group.sortType == 1) "升序" else "降序",
								icon = Icons.Rounded.ArrowDropDown,
								modifier = Modifier.padding(start = 8.dp).clickable { sortType() },
								iconModifier = Modifier.rotate(if(group.sortType == 1) 0F else 180F)
							)
						}
					}
				}
			}
		}
	}

	@Composable
	fun ItemUI(lessonInfo: AcademicInfo, lessonMark: Mark, isExpanded: Boolean, onClick: () -> Unit){
		Card(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Column(
				modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 4.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {

				Row(Modifier.height(IntrinsicSize.Min)){
					Text(
						text = lessonInfo.name,
						modifier = Modifier.weight(1F),
						style = MaterialTheme.typography.titleMedium,
						textAlign = TextAlign.Start
					)
					Text(
						text = if(lessonInfo.status == 4) "成绩: ${lessonInfo.mark}" else LESSON_TYPE[lessonInfo.status],
						modifier = Modifier.fillMaxHeight(),
						textAlign = TextAlign.Center
					)
				}

			Row {
				Text(
					text = HtmlUtils.escapeHtml("${lessonInfo.category} | ${lessonInfo.content}"),
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
				)

					Spacer(modifier = Modifier.fillMaxWidth().weight(1F))

					Text(
						text = "学分: ${lessonInfo.credit}",
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText,
					)
				}

				AnimatedVisibility(visible = isExpanded){
					QueryMarkPage.MarkItems(mark = lessonMark)
				}
			}
		}
	}
}
