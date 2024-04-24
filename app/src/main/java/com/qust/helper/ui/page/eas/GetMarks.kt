package com.qust.helper.ui.page.eas

import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qust.helper.R
import com.qust.helper.data.Data.TermName
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.data.room.Mark
import com.qust.helper.ui.theme.colorError
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.ListPicker
import com.qust.helper.ui.widget.Texts
import com.qust.helper.ui.widget.Texts.SingleLineText
import com.qust.helper.ui.widget.Toast
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.eas.GetMarksViewModel

object GetMarks{

	val GetMarksPage = Page(Keys.Page.GetMarksPage, "成绩查询", iconRes = R.drawable.ic_school) { activity, padding, navController ->
		val viewModel by activity.viewModels<GetMarksViewModel>()
		GetMarksUI(padding, viewModel, activity.toast, navController)
	}

	@Composable
	fun GetMarksUI(padding: PaddingValues, viewModel: GetMarksViewModel, toast: Toast, navController: NavController){
		AppWidgets.CheckEasLogin(viewModel = viewModel, navController = navController)

		var pick by viewModel.pickYear
		var desc by remember { mutableStateOf(false) }
		var sortBy by remember { mutableIntStateOf(0) }
		var showPop by remember { mutableStateOf(false) }

		// 触发Compose更新
		val marks = if(viewModel.update) viewModel.marks.value else viewModel.marks.value

		Column(modifier = Modifier.fillMaxSize().padding(padding)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {

				Text(text = stringResource(id = R.string.text_term), modifier = Modifier.padding(8.dp))

				Box(modifier = Modifier.padding(horizontal = 16.dp)) {
					ListPicker.NumberPicker(
						value = pick,
						range = TermName.indices,
						label = { TermName[it] },
						onValueChange = {
							pick = it
							viewModel.selectData(it)
						},
						horizontalPadding = 8.dp
					)
				}

				Button(onClick = { viewModel.queryMarks() }) {
					Text(text = stringResource(id = R.string.text_query))
				}
			}

			Row(Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically){
				Text(text = "排序方式", style = MaterialTheme.typography.bodySmall)

				Row(
					modifier = Modifier.padding(8.dp).clickable { showPop = true },
					verticalAlignment = Alignment.CenterVertically,
				){
					Texts.SingleLineTextNoPadding(
						text = when(sortBy){ 0 -> "考试类型"; 1 -> "考试成绩"; 2 -> "发布时间"; else -> "" },
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText,
					)
					Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = colorSecondaryText)

					DropdownMenu(expanded = showPop, onDismissRequest = { showPop = false }) {
						Box(Modifier.clickable{ showPop = false; sortBy = 0; viewModel.setSortBy(0) }){
							Text(text = "考试类型", modifier = Modifier.padding(16.dp, 8.dp))
						}
						Box(Modifier.clickable{ showPop = false; sortBy = 1; viewModel.setSortBy(1) }){
							Text(text = "考试成绩", modifier = Modifier.padding(16.dp, 8.dp))
						}
						Box(Modifier.clickable{ showPop = false; sortBy = 2; viewModel.setSortBy(2) }){
							Text(text = "发布时间", modifier = Modifier.padding(16.dp, 8.dp))
						}
					}
				}

				Row(
					modifier = Modifier.padding(8.dp).clickable { desc = !desc; viewModel.setSortType(if(desc) -1 else 1) },
					verticalAlignment = Alignment.CenterVertically,
				){
					Texts.SingleLineTextNoPadding(
						text = if(desc) "降序" else "升序",
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText,
					)
					Icon(imageVector = Icons.Rounded.ArrowDropDown, modifier = Modifier.rotate(if(desc) 0F else 180F), contentDescription = null, tint = colorSecondaryText)
				}
			}

			LazyColumn { items(marks.size) { index -> ExamItem(marks[index]) { viewModel.clearNew(index) } } }
		}

		AppWidgets.DialogBar(dialogText = viewModel.dialogText)
		toast.ToastContent(viewModel.toastContent)
	}

	@Composable
	fun ExamItem(mark: Mark, onClick: () -> Unit = {}){
		var isExpanded by remember { mutableStateOf(false) }

		Box{
			Card(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
				shape = RoundedCornerShape(8.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.background,
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			) {

				Column(
					modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded; if(mark.isNew == 1) onClick() },
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text(
						text = if("正常考试" == mark.type) mark.name else "${mark.name} (${mark.type})",
						modifier = Modifier.padding(8.dp),
						style = MaterialTheme.typography.titleMedium,
						color = if("正常考试" != mark.type) MaterialTheme.colorScheme.tertiary else
							(if(mark.mark < 60) colorError else Color.Unspecified),
						textAlign = TextAlign.Center,
						maxLines = 2,
					)

					Row(
						modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp)
					) {
						Text(
							text = "成绩: ${mark.mark}",
							modifier = Modifier.padding(8.dp).weight(1F),
							color = if(mark.mark < 60) colorError else Color.Unspecified,
							textAlign = TextAlign.Center,
							maxLines = 1,
						)

						SingleLineText(text = "绩点: ${mark.gpa}", modifier = Modifier.weight(1F))
						SingleLineText(text = "学分: ${mark.credit}", modifier = Modifier.weight(1F))
					}

					AnimatedVisibility(visible = isExpanded){
						MarkItems(mark = mark){
							Text(
								text = "发布时间: ${DateUtils.YMD_HMS.format(mark.time)}",
								modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
								style = MaterialTheme.typography.bodySmall,
								color = colorSecondaryText
							)
						}
					}
				}
			}

			if(mark.isNew == 1){
				Badge(
					modifier = Modifier.align(Alignment.TopEnd).padding(12.dp, 4.dp),
					containerColor = MaterialTheme.colorScheme.tertiaryContainer
				){
					Text(text = "new")
				}
			}
		}
	}

	@Composable
	fun MarkItems(mark: Mark, content: @Composable () -> Unit = { }) {
		Column(
			modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp, 0.dp, 0.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

			Row {
				SingleLineText(text = "项目", modifier = Modifier.weight(2F))
				SingleLineText(text = "成绩", modifier = Modifier.weight(1F))
			}

			for(i in 0 until mark.items.size){
				HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
				Row {
					SingleLineText(text = mark.items[i].name, modifier = Modifier.weight(2F))
					SingleLineText(text = mark.items[i].mark, modifier = Modifier.weight(1F))
				}
			}
			content()
		}
	}
}