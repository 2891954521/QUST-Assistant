package com.qust.helper.ui.appwidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.qust.helper.ui.activity.MainActivity
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

		provideContent {
			GlanceTheme {
				DrinkCodeView(drinkCode, openAction)
			}
		}
	}
}

@Composable
fun DrinkCodeView(drinkCode: String, openAction: Action) {
	Box(
		modifier = GlanceModifier.fillMaxSize().padding(8.dp).clickable(openAction),
		contentAlignment = Alignment.Center
	) {
		if(drinkCode.isEmpty()) {
			Text(
				text = "请先登录并获取饮水码",
				style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurfaceVariant)
			)
		} else {
			Text(
				text = drinkCode,
				style = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center, color = GlanceTheme.colors.onSurface)
			)
		}
	}
}

class DrinkCodeWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = DrinkCodeWidget()
}
