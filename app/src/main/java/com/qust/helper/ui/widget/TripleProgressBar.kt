package com.qust.helper.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qust.helper.ui.theme.colorSuccess

/**
 * 有三种颜色的ProgressBar
 */
object TripleProgressBar {

	@Composable
	fun TripleProgressBar(
		redValue: Float = 0F,
		greenValue: Float = 0F,
		height: Dp = 5.dp,
		redColor: Color = MaterialTheme.colorScheme.error,
		greenColor: Color = colorSuccess,
	) {
		Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
			drawRoundRect(
				color = Color.Gray,
				topLeft = Offset(0F, 0F),
				size = Size(size.width, size.height),
				cornerRadius = CornerRadius(size.height / 2F),
			)
			if(redValue > 0F) {
				drawRoundRect(
					color = redColor,
					topLeft = Offset(0F, 0F),
					size = Size(size.width * redValue, size.height),
					cornerRadius = CornerRadius(size.height / 2F),
				)
			}
			if(greenValue > 0F) {
				drawRoundRect(
					color = greenColor,
					topLeft = Offset(0F, 0F),
					size = Size(size.width * greenValue, size.height),
					cornerRadius = CornerRadius(size.height / 2F),
				)
			}
		}
	}
}