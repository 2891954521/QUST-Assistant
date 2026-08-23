package com.qust.helper.ui.appwidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.qust.helper.ui.activity.MainActivity
import com.qust.helper.utils.LinearBarCode
import com.qust.helper.utils.SettingUtils

/**
 * 饮水码桌面小组件，展示登录后缓存的饮水码（Code128 条形码）
 */
class DrinkCodeWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val drinkCode = SettingUtils.getString("drinkCode")
		val openAction = actionStartActivity(
			Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		)
		val barcode = if(drinkCode.isNotEmpty()) drinkCode.toBarcodeBitmap(40) else null

		provideContent {
			GlanceTheme {
				DrinkCodeView(barcode, openAction)
			}
		}
	}
}

@Composable
fun DrinkCodeView(barcode: Bitmap?, openAction: Action) {
	if(barcode == null) {
		Box(
			modifier = GlanceModifier.fillMaxSize().padding(8.dp).clickable(openAction),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "请先登录并获取饮水码",
				style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant)
			)
		}
	} else {
		Image(
			provider = ImageProvider(barcode),
			contentDescription = null,
			contentScale = ContentScale.FillBounds,
			modifier = GlanceModifier.fillMaxSize().padding(8.dp).clickable(openAction)
		)
	}
}

/**
 * 将 LinearBarCode.encode 生成的布尔像素数组绘制成位图
 */
private fun String.toBarcodeBitmap(height: Int): Bitmap {
	val pixels = LinearBarCode.encode(this)
	val bitmap = Bitmap.createBitmap(pixels.size, height, Bitmap.Config.ARGB_8888)
	val canvas = Canvas(bitmap)
	canvas.drawColor(Color.WHITE)
	val paint = Paint().apply { this.color = Color.BLACK }
	for(i in pixels.indices) {
		if(pixels[i]) {
			canvas.drawRect(i.toFloat(), 0f, (i + 1).toFloat(), height.toFloat(), paint)
		}
	}
	return bitmap
}

class DrinkCodeWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = DrinkCodeWidget()
}
