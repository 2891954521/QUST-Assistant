package com.qust.helper.ui.page.third

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.data.i18n.Strings
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.Water
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.form.AccountInput
import com.qust.helper.utils.LinearBarCode
import com.qust.helper.viewmodel.third.DrinkUIEvent
import com.qust.helper.viewmodel.third.DrinkUIState
import com.qust.helper.viewmodel.third.DrinkViewModel


object DrinkPage: BasePage<DrinkViewModel>("饮水码", Drawables.Water) {

	@Composable
	override fun getViewModel() = viewModel<DrinkViewModel>()

	@Composable
	override fun Content(viewModel: DrinkViewModel) {
		val uiState = viewModel.uiState

		DrinkUI(
			uiState = uiState,
			uiEvent = viewModel,
		)

		if(uiState.needLogin){
			LoginDialog(uiState.account, uiState.password, { uiState.needLogin = false }, { viewModel.login() })
		}
	}

	@Composable
	fun DrinkUI(
		uiState: DrinkUIState,
		uiEvent: DrinkUIEvent,
	){
//		val layoutParams: WindowManager.LayoutParams = activity.window.attributes
//		var brightness by remember { mutableFloatStateOf(defaultProgress) }

		var currentSize by remember { mutableIntStateOf(100) }
		var currentRotate by remember { mutableFloatStateOf(0F) }

		val barSize by animateDpAsState(targetValue = currentSize.dp, animationSpec = tween(300), label = "barSize")
		val arrowRotate by animateFloatAsState(targetValue = currentRotate, animationSpec = tween(300), label = "barSize")

		val code by remember(uiState.drinkCode) {
			mutableStateOf(LinearBarCode.encode(uiState.drinkCode))
		}

		Column(
			Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			if(uiState.drinkCode.isNotEmpty()){
				Canvas(Modifier.fillMaxWidth().height(barSize).padding(horizontal = 16.dp).clip(RoundedCornerShape(8.dp))) {
					val count = code.size
					val unitWidth = (size.width - 50) / count

					drawRect(color = Color.White, size = size)
					var i = 0
					var outputX = 25F
					while(i < count) {
						if(code[i]) {
							drawRect(color = Color.Black, Offset(outputX, 0F), Size(unitWidth, size.height))
						}
						i++
						outputX += unitWidth
					}
				}
				Card(
					shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
					elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
				) {
					Box(modifier = Modifier.clickable {
						currentSize = if(currentSize == 100) 200 else 100
						currentRotate = if(currentRotate == 0F) 180F else 0F
					}){
						Icon(
							imageVector = Icons.Rounded.ArrowDropDown,
							contentDescription = "",
							modifier = Modifier.padding(8.dp, 4.dp, 8.dp, 4.dp).rotate(arrowRotate)
						)
					}
				}
			}else{
				Box(modifier = Modifier.fillMaxWidth().height(100.dp)){
					Text(
						text = "请先登录",
						modifier = Modifier.align(Alignment.Center),
						style = MaterialTheme.typography.displaySmall,
						textAlign = TextAlign.Center
					)
				}
			}

//			Row(
//				modifier = Modifier.padding(8.dp),
//				verticalAlignment = Alignment.CenterVertically
//			) {
//				Icon(
//					imageVector = Drawables.Brightness,
//					contentDescription = "",
//					modifier = Modifier.padding(8.dp),
//					tint = Color(200, 200, 200)
//				)
//				Slider(
//					value = brightness,
//					onValueChange = { brightness = it; onProgress(it) },
//				)
//			}

			Button(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
				onClick = { if(uiState.drinkCode.isEmpty()){ uiState.needLogin = true } else uiEvent.getDrinkCode() }
			) {
				Text(text = if(uiState.drinkCode.isEmpty()) Strings.TEXT_LOGIN else "刷新条码")
			}

			Box(modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)){
				Text(
					text = "更换账号",
					color = colorSecondaryText,
					modifier = Modifier.align(Alignment.CenterEnd).clickable { uiState.needLogin = true }
				)
			}
		}

	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun LoginDialog(
		account: MutableState<String>,
		password: MutableState<String>,
		onDismiss: () -> Unit = { },
		login: () -> Unit = { }
	) {
		ModalBottomSheet(onDismissRequest = { onDismiss() }) {
			Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
				AccountInput(
					account = account,
					password = password,
					labelAccount = "手机号",
					login = { login(); onDismiss() }
				)
				TextButton(onClick = { login(); onDismiss() }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
					Text(text = Strings.TEXT_LOGIN)
				}
				Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
			}
		}
	}

}