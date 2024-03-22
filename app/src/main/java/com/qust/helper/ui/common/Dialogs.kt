package com.qust.helper.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

object Dialogs {

	@Composable
	fun IndeterminateProgressDialog(
		dialogText: String,
		onDismissRequest: () -> Unit = { }
	) {
		Dialog(
			onDismissRequest = { onDismissRequest() }
		){
			Card(
				modifier = Modifier.fillMaxWidth().padding(32.dp, 0.dp),
				shape = RoundedCornerShape(16.dp),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			) {

				Row(
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(16.dp)
				) {

					CircularProgressIndicator(
						color = MaterialTheme.colorScheme.secondary,
						trackColor = MaterialTheme.colorScheme.surfaceVariant,
					)
					Text(
						text = dialogText,
						modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
					)
				}
			}
		}
	}
}

@Preview(showBackground = false)
@Composable
fun IndeterminateProgressDialogPreview(){
	Dialogs.IndeterminateProgressDialog(
		dialogText = "加载中"
	)
}