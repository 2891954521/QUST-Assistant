package com.qust.helper.next.ui.business.eas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.next.entity.eas.Mark
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.utils.DateUtils

/**
 * 成绩详情UI
 */
@Composable
fun MarkDetailsUI(mark: Mark) {
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