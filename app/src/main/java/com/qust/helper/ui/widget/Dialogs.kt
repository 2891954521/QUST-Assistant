package com.qust.helper.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qust.helper.R
import java.util.Calendar
import java.util.Date

object Dialogs {

	@Composable
	fun AskDialog(
		title: String = "",
		content: String = "",
		onDismiss: () -> Unit = { },
		onConfirm: () -> Unit = { }
	){
		AlertDialog(
			onDismissRequest = { onDismiss() },
			title = { Text(text = title) },
			text = { Text(content) },
			confirmButton = {
				TextButton(onClick = { onConfirm() }) {
					Text(stringResource(id = R.string.text_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { onDismiss() }) {
					Text(stringResource(id = R.string.text_cancel))
				}
			}
		)
	}

	@Composable
	fun InputDialog(
		title: String = "",
		content: String = "",
		keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
		onDismiss: () -> Unit = { },
		onConfirm: (String) -> Unit = { }
	){
		var input by remember { mutableStateOf(content) }
		AlertDialog(
			onDismissRequest = { onDismiss() },
			title = { Text(text = title) },
			text = {
				OutlinedTextField(
					value = input,
					onValueChange = { input = it },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
					keyboardOptions = keyboardOptions,
					maxLines = 1
				)
			},
			confirmButton = {
				TextButton(onClick = { onConfirm(input) }) {
					Text(stringResource(id = R.string.text_ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { onDismiss() }) {
					Text(stringResource(id = R.string.text_cancel))
				}
			}
		)
	}

	@Composable
	fun IndeterminateProgressDialog(
		dialogText: String = "Loading",
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

	@Composable
	fun ListDialog(
		title: String = "Choose",
		items: Array<String> = emptyArray(),
		onDismiss: () -> Unit = { },
		onItemChoose: (Array<String>, Int) -> Unit = { _, _ -> },
	) {
		Dialog(onDismissRequest = { onDismiss() }){
			Card(
				modifier = Modifier.fillMaxWidth().padding(32.dp, 0.dp),
				shape = RoundedCornerShape(16.dp),
				elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Texts.SingleLineTextNoPadding(
						text = title,
						style = MaterialTheme.typography.titleLarge,
						modifier = Modifier.fillMaxWidth()
					)
					LazyColumn(Modifier.wrapContentSize().padding(horizontal= 16.dp)){
						items(items.size){
							Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onItemChoose(items, it); onDismiss() }){
								Texts.SingleLineTextNoPadding(
									text = items[it],
									textAlign = TextAlign.Start,
									modifier = Modifier.fillMaxWidth().padding(8.dp)
								)
							}
						}
					}
					Box(modifier = Modifier.fillMaxWidth()){
						TextButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = { onDismiss() }) {
							Text(text = stringResource(id = R.string.text_cancel))
						}
					}
				}
			}
		}
	}


	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun DatePickerDialog(
		currentDate: Date,
		onDismissRequest: () -> Unit,
		onDateSelected: (Date) -> Unit,
	) {
		Dialog(onDismissRequest = { onDismissRequest() }, properties = DialogProperties()) {
			Column(
				modifier = Modifier.wrapContentSize().background(
					color = MaterialTheme.colorScheme.surface,
					shape = RoundedCornerShape(size = 16.dp)
				)
			) {

				val datePickerState = rememberDatePickerState(
					initialSelectedDateMillis = null,
					selectableDates = object : SelectableDates {
						val calendar = Calendar.getInstance()
						val currentYear = calendar[Calendar.YEAR]
						override fun isSelectableDate(utcTimeMillis: Long): Boolean {
							calendar.timeInMillis = utcTimeMillis
							return calendar[Calendar.DAY_OF_WEEK] == Calendar.MONDAY
						}
						override fun isSelectableYear(year: Int): Boolean {
							return year <= currentYear
						}
					}
				)

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					DatePicker(state = datePickerState)
				}

				Spacer(modifier = Modifier.size(8.dp))

				Row(modifier = Modifier.align(Alignment.End).padding(bottom = 16.dp, end = 16.dp)) {
					TextButton(
						enabled = datePickerState.selectedDateMillis != null,
						onClick = { onDateSelected(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())) }) {
						Text(stringResource(id = R.string.text_ok))
					}

					TextButton(onClick = { onDismissRequest() }) {
						Text(stringResource(id = R.string.text_cancel))
					}
				}
			}
		}
	}
}

interface DialogAble {
	val _dialogText: MutableState<String>
	val dialogText: String
	fun showDialog(message: String)
	fun clearDialog()
}

class DialogAbleImpl(override val _dialogText: MutableState<String> = mutableStateOf("")): DialogAble{
	override val dialogText: String
		get() = _dialogText.value

	override fun showDialog(message: String) {
		_dialogText.value = message
	}
	override fun clearDialog() {
		_dialogText.value = ""
	}
}