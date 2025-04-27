package com.qust.helper.ui.page.eas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qust.helper.data.i18n.Strings
import com.qust.helper.ui.widget.picker.ListItemPicker


@Composable
fun BaseEasQueryUI(
	pickYear: Int = 0,
	onYearPick: (Int) -> Unit = { },
	doQuery: () -> Unit = { },
	searchBar: @Composable RowScope.() -> Unit = { },
	content: @Composable ColumnScope.() -> Unit
){
	Column(modifier = Modifier.fillMaxSize()) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {

			Text(text = Strings.TEXT_TERM)

			Spacer(Modifier.width(4.dp))

			ListItemPicker(
				value = Strings.ARRAY_TERM_NAME[pickYear],
				list = Strings.ARRAY_TERM_NAME,
				onValueChange = { i, _ -> onYearPick(i) },
				horizontalPadding = 8.dp
			)

			Spacer(Modifier.width(4.dp))

			searchBar()

			Button(modifier = Modifier.wrapContentSize().padding(8.dp), onClick = { doQuery() }) {
				Text(text = Strings.TEXT_OK, maxLines = 1)
			}
		}

		content()
	}
}