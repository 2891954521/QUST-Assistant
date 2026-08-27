package com.qust.helper.next.ui.component.overlay.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
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
import com.qust.helper.next.ui.component.overlay.OverlayController
import com.qust.helper.next.ui.theme.Theme


@Composable
fun ToastHost(
    controller: OverlayController,
    modifier: Modifier = Modifier
) {
    var data: ToastItem by remember { mutableStateOf(ToastItem(message = "", type = ToastType.NORMAL)) }

    val currentData = controller.toastData

    if (currentData != null) {
        data = currentData
        LaunchedEffect(data) {
            controller.onShowToast(data)
        }
    }
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = currentData != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(durationMillis = 500), initialAlpha = 0.1f),
            exit = fadeOut(animationSpec = tween(durationMillis = 500), targetAlpha = 0.1f)
        ) {
            Toast(data)
        }
    }

}


@Composable
fun Toast(toastData: ToastItem) {
    val icon = toastData.icon ?: when (toastData.type) {
        ToastType.NORMAL -> Icons.Default.Notifications
        ToastType.SUCCESS -> Icons.Default.Check
        ToastType.INFO -> Icons.Default.Info
        ToastType.WARNING -> Icons.Default.Warning
        ToastType.ERROR -> Icons.Default.Warning
    }

    key(toastData) {
        Toast(
            message = toastData.message,
            icon = icon,
            backgroundColor = Color(0x9A000000),
            iconColor = Color(0xFFFFFFFF),
            textColor = Color(0xFFCDCDCD),
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
    val roundedValue = 8.dp
    Surface(
        modifier = Modifier.defaultMinSize(minWidth = 100.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(roundedValue)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, Modifier.size(32.dp), tint = iconColor)
            }
            Text(message, modifier = Modifier.padding(8.dp), color = textColor)
        }
    }
}