package com.qust.helper.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.qust.helper.R

fun toastOK(message: String): ToastContent {
	return ToastContent(R.drawable.tips_finish, message)
}

fun toastWarning(message: String): ToastContent {
	return ToastContent(R.drawable.tips_warning, message)
}

fun toastError(message: String): ToastContent {
	return ToastContent(R.drawable.tips_error, message)
}

data class ToastContent(
	var icon: Int = 0,
	val message: String = "",
)

@Composable
fun ToastComponent(toastContent: MutableState<ToastContent>) {
	val scope = rememberCoroutineScope()
	val snackBarHostState = remember { SnackbarHostState() }
	if(toastContent.value.icon != 0) {
		LaunchedEffect(scope) {
			snackBarHostState.showSnackbar(
				message = toastContent.value.message,
				duration = SnackbarDuration.Short
			)
			toastContent.value = ToastContent()
		}
		SnackbarHost(hostState = snackBarHostState) {
			MySnackBar(toastContent.value.icon, toastContent.value.message)
		}
	}
}

@Composable
fun MySnackBar(icon: Int, message: String) {
	Card(
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
		elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
		modifier = Modifier.padding(16.dp).fillMaxWidth(),
	) {
		Row(
			modifier = Modifier.padding(16.dp, 8.dp).fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier.width(32.dp).height(32.dp).padding(),
				painter = painterResource(icon),
				contentDescription = ""
			)
			Text(text = message,
				modifier = Modifier.padding(8.dp),
				style = MaterialTheme.typography.bodyMedium,
			)
		}
	}
}