package com.qust.helper.next.ui.component.input

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.spinner.SpinnerWidget
import com.qust.helper.next.ui.theme.Theme
import io.github.composefluent.component.SplitButton

/**
 * 时:分 时间输入
 *
 * @param hour 小时文本（两位数，可手动输入）
 * @param minute 分钟文本（两位数，可手动输入）
 * @param items 下拉候选，元素形如 "08:00"；选中后同时填充 [hour] 与 [minute]
 */
@Composable
fun TimePick(
	modifier: Modifier = Modifier,
	hour: MutableState<String>,
	minute: MutableState<String>,
	items: List<String>,
){
	SpinnerWidget(
		data = items,
		modifier = modifier,
		onSelect = { i, it ->
			val sp = it.split(":")
			if(sp.size == 2){
				hour.value = sp[0]
				minute.value = sp[1]
			}
	    },
	){
		SplitButton(flyoutClick = { isFlyoutVisible = true }, onClick = { }) {
			BasicTextField(
				value = hour.value,
				onValueChange = {
					hour.value = when(it.length){
						0 -> ""
						1 -> if(!it.startsWith('0')) "0$it" else it
						else -> it.substring(it.length - 2, it.length)
					}
				},
				modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp).width(20.dp),
				textStyle = Theme.textStyles.body.copy(textAlign = TextAlign.End),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)

			Text(text = ":")

			BasicTextField(
				value = minute.value,
				onValueChange = {
					minute.value = when(it.length){
						0 -> ""
						1 -> if(!it.startsWith('0')) "0$it" else it
						else -> it.substring(it.length - 2, it.length)
					}
				},
				modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp).width(20.dp),
				textStyle = Theme.textStyles.body.copy(textAlign = TextAlign.Start),
				keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
				maxLines = 1,
			)
		}
	}
}
