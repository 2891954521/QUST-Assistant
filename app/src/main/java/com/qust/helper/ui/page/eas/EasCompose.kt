package com.qust.helper.ui.page.eas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.data.Data.TermName
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.widget.ListPicker
import com.qust.helper.viewmodel.eas.BaseEasViewModel

@Composable
fun <T> EasQuery(
	viewModel: BaseEasViewModel,
	items: Array<T>,
	termName: Array<String> = TermName,
	itemView: @Composable (T) -> Unit,
	onYearPick: (Int) -> Unit = { },
	doQuery: () -> Unit,
) {
	var pickYear by viewModel.pickYear

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {

			Text(
				modifier = Modifier.padding(8.dp),
				text = stringResource(id = R.string.text_term)
			)

			Box(modifier = Modifier.padding(16.dp, 0.dp)){
				ListPicker.NumberPicker(
					value = pickYear,
					range = termName.indices,
					label = { termName[it] },
					onValueChange = {
						pickYear = it
						onYearPick(it)
					},
					horizontalPadding = 8.dp
				)
			}

			Button(onClick = { doQuery() }) {
				Text(text = stringResource(id = R.string.text_query))
			}
		}

		LazyColumn {
			items(items.size) { index ->
				itemView(items[index])
			}
		}
	}

	AppBar.DialogAndToast(dialogText = viewModel.dialogText, toastContent = viewModel.toastContent)
}