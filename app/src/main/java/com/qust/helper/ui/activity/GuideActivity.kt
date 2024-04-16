package com.qust.helper.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.qust.helper.data.lesson.LessonTableQueryResult
import com.qust.helper.model.LessonTableRepository
import com.qust.helper.model.Logger
import com.qust.helper.model.account.EASAccount
import com.qust.helper.ui.page.LoginPage
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.AppBar
import com.qust.helper.viewmodel.account.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Calendar

class GuideActivity : BaseActivity() {

	private val viewModel by viewModels<GuideViewModel>()

	@Composable
	override fun Content() {
		var animPlay by remember{ mutableStateOf(false) }

		val animationTransition = updateTransition(targetState = animPlay, label = "enterAnim")
		val y by animationTransition.animateDp(label = "offsetAnim") { if(it) 0.dp else 100.dp }
		val alpha by animationTransition.animateFloat(label = "alphaAnim") { if(it) 1f else 0.5f }

		LaunchedEffect(animPlay){
			if(!animPlay) {
				delay(300L)
				animPlay = true
			}
		}

		Column(
			modifier = Modifier.statusBarsPadding().fillMaxSize().offset(y = y).alpha(alpha = alpha).padding(horizontal = 16.dp),
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = stringResource(R.string.text_welcome),
					modifier = Modifier.weight(1f),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.primary,
					textAlign = TextAlign.Start,
				)
				TextButton(onClick = { btnSkip() }) {
					Text(
						text = stringResource(id = R.string.text_skip),
						style = MaterialTheme.typography.bodySmall,
						color = colorSecondaryText
					)
				}
			}

			Text(text = "欢迎使用青科助手，我们需要以下信息以便使用软件完整功能")

			val context = LocalContext.current
			val keyboardController = LocalSoftwareKeyboardController.current

			fun login(){
				keyboardController?.hide()
				viewModel.login(viewModel.account.value, viewModel.password.value){ loginSuccess() }
			}

			LoginPage.AccountInput(
				account = viewModel.account,
				password = viewModel.password,
				accountError = viewModel.errorText.value.ifEmpty { viewModel.accountError },
				passwordError = viewModel.passwordError,
				"学号", "教务系统密码",
				login = { login() }
			)

			Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
				Checkbox(checked = viewModel.checkBox, onCheckedChange = { viewModel.checkBox = it })
				val str = buildAnnotatedString {
					append("我已阅读并同意 ")
					pushStringAnnotation(tag = "userAgreement", annotation = "用户协议")
					withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) { append("用户协议") }
					pop()
				}
				ClickableText(text = str, onClick = { offset ->
					str.getStringAnnotations(tag = "userAgreement", start = offset, end = offset).firstOrNull()?.let {
						ComposeActivity.startActivity(context, "userAgreement")
					}
				})
			}

			Button(modifier = Modifier.fillMaxWidth().padding(16.dp), onClick = { login() }){
				Text(text = stringResource(id = R.string.text_ok))
			}
		}

		AppBar.DialogBar(dialogText = viewModel.dialogText)
		toast.ToastContent(viewModel.toastContent)
	}

	private fun btnSkip() {
		Setting.edit { it.putBoolean(Keys.IS_FIRST_USE, false) }
		finish()
	}

	private fun loginSuccess() {
		Setting.edit { it.putBoolean(Keys.IS_FIRST_USE, false) }
		onBackPressedDispatcher.onBackPressed()
	}

	class GuideViewModel: AccountViewModel(){

		private val easAccount = EASAccount.getInstance()

		var account = mutableStateOf("")
		var password = mutableStateOf("")
		var accountError by mutableStateOf("")
		var passwordError by mutableStateOf("")

		var checkBox by mutableStateOf(false)

		override fun login(accountStr: String, passwordStr: String, block: () -> Unit) {
			if(account.value.isEmpty()) { accountError = "请输入学号"; return }
			else if(account.value.length < 2){ accountError = "学号格式错误"; return  }
			else accountError = ""
			if(password.value.isEmpty()) { passwordError = "请输入密码"; return }
			else passwordError = ""
			if(!checkBox){
				toastWarning("请先阅读并同意《用户许可协议》")
				return
			}
			viewModelScope.launch {
				try{
					showDialog("登录中")
					val result = withContext(Dispatchers.IO){
						easAccount.login(accountStr, passwordStr, true)
					}

					if(result){
						val year = Calendar.getInstance()[Calendar.YEAR].toString()
						val entranceTime = (year.substring(0, year.length - 2) + accountStr.substring(0, 2)).toInt()
						easAccount.entranceTime = entranceTime
						queryLesson(entranceTime, easAccount.getCurrentGrade())
						block()
					}else{
						toastError("用户名或密码错误")
					}
				}catch(e: IOException){
					Logger.e("LoginFail", e)
					toastError("网络错误: " + e.message)
				}finally{
					clearDialog()
				}
			}
		}

		suspend fun queryLesson(entranceTime: Int, index: Int){
			val calendar = Calendar.getInstance()
			val y = calendar[Calendar.YEAR]

			showDialog("正在查询课表")

			val result: LessonTableQueryResult
			withContext(Dispatchers.IO){
				easAccount.checkLogin()
				result = LessonTableRepository.queryLessonTable(easAccount = easAccount, (index / 2 + entranceTime).toString(), if(index % 2 == 0) "3" else "12")
				val error = result.error
				if(error == null) {
					if(!LessonTableRepository.saveLessonTable(result.lessonTable)){
						toastError("保存课表失败")
						clearDialog()
						delay(4000)
					}
				}else{
					toastError(error)
					clearDialog()
					delay(4000)
				}
			}
			clearDialog()
		}
	}
}

