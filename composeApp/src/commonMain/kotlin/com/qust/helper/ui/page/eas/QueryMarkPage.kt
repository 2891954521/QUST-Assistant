package com.qust.helper.ui.page.eas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.eas.Mark
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.School
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.LocalColor
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.utils.DateUtils
import com.qust.helper.viewmodel.eas.QueryMarkViewModel

object QueryMarkPage: BasePage<QueryMarkViewModel>("成绩查询", Drawables.School) {

	@Composable
	override fun getViewModel() = viewModel<QueryMarkViewModel>()

	@Composable
	override fun Content(viewModel: QueryMarkViewModel) {

		BaseEasQueryUI(
			pickYear = viewModel.pickYear.value,
			onYearPick = {
				viewModel.pickYear.value = it
				viewModel.selectData(it)
			 },
			doQuery = { viewModel.query() },
		){

			SortBar(viewModel)

			LazyColumn {
				items(viewModel.marks.size) { index ->
					MarkItem(viewModel.marks[index]) {
						viewModel.clearNew(index)
					}
				}
			}
		}
	}

	@Composable
	private fun SortBar(viewModel: QueryMarkViewModel){
		var desc by remember { mutableStateOf(false) }
		var sortBy by remember { mutableIntStateOf(0) }
		var showPop by remember { mutableStateOf(false) }

		Row(Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
			Text(text = "排序方式", style = MaterialTheme.typography.bodySmall)

			Row(
				modifier = Modifier.padding(8.dp).clickable { showPop = true },
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = when(sortBy) {
						0 -> "考试类型"; 1 -> "考试成绩"; 2 -> "发布时间"; else -> ""
					},
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
				)
				Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = colorSecondaryText)

				DropdownMenu(expanded = showPop, onDismissRequest = { showPop = false }) {
					Box(Modifier.clickable { showPop = false; sortBy = 0; viewModel.setSortBy(0) }) {
						Text(text = "考试类型", modifier = Modifier.padding(16.dp, 8.dp))
					}
					Box(Modifier.clickable { showPop = false; sortBy = 1; viewModel.setSortBy(1) }) {
						Text(text = "考试成绩", modifier = Modifier.padding(16.dp, 8.dp))
					}
					Box(Modifier.clickable { showPop = false; sortBy = 2; viewModel.setSortBy(2) }) {
						Text(text = "发布时间", modifier = Modifier.padding(16.dp, 8.dp))
					}
				}
			}

			Row(
				modifier = Modifier.padding(8.dp).clickable { desc = !desc; viewModel.setSortType(if(desc) -1 else 1) },
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = if(desc) "降序" else "升序",
					style = MaterialTheme.typography.bodySmall,
					color = colorSecondaryText,
				)
				Icon(imageVector = Icons.Rounded.ArrowDropDown, modifier = Modifier.rotate(if(desc) 0F else 180F), contentDescription = null, tint = colorSecondaryText)
			}
		}
	}

	@Composable
	private fun MarkItem(mark: Mark, onClickNew: () -> Unit = {}) {
		var isExpanded by remember { mutableStateOf(false) }

		Box {
			Card(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
				shape = RoundedCornerShape(8.dp),
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.background,
				),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			) {

				Column(
					modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded; if(mark.isNew == 1) onClickNew() },
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text(
						text = if("正常考试" == mark.type) mark.name else "${mark.name} (${mark.type})",
						modifier = Modifier.padding(8.dp),
						style = MaterialTheme.typography.titleMedium,
						color = if("正常考试" != mark.type) MaterialTheme.colorScheme.tertiary else
							(if(mark.mark < 60) LocalColor.current.error else Color.Unspecified),
						textAlign = TextAlign.Center,
						maxLines = 2,
					)

					Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
						Text(
							text = "成绩: ${mark.mark}",
							modifier = Modifier.weight(1F),
							color = if(mark.mark < 60) LocalColor.current.error else Color.Unspecified,
							textAlign = TextAlign.Center,
							maxLines = 1,
						)
						Text(
							text = "绩点: ${mark.gpa}",
							modifier = Modifier.weight(1F),
							textAlign = TextAlign.Center,
							maxLines = 1
						)
						Text(
							text = "学分: ${mark.credit}",
							modifier = Modifier.weight(1F),
							textAlign = TextAlign.Center,
							maxLines = 1
						)
					}

					AnimatedVisibility(visible = isExpanded) {
						MarkItems(mark = mark)
					}
				}
			}

			if(mark.isNew == 1) {
				Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp, 4.dp).background(LocalColor.current.primary, RoundedCornerShape(8.dp))) {
					Text(text = Strings.TEXT_NEW, modifier = Modifier.padding(4.dp), color = LocalColor.current.onPrimary, fontSize = 10.sp)
				}
			}
		}
	}

	@Composable
	private fun MarkItems(mark: Mark) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			if(mark.items.isNotEmpty()){
				HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

				Row {
					Text(text = "项目", modifier = Modifier.weight(2F), textAlign = TextAlign.Center)
					Text(text = "成绩", modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
				}

				for(i in 0 until mark.items.size) {
					HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
					Row {
						Text(text = mark.items[i].name, modifier = Modifier.weight(2F), textAlign = TextAlign.Center)
						Text(text = mark.items[i].mark, modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
					}
				}

				HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
			}

			Text(
				text = "发布时间: ${DateUtils.YMD_HMS.format(mark.time)}",
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
				style = MaterialTheme.typography.bodySmall,
				color = colorSecondaryText
			)
		}
	}
}