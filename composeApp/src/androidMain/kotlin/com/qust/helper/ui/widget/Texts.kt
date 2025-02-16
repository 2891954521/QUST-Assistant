package com.qust.helper.ui.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.ui.colorSecondaryText

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

	@Composable
	fun IconText(
		text: String,
		icon: ImageVector,
		modifier: Modifier = Modifier,
		iconModifier: Modifier = Modifier
	) {
		Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically){
			Text(
				text = text,
				style = MaterialTheme.typography.bodySmall,
				modifier = Modifier.padding(start = 8.dp),
				color = colorSecondaryText,
			)
			Icon(imageVector = icon, modifier = iconModifier, contentDescription = null, tint = colorSecondaryText)
		}
	}
}