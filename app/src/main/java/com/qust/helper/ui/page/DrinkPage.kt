package com.qust.helper.ui.page

import android.view.WindowManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.ui.activity.BaseActivity
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.AppBar
import com.qust.helper.ui.widget.Inputs
import com.qust.helper.ui.widget.Texts
import com.qust.helper.utils.LinearBarCode
import com.qust.helper.viewmodel.DrinkUIEvent
import com.qust.helper.viewmodel.DrinkUIState
import com.qust.helper.viewmodel.DrinkViewModel

/**
 * 饮水码
 */
object DrinkPage {

	@Composable
	fun DrinkPage(padding: PaddingValues, viewModel: DrinkViewModel, activity: BaseActivity){
		val layoutParams: WindowManager.LayoutParams = activity.window.attributes
		DrinkUI(
			padding = padding,
			defaultProgress = layoutParams.screenBrightness,
			onProgress = {
				layoutParams.screenBrightness = it
				activity.window.attributes = layoutParams
			},
			uiState = viewModel.uiState,
			uiEvent = viewModel.uiEvent,
		)
		AppBar.DialogBar(dialogText = viewModel.dialogText)
		activity.toast.ToastContent(toastContent = viewModel.toastContent)
	}

	@Composable
	fun DrinkUI(
		padding: PaddingValues,
		defaultProgress: Float = 0F,
		onProgress: (Float) -> Unit = { },
		uiState: DrinkUIState,
		uiEvent: DrinkUIEvent,
	){
		var brightness by remember { mutableFloatStateOf(defaultProgress) }

		var currentSize by remember { mutableIntStateOf(100) }
		var currentRotate by remember { mutableFloatStateOf(0F) }

		val barSize by animateDpAsState(targetValue = currentSize.dp, animationSpec = tween(300), label = "barSize")
		val arrowRotate by animateFloatAsState(targetValue = currentRotate, animationSpec = tween(300), label = "barSize")

		Box(modifier = Modifier.padding(padding)){
			Column(
				Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				if(uiState.drinkCode.isNotEmpty()){
					Image(
						bitmap = LinearBarCode.createCode128Barcode(uiState.drinkCode, 1000, 50).asImageBitmap(),
						contentDescription = "",
						contentScale = ContentScale.FillBounds,
						modifier = Modifier.fillMaxWidth().height(barSize)
					)
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

				Row(
					modifier = Modifier.padding(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_brightness),
						contentDescription = "",
						modifier = Modifier.padding(8.dp),
						tint = Color(200, 200, 200)
					)
					Slider(
						value = brightness,
						onValueChange = { brightness = it; onProgress(it) },
					)
				}

				Button(
					modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
					onClick = { if(uiState.drinkCode.isEmpty()){ uiState.needLogin = true } else uiEvent.getDrinkCode() }
				) {
					Text(text = if(uiState.drinkCode.isEmpty()) stringResource(id = R.string.text_login) else "刷新条码")
				}

				Box(modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)){
					Texts.SingleLineText(
						text = "更换账号",
						color = colorSecondaryText,
						modifier = Modifier.align(Alignment.CenterEnd).clickable { uiState.needLogin = true }
					)
				}
			}
		}

		if(uiState.needLogin){
			LoginDialog(uiState.account, uiState.password, { uiState.needLogin = false }, { a, b -> uiEvent.login(a, b) })
		}
	}

	@Composable
	@OptIn(ExperimentalMaterial3Api::class)
	fun LoginDialog(
		account: String,
		password: String,
		onDismiss: () -> Unit = { },
		login: (String, String) -> Unit = { _, _ -> }
	) {
		val newAccount = remember { mutableStateOf(account) }
		val newPassword = remember { mutableStateOf(password) }
		ModalBottomSheet(onDismissRequest = { onDismiss() }) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Inputs.AccountInput(
					account = newAccount,
					password = newPassword,
					labelAccount = "手机号",
					login = { login(newAccount.value, newAccount. value); onDismiss() }
				)
				TextButton( onClick = { login(newAccount.value, newPassword.value); onDismiss() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
					Text(text = stringResource(id = R.string.text_login))
				}
				Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
			}
		}
	}
}