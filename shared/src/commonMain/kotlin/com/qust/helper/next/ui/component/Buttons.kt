package com.qust.helper.next.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.utils.DateUtils
import com.qust.helper.next.utils.DateUtils.to_YMD_HMS
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.theme.Theme


@Preview(device = "spec:width=720dp,height=480dp,dpi=160")
@Composable
private fun ButtonPreview() {
	Column(Modifier.background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
		Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
			PrimaryButton(text = "小按钮", type = ButtonType.Small) { }

			PrimaryButton(text = "中按钮") { }

			PrimaryButton(text = "大按钮", type = ButtonType.Large) { }
		}

		Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
			PrimaryButton(text = "小按钮", type = ButtonType.Small, colors = ContainerColors.Secondary) { }

			PrimaryButton(text = "中按钮", colors = ContainerColors.Secondary) { }

			PrimaryButton(text = "大按钮", type = ButtonType.Large, colors = ContainerColors.Secondary) { }
		}

		Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
			var content by remember { mutableStateOf("内容") }

			SafeClickButton(text = "安全按钮") {
				// 短时间多次点击只会执行一次
				content = DateUtils.currentTime().to_YMD_HMS()
			}

			Text(content)
		}

		Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
			var content by remember { mutableStateOf("内容") }

			PrimaryButton(text = "支持长按", onClick = {
					content = "点击"
			}, longClick = {
					content = "长按"
				})

			Text(content)
		}
	}
}


@Composable
fun PrimaryButton(
	modifier: Modifier = Modifier,
	text: String,
	enabled: Boolean = true,
	type: ButtonType = ButtonType.Normal,
	colors: ContainerColors = ContainerColors.Primary,
	onClick: () -> Unit
) {
	val backgroundColor = if (enabled) colors.background else colors.background.copy(alpha = 0.5f)
	val textColor = if (enabled) colors.content else colors.content.copy(alpha = 0.5f)

	Box(modifier = modifier
		.background(backgroundColor, RoundedCornerShape(5.dp))
		.clip(RoundedCornerShape(5.dp))
		.clickable(enabled = enabled, onClick = onClick)
	) {
		Text(
			text = text,
			style = type.textStyle,
			modifier = Modifier.align(Alignment.Center).padding(type.innerPadding),
			color = textColor
		)
	}
}

@Composable
fun PrimaryButton(
	modifier: Modifier = Modifier,
	text: String,
	enabled: Boolean = true,
	type: ButtonType = ButtonType.Normal,
	colors: ContainerColors = ContainerColors.Primary,
	longClick: () -> Unit,
	onClick: () -> Unit = { }
) {
	val backgroundColor = if (enabled) colors.background else colors.background.copy(alpha = 0.5f)
	val textColor = if (enabled) colors.content else colors.content.copy(alpha = 0.5f)

	Box(modifier = modifier
		.background(backgroundColor, RoundedCornerShape(5.dp))
		.clip(RoundedCornerShape(5.dp))
		.combinedClickable(enabled = enabled, onClick = onClick, onLongClick = longClick)
	) {
		Text(
			text = text,
			style = type.textStyle,
			modifier = Modifier.align(Alignment.Center).padding(type.innerPadding),
			color = textColor
		)
	}
}


/**
 * 安全的Button，短时间内只能触发一次点击
 */
@Composable
fun SafeClickButton(
	modifier: Modifier = Modifier,
	text: String,
	enabled: Boolean = true,
	type: ButtonType = ButtonType.Normal,
	colors: ContainerColors = ContainerColors.Primary,
	delay: Int = 1000,
	onClick: () -> Unit
) {
	var time by remember { mutableLongStateOf(0L) }

	PrimaryButton(
		modifier = modifier,
		text = text,
		enabled = enabled,
		type = type,
		colors = colors,
		onClick = {
			if (System.currentTimeMillis() - time > delay) {
				time = System.currentTimeMillis()
				onClick()
			}
		}
	)
}

data class ButtonType(
	val textStyle: TextStyle,
	val innerPadding: PaddingValues,
) {
	companion object {

		val Small: ButtonType
			@Composable
			get() = ButtonType(
				textStyle = Theme.textStyles.bodySmall,
				innerPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
			)

		val Normal: ButtonType
			@Composable
			get() = ButtonType(
				textStyle = Theme.textStyles.bodyMedium,
				innerPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
			)

		val Large: ButtonType
			@Composable
			get() = ButtonType(
				textStyle = Theme.textStyles.bodyLarge,
				innerPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
			)
	}
}
