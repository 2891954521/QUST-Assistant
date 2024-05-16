package com.qust.helper.ui.appwidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.qust.helper.R
import com.qust.helper.model.account.DrinkAccount
import com.qust.helper.ui.activity.ComposeActivity
import com.qust.helper.utils.LinearBarCode

private val DrinkCodePreferenceKey = stringPreferencesKey("drinkCode")

class DrinkCode : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		provideContent { GlanceTheme { DrinkCodeView() } }
	}
	
	@Composable
	fun DrinkCodeView() {
		val prefs = currentState<Preferences>()
		val drinkCode = prefs[DrinkCodePreferenceKey] ?: ""
		Row(
			modifier = GlanceModifier.fillMaxSize().padding(4.dp).background(ImageProvider(R.drawable.card_background)).clickable(actionRunCallback<UpdateCode>()),
			horizontalAlignment = Alignment.Start,
			verticalAlignment = Alignment.CenterVertically
		){
			if(drinkCode.isNotEmpty()){
				Image(
					provider = ImageProvider(LinearBarCode.createCode128Barcode(drinkCode, 50)),
					contentDescription = null,
					contentScale = ContentScale.FillBounds,
					modifier = GlanceModifier.fillMaxSize().padding(8.dp)
				)
			}else{
				Text(
					text = "请先登录",
					modifier = GlanceModifier.fillMaxSize(),
					style = TextStyle(textAlign = TextAlign.Center, fontSize = 21.sp)
				)
			}
		}
	}
}


class DrinkCodeReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = DrinkCode()
}


class UpdateCode : ActionCallback {
	override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
		val drinkCode = DrinkAccount.getInstance().drinkCode

		if(drinkCode.isEmpty()){
			context.startActivity(Intent(context, ComposeActivity::class.java).putExtra("page", "drinkCode").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
		}else{
			updateAppWidgetState(
				context = context,
				definition = PreferencesGlanceStateDefinition,
				glanceId = glanceId,
			){ preferences -> preferences.toMutablePreferences().apply { this[DrinkCodePreferenceKey] = DrinkAccount.getInstance().drinkCode } }

			DrinkCode().update(context, glanceId)
		}
	}
}