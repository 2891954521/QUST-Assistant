package com.qust.helper.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
	fun BackIcon(icon: ImageVector, onClick: () -> Unit){
		Box(modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(32.dp)).clickable { onClick() }){
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