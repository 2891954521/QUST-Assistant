package com.qust.helper.next.ui.page.thrid

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.common.exception.NeedLoginException
import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.repository.thrid.DrinkCodeRepository
import com.qust.helper.next.ui.business.dialog.BottomDialog
import com.qust.helper.next.ui.business.login.AccountInput
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.BackHandler
import com.qust.helper.next.ui.component.PrimaryButton
import com.qust.helper.next.ui.component.button.TextButton
import com.qust.helper.next.ui.component.button.TextButtonColors
import com.qust.helper.next.ui.component.layout.SurfaceBox
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.ContainerColors
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import com.qust.helper.next.utils.LinearBarCode


@Preview
@Composable
private fun DrinkCodePreview() = AppPreview(::DrinkCodeUI)

class DrinkCodePage: AppPage<DrinkCodeViewModel>(title = "饮水码", viewModelClass = DrinkCodeViewModel::class) {
	@Composable
	override fun Content(viewModel: DrinkCodeViewModel) = DrinkCodeUI(viewModel)
}

@Composable
private fun DrinkCodeUI(viewModel: DrinkCodeViewModel) {
	val drinkCode by viewModel.drinkCode

	var currentSize by remember { mutableIntStateOf(100) }
	var currentRotate by remember { mutableFloatStateOf(0F) }

	val barSize by animateDpAsState(targetValue = currentSize.dp, animationSpec = tween(300), label = "barSize")
	val arrowRotate by animateFloatAsState(targetValue = currentRotate, animationSpec = tween(300), label = "arrowRotate")

	val code = remember(drinkCode) {
		if(drinkCode.isEmpty()) BooleanArray(0) else LinearBarCode.encode(drinkCode)
	}

	BackHandler(viewModel.needLogin) {
		viewModel.needLogin = false
	}

	Box(Modifier.fillMaxSize()) {
		Column(
			Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			if(drinkCode.isNotEmpty()) {
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
				SurfaceBox(Modifier.clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)).clickable {
					currentSize = if(currentSize == 100) 200 else 100
					currentRotate = if(currentRotate == 0F) 180F else 0F
				}){
					Icon(
						imageVector = Icons.Rounded.ArrowDropDown,
						contentDescription = "",
						modifier = Modifier.padding(8.dp, 4.dp, 8.dp, 4.dp).rotate(arrowRotate)
					)
				}
			} else {
				Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
					Text(
						text = "请先登录",
						modifier = Modifier.align(Alignment.Center),
						style = Theme.textStyles.subtitle,
						textAlign = TextAlign.Center
					)
				}
			}

			PrimaryButton(
				modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
				text = if(drinkCode.isEmpty()) Strings.TEXT_LOGIN else "刷新条码",
				onClick = {
					if(drinkCode.isEmpty()) viewModel.needLogin = true
					else viewModel.getDrinkCode()
				}
			)

			Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
				TextButton(
					modifier = Modifier.align(Alignment.CenterEnd),
					text = "更换账号",
					colors = TextButtonColors.Secondary,
					onClick = { viewModel.needLogin = true }
				)
			}
		}

		BottomDialog(isExpanded = viewModel.needLogin) {
			LoginSheet(viewModel)
		}
	}
}

@Composable
private fun LoginSheet(viewModel: DrinkCodeViewModel) {
	val keyboardController = LocalSoftwareKeyboardController.current

	Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

		Text(text = "登录", style = Theme.textStyles.bodyLarge)

		AccountInput(
			account = viewModel.account,
			password = viewModel.password,
			accountError = viewModel.accountError,
			passwordError = viewModel.passwordError,
			labelAccount = "手机号",
			login = {
				keyboardController?.hide()
				viewModel.login()
			}
		)

		Spacer(modifier = Modifier.weight(1F))

		Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			PrimaryButton(
				modifier = Modifier.weight(1F),
				text = Strings.TEXT_CANCEL,
				colors = ContainerColors.Danger,
				onClick = {
					keyboardController?.hide()
					viewModel.needLogin = false
				}
			)
			PrimaryButton(
				modifier = Modifier.weight(1F),
				text = Strings.TEXT_LOGIN,
				onClick = {
					keyboardController?.hide()
					viewModel.login()
				}
			)
		}

		Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
	}
}

class DrinkCodeViewModel : BaseViewModel() {

	val drinkCode: State<String> = DrinkCodeRepository.drinkCode

	val account = mutableStateOf(AppSetting[DataKeys.DRINK_ACCOUNT, ""])
	val password = mutableStateOf(AppSetting[DataKeys.DRINK_PASSWORD, ""])

	var accountError by mutableStateOf("")
	var passwordError by mutableStateOf("")
	var needLogin by mutableStateOf(false)

	fun login() {
		if(account.value.isEmpty()) { accountError = "请输入手机号"; return }
		if(password.value.isEmpty()) { passwordError = "请输入密码"; return }

		accountError = ""
		passwordError = ""

		loading {
			DrinkCodeRepository.login(account.value, password.value)
			DrinkCodeRepository.getDrinkCode()
			needLogin = false
		}
	}

	fun getDrinkCode() {
		loading {
			try {
				DrinkCodeRepository.getDrinkCode()
			} catch(e: NeedLoginException) {
				toastWarning("请先登录")
				needLogin = true
			}
		}
	}
}
