package com.qust.helper.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object AppBar{

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun TopBar(
		title: String,
		navigationIcon: ImageVector? = null,
		navigationClick: () -> Unit = { },
	) {
		if(navigationIcon == null){
			TopAppBar(
				title = { TitleText(title = title) }
			)
		}else{
			TopAppBar(
				title = { TitleText(title = title) },
				navigationIcon = {
					BackIcon(icon = navigationIcon) {
						navigationClick()
					}
				}
			)
		}
	}

	@Composable
	fun TitleText(title: String){
		Text(
			text = title,
			modifier = Modifier.padding(16.dp),
			style = MaterialTheme.typography.titleLarge,
			maxLines = 1,
		)
	}

	@Composable
	fun BackIcon(
		icon: ImageVector,
		onClick: () -> Unit
	){
		val interactionSource = remember { MutableInteractionSource() }
		val rippleIndication = rememberRipple(bounded = false, color = Color.Gray)

		Box(
			modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp).clickable(
				interactionSource = interactionSource,
				indication = rippleIndication,
			) {
				onClick()
			}.background(
				color = Color.Transparent,
				shape = CircleShape
			)
		){
			Icon(
				imageVector = icon,
				contentDescription = "Back",
				modifier = Modifier.padding(8.dp)
			)
		}

	}

	@Composable
	fun DialogAndToast(dialogText: String, toastContent: MutableState<ToastContent>){
		if(dialogText.isNotEmpty()) Dialogs.IndeterminateProgressDialog(dialogText)
		ToastComponent(toastContent)
	}

	@Composable
	fun DialogBar(dialogText: String){
		if(dialogText.isNotEmpty()) Dialogs.IndeterminateProgressDialog(dialogText)
	}
}




@Preview
@Composable
fun TopBarPreview() {
	AppBar.TopBar(
		"Title",
		navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
		)
}