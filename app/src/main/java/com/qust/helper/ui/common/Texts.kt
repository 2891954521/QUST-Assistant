package com.qust.helper.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

object Texts {
	
	@Composable
	fun SingleLineText(
		text: String,
		style: TextStyle = MaterialTheme.typography.bodyMedium,
		color: Color = style.color,
		textAlign: TextAlign = TextAlign.Center,
		modifier: Modifier = Modifier
	){
		Text(
			text = text,
			modifier = modifier.padding(8.dp),
			style = style,
			color = color,
			textAlign = textAlign,
			maxLines = 1,
		)
	}

	@Composable
	fun SingleLineTextNoPadding(
		text: String,
		style: TextStyle = MaterialTheme.typography.bodyMedium,
		color: Color = style.color,
		textAlign: TextAlign = TextAlign.Center,
		modifier: Modifier = Modifier
	){
		Text(
			text = text,
			modifier = modifier,
			style = style,
			color = color,
			textAlign = textAlign,
			maxLines = 1,
		)
	}
}