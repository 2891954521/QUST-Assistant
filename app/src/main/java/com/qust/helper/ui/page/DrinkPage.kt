package com.qust.helper.ui.page

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.common.Inputs
import com.qust.helper.ui.common.Texts
import com.qust.helper.ui.common.ToastComponent
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.utils.LinearBarCode
import com.qust.helper.viewmodel.DrinkViewModel

/**
 * 饮水码
 */
object DrinkPage {

	@Composable
	fun DrinkPage(activity: ComponentActivity){
		val viewModel: DrinkViewModel by activity.viewModels()
		val layoutParams: WindowManager.LayoutParams = activity.window.attributes

		DrinkUI(
			drinkCode = viewModel.drinkCode.value,
			defaultProgress = layoutParams.screenBrightness,
			account = viewModel.drinkAccountStr,
			password = viewModel.drinkPasswordStr,
			needLogin = viewModel.needLogin,
			toastContent = viewModel.toastContent,
			dialogText = viewModel.dialogText.value,
			onProgress = {
				layoutParams.screenBrightness = it
				activity.window.attributes = layoutParams
			},
			login = { viewModel.login() },
			getDrinkCode = { viewModel.getDrinkCode() }
		)
	}

	@Composable
	fun DrinkUI(
		drinkCode: String,
		defaultProgress: Float = 0F,
		account: MutableState<String>,
		password: MutableState<String>,
		needLogin: MutableState<Boolean>,
		toastContent: MutableState<ToastContent>,
		dialogText: String = "",
		onProgress: (Float) -> Unit = { },
		login: () -> Unit = { },
		getDrinkCode: () -> Unit = { }
	){
		var brightness by remember { mutableFloatStateOf(defaultProgress) }

		var currentSize by remember { mutableIntStateOf(100) }
		var currentRotate by remember { mutableFloatStateOf(0F) }

		val barSize by animateDpAsState(targetValue = currentSize.dp, animationSpec = tween(300), label = "barSize")
		val arrowRotate by animateFloatAsState(targetValue = currentRotate, animationSpec = tween(300), label = "barSize")

		Scaffold{ padding ->
			Box(modifier = Modifier.padding(padding)){
				Column(
					Modifier.fillMaxSize(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					if(drinkCode.isNotEmpty()){
						Image(
							bitmap = LinearBarCode.createCode128Barcode(drinkCode, 1000, 50).asImageBitmap(),
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
						onClick = { if(drinkCode.isEmpty()){ needLogin.value = true } else getDrinkCode() }
					) {
						Text(text = if(drinkCode.isEmpty()) stringResource(id = R.string.text_login) else "刷新条码")
					}

					Box(modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp)){
						Texts.SingleLineText(
							text = "更换账号",
							color = colorSecondaryText,
							modifier = Modifier.align(Alignment.CenterEnd).clickable { needLogin.value = true }
						)
					}
				}
			}

			if(needLogin.value){
				LoginDialog(account, password, { needLogin.value = false }, { login() })
			}

			AppBar.DialogBar(dialogText = dialogText)

			ToastComponent(toastContent)
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
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Inputs.AccountInput(
					account = account,
					password = password,
					labelAccount = "手机号",
					login = { login(); onDismiss() }
				)
				TextButton( onClick = { login(); onDismiss() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
					Text(text = stringResource(id = R.string.text_login))
				}
				Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
			}
		}
	}
}