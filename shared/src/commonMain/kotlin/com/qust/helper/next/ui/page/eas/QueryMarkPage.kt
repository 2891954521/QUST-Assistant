package com.qust.helper.next.ui.page.eas

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.module.eas.MarkModel
import com.qust.helper.next.repository.MarkRepository
import com.qust.helper.next.ui.business.eas.EasQueryLayout
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.spinner.SpinnerWidget
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.router.params.PageParam
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.viewmodel.eas.BaseEasViewModel
import com.qust.helper.next.utils.DateUtils


@Preview
@Composable
private fun QueryMarkPreview() = AppPreview(::QueryMarkUI)

class QueryMarkPage : AppPage<QueryMarkViewModel>(title = "成绩查询", viewModelClass = QueryMarkViewModel::class) {
	@Composable
	override fun Content(viewModel: QueryMarkViewModel) = QueryMarkUI(viewModel)
}

@Composable
private fun QueryMarkUI(viewModel: QueryMarkViewModel) {
	EasQueryLayout(
		pickYear = viewModel.pickYear,
		onYearPick = {
			viewModel.pickYear = it
			viewModel.changeTerm(it)
		},
		doQuery = { viewModel.query() },
	) {
		SortBar(viewModel)

		GetMarksUI(marks = viewModel.marks) {
			viewModel.clearNew(it)
		}
	}
}

@Composable
private fun SortBar(viewModel: QueryMarkViewModel) {
	var desc by remember { mutableStateOf(false) }
	var sortBy by remember { mutableIntStateOf(0) }

	Row(Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
		Text(text = "排序方式", style = Theme.textStyles.caption)

		SpinnerWidget(
			data = listOf("考试类型", "考试成绩", "发布时间"),
			onSelect = { i, it ->
				sortBy = i
				viewModel.setSortBy(i)
			}
		){
			Row(
				modifier = Modifier.padding(8.dp).clickable { isFlyoutVisible = true },
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = when(sortBy) {
						0 -> "考试类型"
						1 -> "考试成绩"
						2 -> "发布时间"
						else -> ""
					},
					style = Theme.textStyles.caption,
					color = Theme.color.textSecondary,
					modifier = Modifier.padding(start = 8.dp)
				)
				Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = Theme.color.textSecondary)
			}
		}

		Row(
			modifier = Modifier.padding(8.dp).clickable { desc = !desc; viewModel.setSortType(if(desc) -1 else 1) },
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = if(desc) "降序" else "升序",
				style = Theme.textStyles.caption,
				color = Theme.color.textSecondary,
				modifier = Modifier.padding(start = 8.dp)
			)
			Icon(
				imageVector = Icons.Rounded.ArrowDropDown,
				modifier = Modifier.rotate(if(desc) 0F else 180F),
				contentDescription = null,
				tint = Theme.color.textSecondary,
			)
		}
	}
}

@Composable
private fun GetMarksUI(marks: List<Mark>, onClickNew: (Int) -> Unit = {}) {
	LazyColumn {
		items(marks.size) { index ->
			MarkItem(marks[index]) {
				onClickNew(index)
			}
		}
	}
}

