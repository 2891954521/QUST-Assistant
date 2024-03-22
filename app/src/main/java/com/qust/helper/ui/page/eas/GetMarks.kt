package com.qust.helper.ui.page.eas

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qust.helper.data.eas.Mark
import com.qust.helper.ui.common.Texts.SingleLineText
import com.qust.helper.viewmodel.eas.GetMarksViewModel

object GetMarks{

	@Composable
	fun GetMarks(activity: ComponentActivity) {
		val viewModel: GetMarksViewModel by activity.viewModels()

		val marks by viewModel.marks

		EasQuery(
			viewModel = viewModel,
			items = marks,
			itemView = { ExamItem(it) },
			onYearPick = {
				viewModel.marks.value = viewModel.marksData[it]
			}
		){
			viewModel.queryMarks { }
		}
	}

	@Composable
	fun ExamItem(mark: Mark){
		var isExpanded by remember { mutableStateOf(false) }

		Card(
			modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 8.dp),
			shape = RoundedCornerShape(8.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.background,
			),
			elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		) {

			Column(
				modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
				horizontalAlignment = Alignment.CenterHorizontally,
			) {

				SingleLineText(
					text = mark.name,
					style = MaterialTheme.typography.titleMedium,
				)

				Row(
					modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp)
				) {
					SingleLineText(text = "成绩: ", modifier = Modifier.weight(1F))
					SingleLineText(text = mark.mark.toString(), modifier = Modifier.weight(1F))
					SingleLineText(text = "绩点", modifier = Modifier.weight(1F))
					SingleLineText(text = mark.gpa, modifier = Modifier.weight(1F))
					SingleLineText(text = "学分", modifier = Modifier.weight(1F))
					SingleLineText(text = mark.credit, modifier = Modifier.weight(1F))
				}

				AnimatedVisibility(visible = isExpanded){
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
								SingleLineText(text = mark.items[i], modifier = Modifier.weight(2F))
								SingleLineText(text = mark.itemMarks[i], modifier = Modifier.weight(1F))
							}
						}
					}
				}
			}
		}
	}
}