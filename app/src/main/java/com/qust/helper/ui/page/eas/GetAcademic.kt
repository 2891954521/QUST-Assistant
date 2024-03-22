package com.qust.helper.ui.page.eas

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.eas.Academic
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.common.Texts
import com.qust.helper.ui.common.toastOK
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.TripleProgressBar
import com.qust.helper.viewmodel.eas.GetAcademicViewModel

object GetAcademic {


	@Composable
	fun GetAcademic(activity: ComponentActivity){
		val viewModel: GetAcademicViewModel by activity.viewModels()
		GetAcademicUI(
			index = viewModel.choose,
			groups = viewModel.lessonGroups.value,
			lessons = viewModel.lessonInfo, {
				viewModel.queryData { viewModel.toastContent.value = toastOK("查询完成") }
			}, {
				viewModel.changeGroupMode()
			}, sortByMark = {
				viewModel.sortByMark(it)
			}, sortByCredit = {
				viewModel.sortByCredit(it)
			}, sortByStatus = {

			}
		)
		AppBar.DialogAndToast(dialogText = viewModel.dialogText, toastContent = viewModel.toastContent)
	}

	@Composable
	@OptIn(ExperimentalFoundationApi::class)
	fun GetAcademicUI(
		index: Int = -1,
		groups: Array<Academic.LessonInfoGroup>,
		lessons: Array<Academic.LessonInfo>,
		queryData: () -> Unit = { },
		changeGroup: () -> Unit = { },
		sortByMark: (Int) -> Unit = { },
		sortByCredit: (Int) -> Unit = { },
		sortByStatus: (Int) -> Unit = { }
	) {
		val groupExpandState = remember(groups) {
			lessons.mapIndexed { i, _ -> i == index }.toMutableStateList()
		}
		val expandState = remember(lessons) { lessons.map { false }.toMutableStateList() }

		Column {
			Row {
				TextButton(onClick = { queryData() }) {
					Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
					Text(text = "刷新")
				}
				TextButton(onClick = { changeGroup() }) {
					Icon(painter = painterResource(id = R.drawable.ic_view_list), contentDescription = null)
					Text(text = "切换视图")
				}
			}

			LazyColumn {
				groups.forEachIndexed { i, dataItem ->
					val isExpand = groupExpandState[i]
					stickyHeader {
						GroupUI(dataItem, isExpand,
							onClick = { groupExpandState[i] = !isExpand },
							sortByMark = { sortByMark(i) },
							sortByCredit = { sortByCredit(i) },
							sortByStatus = { sortByStatus(i) }
						)
					}
					if(isExpand) {
						items(dataItem.lessonIndex.size) { row ->
							val index1 =  dataItem.lessonIndex[row]
							ItemUI(lessonInfo = lessons[index1], expandState[index1]){ expandState[index1] = !expandState[index1]}
						}
					}
				}
			}
		}

	}


	@Composable
	fun GroupUI(
		lessonGroup: Academic.LessonInfoGroup,
		isExpand: Boolean = false,
		onClick: () -> Unit = { },
		sortByMark: () -> Unit = { },
		sortByCredit: () -> Unit = { },
		sortByStatus: () -> Unit = { }
	){
		var showPop by remember { mutableStateOf(false) }

		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Box(Modifier.clickable { onClick() }) {
				Column(Modifier.padding(8.dp)) {
					Box(Modifier.fillMaxWidth()) {
						Texts.SingleLineTextNoPadding(
							text = lessonGroup.groupName,
							style = MaterialTheme.typography.titleMedium,
							modifier = Modifier.align(Alignment.Center)
						)
						Texts.SingleLineTextNoPadding(
							text = "共 ${lessonGroup.lessonIndex.size} 门，通过 ${lessonGroup.passedCounts} 门",
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.align(Alignment.CenterStart)
						)
						Texts.SingleLineTextNoPadding(
							text = "学分: ${lessonGroup.obtainedCredits} / ${lessonGroup.requireCredits}",
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.align(Alignment.CenterEnd)
						)
					}

					Spacer(modifier = Modifier.height(8.dp))

					TripleProgressBar.TripleProgressBar(
						redValue = ((lessonGroup.obtainedCredits + lessonGroup.creditNotEarned) / lessonGroup.requireCredits).coerceAtMost(1F),
						greenValue = (lessonGroup.obtainedCredits / lessonGroup.requireCredits).coerceAtMost(1F),
						height = 5.dp
					)

					if(isExpand){
						Spacer(modifier = Modifier.height(8.dp))
						Row(Modifier.padding(8.dp, 0.dp)){
							Row(
								modifier = Modifier.clickable { showPop = true },
								verticalAlignment = Alignment.CenterVertically,
							){
								Texts.SingleLineTextNoPadding(
									text = "排序方式",
									style = MaterialTheme.typography.bodySmall,
									color = colorSecondaryText,
								)
								Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = colorSecondaryText)
							}

							DropdownMenu(expanded = showPop, onDismissRequest = { showPop = false }) {
								Box(Modifier.clickable{ showPop = false; sortByMark() }){
									Text(text = "成绩降序", modifier = Modifier.padding(16.dp, 8.dp))
								}
								Box(Modifier.clickable{ showPop = false; sortByCredit() }){
									Text(text = "学分降序", modifier = Modifier.padding(16.dp, 8.dp))
								}
								Box(Modifier.clickable{ showPop = false; sortByStatus() }){
									Text(text = "修读状态", modifier = Modifier.padding(16.dp, 8.dp))
								}
							}
						}
					}
				}
			}
		}
	}

	@Composable
	fun ItemUI(lessonInfo: Academic.LessonInfo, isExpanded: Boolean, onClick: () -> Unit){
		Card(
			modifier = Modifier.fillMaxWidth().padding(32.dp, 8.dp, 32.dp, 8.dp),
			colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Column(
				modifier = Modifier.fillMaxWidth().clickable { onClick() },
			) {

				Row(Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp).height(IntrinsicSize.Min)){
					Text(
						text = lessonInfo.name,
						modifier = Modifier.weight(1F),
						style = MaterialTheme.typography.titleMedium,
						textAlign = TextAlign.Start
					)
					Texts.SingleLineTextNoPadding(
						text = if(lessonInfo.status == 4) "成绩: ${lessonInfo.mark}"  else Academic.LESSON_TYPE[lessonInfo.status],
						modifier = Modifier.fillMaxHeight(),
						textAlign = TextAlign.Center
					)
				}

				Box(modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 8.dp)) {
					Texts.SingleLineTextNoPadding(
						text = lessonInfo.content,
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText,
					)
					Texts.SingleLineTextNoPadding(
						text = "学分: ${lessonInfo.credit}",
						modifier = Modifier.align(Alignment.CenterEnd),
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText,
					)
				}

				AnimatedVisibility(visible = isExpanded){
					Row(modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp, 8.dp, 8.dp)) {
						Texts.SingleLineTextNoPadding(text = "成绩: ${lessonInfo.mark}", modifier = Modifier.weight(1F))
						Texts.SingleLineTextNoPadding(text = "绩点: ${lessonInfo.gpa}", modifier = Modifier.weight(1F))
					}
				}
			}
		}
	}
}