package com.qust.helper.next.ui.component.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.ButtonType
import com.qust.helper.next.ui.theme.Theme

@Composable
fun TextButton(
	modifier: Modifier = Modifier,
	text: String,
	enabled: Boolean = true,
	type: ButtonType = ButtonType.Normal,
	hoverColor: Color = Theme.color.hover,
	onClick: () -> Unit
) {
	CompositionLocalProvider(
		LocalRippleConfiguration provides RippleConfiguration(color = hoverColor),
	) {
		Box(modifier = modifier
			.clip(RoundedCornerShape(5.dp))
			.clickable(enabled = enabled, onClick = onClick)
		) {
			Text(
				text = text,
				style = type.textStyle,
				modifier = Modifier.align(Alignment.Center).padding(type.innerPadding).alpha(if(enabled) 1F else 0.5F)
			)
		}
	}

}