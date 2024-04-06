package com.qust.helper.ui.widget

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


open class ToastAble(context: Context){
	private var isShowToast = false

	private var toastWindow = context.getSystemService(ComponentActivity.WINDOW_SERVICE) as WindowManager

//	private var toastLayout = ComposeView(context).also { it.setContent { ToastContent() } }
	private var toastLayout = LayoutInflater.from(context).inflate(R.layout.layout_tips, null) as LinearLayout

	private var toastIcon = toastLayout.findViewById<ImageView>(R.id.tips_icon)
	private var toastMessage = toastLayout.findViewById<TextView>(R.id.tips_message)

	private var toastParams = WindowManager.LayoutParams().also {
		it.gravity = Gravity.CENTER
		it.format = PixelFormat.TRANSLUCENT
		it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		it.width = WindowManager.LayoutParams.WRAP_CONTENT
		it.height = WindowManager.LayoutParams.WRAP_CONTENT
		it.windowAnimations = android.R.style.Animation_Toast
	}

//	private var text by mutableStateOf("")
//	private var icon by mutableIntStateOf(R.drawable.tips_finish)
	fun toast(icon: Int, message: String){
		toastIcon.setImageResource(icon)
		toastMessage.text = message
		if(!isShowToast){
			isShowToast = true
			toastWindow.addView(toastLayout, toastParams)
			CoroutineScope(Dispatchers.Main).launch {
				delay(4000)
				toastWindow.removeView(toastLayout)
				isShowToast = false
			}
		}
	}

	fun toastOK(message: String){ toast(R.drawable.tips_finish, message) }

	fun toastWarning(message: String) { toast(R.drawable.tips_warning, message) }

	fun toastError(message: String) { toast(R.drawable.tips_error, message) }

//	@Composable
//	fun ToastContent() {
//		Card(
//			colors = CardDefaults.cardColors(containerColor = Color(0x7A000000)),
//			shape = RoundedCornerShape(16.dp),
//			modifier = Modifier.wrapContentSize(),
//		) {
//			Column(
//				modifier = Modifier.padding(16.dp, 8.dp).fillMaxWidth(),
//				horizontalAlignment = Alignment.CenterHorizontally,
//			) {
//				Icon(
//					modifier = Modifier.size(48.dp),
//					painter = painterResource(icon),
//					contentDescription = ""
//				)
//				Text(text = text, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium)
//			}
//		}
//	}
}


fun toastOK(message: String): ToastContent {
	return ToastContent(R.drawable.tips_finish, message)
}

fun toastWarning(message: String): ToastContent {
	return ToastContent(R.drawable.tips_warning, message)
}

fun toastError(message: String): ToastContent {
	return ToastContent(R.drawable.tips_error, message)
}

class ToastContent(
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