@Composable
private fun MarkItem(mark: Mark, onClickNew: () -> Unit = {}) {
	var isExpanded by remember { mutableStateOf(false) }

	Box {
		Card(
			modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
			shape = RoundedCornerShape(8.dp),
			colors = CardDefaults.cardColors(containerColor = Theme.color.background),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {
			Column(
				modifier = Modifier.fillMaxWidth().clickable {
					isExpanded = !isExpanded
					if(mark.isNew == 1) onClickNew()
				},
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = if("正常考试" == mark.type) mark.name else "${mark.name} (${mark.type})",
					modifier = Modifier.padding(8.dp),
					style = Theme.textStyles.bodyStrong,
					color = if("正常考试" != mark.type) Theme.color.tertiary
					else if(mark.mark < 60) Theme.color.error else Color.Unspecified,
					textAlign = TextAlign.Center,
					maxLines = 2,
				)

				Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
					Text(
						text = "成绩: ${mark.mark}",
						modifier = Modifier.weight(1F),
						color = if(mark.mark < 60) Theme.color.error else Color.Unspecified,
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
					Text(
						text = "绩点: ${mark.gpa}",
						modifier = Modifier.weight(1F),
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
					Text(
						text = "学分: ${mark.credit}",
						modifier = Modifier.weight(1F),
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
				}

				AnimatedVisibility(visible = isExpanded) {
					MarkItems(mark = mark)
				}
			}
		}

		if(mark.isNew == 1) {
			Box(
				modifier = Modifier
					.align(Alignment.TopStart)
					.padding(12.dp, 4.dp)
					.background(Theme.color.primary, RoundedCornerShape(8.dp))
			) {
				Text(
					text = Strings.TEXT_NEW,
					modifier = Modifier.padding(4.dp),
					color = Theme.color.onPrimary,
					fontSize = 10.sp,
				)
			}
		}
	}
}

@Composable
private fun MarkItems(mark: Mark) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		if(mark.items.isNotEmpty()) {
			HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

			Row {
				Text(text = "项目", modifier = Modifier.weight(2F), textAlign = TextAlign.Center)
				Text(text = "成绩", modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
			}

			for(i in mark.items.indices) {
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
			style = Theme.textStyles.caption,
			color = Theme.color.textSecondary,
		)
	}
}

class QueryMarkViewModel : BaseEasViewModel() {

	val marks = mutableStateListOf<Mark>()

	private val markData: MutableList<List<Mark>?> = MutableList(Strings.ARRAY_TERM_NAME.size) { null }

	private var sortBy = 0
	private var sortType = 1

	override fun onCreate(param: PageParam) {
		super.onCreate(param)
		changeTerm(pickYear)
	}

	fun query() {
		loading("查询中") {
			val term = pickYear
			val picker = getPickTerm()
			val resultData = sort(MarkModel.queryMarks(term, picker.first, picker.second).toMutableList())

			val origData = markData[term] ?: MarkRepository.getMarksByTerm(term)

			if(origData.isEmpty()) {
				MarkRepository.insertAll(resultData)
				markData[term] = null
				loadTerm(term)
				toastSuccess("查询完成")
				return@loading
			}

			val origin = origData.toSet()
			val result = resultData.toSet()

			val new = result subtract origin
			val update = origin intersect result
			if(new.isNotEmpty() || update.isNotEmpty()) {
				MarkRepository.updateAll(update.toList())
				MarkRepository.insertAll(new.toList())
				markData[term] = null
				loadTerm(term)
				toastSuccess("新查询到 ${new.size} 门成绩")
			} else {
				toastSuccess("未查询到新成绩")
			}
		}
	}

	fun changeTerm(index: Int) {
		val data = markData[index]
		if(data == null) {
			runInBackground {
				loadTerm(index)
			}
		} else {
			marks.clear()
			marks.addAll(data)
		}
	}

	fun setSortBy(index: Int) {
		sortBy = index
		val data = markData[pickYear] ?: return
		val sorted = sort(data.toMutableList())
		markData[pickYear] = sorted
		marks.clear()
		marks.addAll(sorted)
	}

	fun setSortType(index: Int) {
		sortType = index
		val data = markData[pickYear] ?: return
		val sorted = sort(data.toMutableList())
		markData[pickYear] = sorted
		marks.clear()
		marks.addAll(sorted)
	}

	fun clearNew(index: Int) {
		val mark = marks.getOrNull(index) ?: return
		if(mark.isNew != 1) return

		marks[index] = mark.copy(isNew = 0)
		markData[pickYear] = marks.toList()
		runInBackground {
			MarkRepository.setRead(mark.id)
		}
	}

	private suspend fun loadTerm(index: Int) {
		val data = sort(MarkRepository.getMarksByTerm(index).toMutableList())
		markData[index] = data
		marks.clear()
		marks.addAll(data)
	}

	private fun sort(array: MutableList<Mark>): MutableList<Mark> {
		when(sortBy) {
			0 -> array.sortWith { a, b ->
				val v = a.type.compareTo(b.type) * sortType
				if(v == 0) a.mark.compareTo(b.mark) * sortType else v
			}

			1 -> array.sortWith { a, b -> a.mark.compareTo(b.mark) * sortType }
			2 -> array.sortWith { a, b -> a.time.compareTo(b.time) * sortType }
		}
		return array
	}
}
