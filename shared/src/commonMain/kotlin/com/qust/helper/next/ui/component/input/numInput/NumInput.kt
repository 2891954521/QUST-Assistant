package com.qust.helper.next.ui.component.input.numInput

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.next.App
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.theme.Theme


@Preview
@Composable
private fun NumberInputPreview() {
	val uiState = remember {
		NumInputUIState(defaultContent = "") { result ->
			// 点击确定后触发
		}
	}
	Column(Modifier.background(Color.White)) {
		Text(text = uiState.content, color = Color.Black)
		NumberInput(uiState)
	}
}


@Preview
@Composable
private fun NumberInputPopPreview() {
	var content by remember { mutableStateOf("123456") }

	val uiState = remember {
		PopNumInputUIState(defaultContent = content) { result ->
			content = result
		}
	}

	Column(Modifier.background(Color.White).size(100.dp)) {
		Text(text = content, color = Color.Black, modifier = Modifier.clickable {
			uiState.setValue(content)
			uiState.show()
		})
	}

	NumberInputPop(uiState)
}


/**
 * 数字输入软键盘，处于弹出层
 *
 * 用法：
 *  - 初始化一个 NumberInputUIState，可以在构造函数里传入初始值
 *  - NumberInputUIState 的 content 为实际的内容，可以用于别处展示
 *  - 可以通过 onUpdate，onDone 监听输入
 *  - 可以通过 NumberInputUIState 的 setData 从外部更新内容
 */
@Composable
fun NumberInputPop(uiState: PopNumInputUIState, expanded: Boolean = uiState.isShowPop){
	DropdownMenu(modifier = Modifier.background(Theme.color.primaryContainer), expanded = expanded, onDismissRequest = uiState::hide) {
		CompositionLocalProvider(LocalDensity provides App.scale.getDensity()) {
			NumberInput(uiState)
		}
	}
}

/**
 * 数字输入软键盘
 * @see NumberInputPop
 */
@Composable
fun NumberInput(uiState: NumInputUIState) {
	NumberInputPad(uiState.enableDot, uiState.enableNegative, {
		val s = uiState.append(it)
		if(s != null){
			uiState.content = s
			uiState.onUpdate(s)
		}
	}, {
		uiState.clickDone()
	})
}


@Composable
private fun NumberInputPad(hasDot: Boolean, hasNegative: Boolean, onInput: (Char) -> Unit, onDone: () -> Unit) {
	Row(Modifier.padding(2.dp)) {
		Column {
			Row {
				Column {
					ItemButton("1", '1', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("4", '4', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("7", '7', colors = ContainerColors.Primary, onClick = onInput)
				}
				Column {
					ItemButton("2", '2', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("5", '5', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("8", '8', colors = ContainerColors.Primary, onClick = onInput)
				}
				Column {
					ItemButton("3", '3', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("6", '6', colors = ContainerColors.Primary, onClick = onInput)
					ItemButton("9", '9', colors = ContainerColors.Primary, onClick = onInput)
				}
			}
			Row {
				if(hasNegative) ItemButton("±", '-', colors = ContainerColors.Primary, onClick = onInput)
				ItemButton("0", '0', when { hasNegative && hasDot -> 240.dp; hasNegative || hasDot -> 160.dp; else -> 80.dp }, colors = ContainerColors.Primary, onClick = onInput)
				if(hasDot) ItemButton(".", '.', colors = ContainerColors.Primary, onClick = onInput)
			}
		}

		Column {
			ItemButton("退\n格", '\b', 80.dp, 160.dp, colors = ContainerColors.Danger, onClick = onInput)
			ItemButton("确\n定", '\r', 80.dp, 160.dp, colors = ContainerColors.Secondary, onClick = { onDone() })
		}
	}
}


@Composable
private fun ItemButton(text: String, key: Char, width: Dp = 80.dp, height: Dp = 80.dp, colors: ContainerColors, onClick: (Char) -> Unit) {
	Box(Modifier
		.size(width, height)
		.padding(2.dp)
		.clip(RoundedCornerShape(8.dp))
		.background(colors.background, RoundedCornerShape(8.dp))
		.clickable { onClick(key) }
	) {
		Text(text, modifier = Modifier.align(Alignment.Center), color = colors.content)
	}
}