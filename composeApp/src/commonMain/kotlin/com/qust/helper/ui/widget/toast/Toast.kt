package com.qust.helper.ui.widget.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qust.helper.ui.theme.LocalColor

data class ToastData(
	val message: String,
	val type: Type,
	val icon: ImageVector? = null
){
	enum class Type {
		NORMAL,
		SUCCESS,
		INFO,
		WARNING,
		ERROR,
	}
}


data class ColorData(
	val backgroundColor: Color,
	val textColor: Color,
	val iconColor: Color,
	val icon: ImageVector,
)


@Composable
fun ToastUI(hostState: ToastUIState, modifier: Modifier, toast: @Composable (ToastData) -> Unit = { Toast(it) }) {
	var data: ToastData by remember { mutableStateOf(ToastData("", ToastData.Type.NORMAL)) }

	val currentData = hostState.currentData

	if (currentData != null) {
		data = currentData
		LaunchedEffect(data) {
			hostState.run()
		}
	}

	AnimatedVisibility(
		modifier = modifier,
		visible = currentData != null,
		enter = fadeIn(animationSpec = tween(durationMillis = 500), initialAlpha = 0.1f),
		exit = fadeOut(animationSpec = tween(durationMillis = 500), targetAlpha = 0.1f)
	) {
		toast(data)
	}
}


@Composable
fun Toast(toastData: ToastData) {

	val colorData = when (toastData.type) {
		ToastData.Type.NORMAL -> ColorData(
			backgroundColor = LocalColor.current.background,
			textColor = LocalColor.current.onBackground,
			iconColor = LocalColor.current.onBackground,
			icon = Icons.Rounded.Notifications
		)

		ToastData.Type.SUCCESS -> ColorData(
			backgroundColor = LocalColor.current.background,
			textColor = LocalColor.current.onBackground,
			iconColor = LocalColor.current.onBackground,
			icon = Icons.Rounded.Check
		)

		ToastData.Type.INFO -> ColorData(
			backgroundColor = LocalColor.current.background,
			textColor = LocalColor.current.onBackground,
			iconColor = LocalColor.current.onBackground,
			icon = Icons.Rounded.Info
		)

		ToastData.Type.WARNING -> ColorData(
			backgroundColor = LocalColor.current.background,
			textColor = LocalColor.current.onBackground,
			iconColor = LocalColor.current.onBackground,
			icon = Icons.Rounded.Warning
		)

		ToastData.Type.ERROR -> ColorData(
			backgroundColor = LocalColor.current.background,
			textColor = LocalColor.current.onBackground,
			iconColor = LocalColor.current.onBackground,
			icon = Icons.Rounded.Warning
		)
	}

	val icon = toastData.icon ?: colorData.icon

	key(toastData) {
		Toast(
			message = toastData.message,
			icon = icon,
			backgroundColor = colorData.backgroundColor,
			iconColor = colorData.iconColor,
			textColor = colorData.textColor,
		)
	}
}


@Composable
private fun Toast(
	message: String,
	icon: ImageVector?,
	backgroundColor: Color,
	iconColor: Color,
	textColor: Color,
) {
	val roundedValue = 26.dp
	Surface(
		modifier = Modifier.defaultMinSize(minWidth = 100.dp),
		color = backgroundColor,
		shape = RoundedCornerShape(roundedValue),
		tonalElevation = 2.dp,
	) {
		Column(
			modifier = Modifier.padding(32.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			if (icon != null) {
				Icon(icon, contentDescription = null, Modifier.size(64.dp), tint = iconColor)
			}
			Text(message, modifier = Modifier.padding(8.dp), color = textColor, fontSize = 24.sp)
		}
	}
}