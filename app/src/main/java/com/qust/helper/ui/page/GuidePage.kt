package com.qust.helper.ui.page

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qust.helper.R
import com.qust.helper.ui.common.AppBar
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.viewmodel.account.EasAccountViewModel
import kotlinx.coroutines.delay

object GuidePage {

	@Composable
	fun EasAccountLogin(
		viewModel: EasAccountViewModel,
		dialogText: String,
		toastContent: MutableState<ToastContent>,
		btnSkip: () -> Unit,
		loginSuccess: () -> Unit
	) {

		var animPlay by remember{ mutableStateOf(false) }
		val animationTransition = updateTransition(targetState = animPlay, label = "enterAnim")
		val y by animationTransition.animateDp(label = "offsetAnim") { if(it) 0.dp else 100.dp }
		val alpha by animationTransition.animateFloat(label = "alphaAnim") { if(it) 1f else 0.5f }
		if(!animPlay) {
			LaunchedEffect(Unit){
				delay(300L)
				animPlay = true
			}
		}

		Column(
			modifier = Modifier.fillMaxSize().offset(y = y).alpha(alpha = alpha),
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			Row(
				modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 32.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.text_welcome),
					modifier = Modifier.padding(8.dp).weight(1f),
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

			Text(
				text = "\t欢迎使用青科助手，我们需要以下信息以便使用软件完整功能",
				modifier = Modifier.padding(16.dp),
				style = MaterialTheme.typography.bodyMedium,
			)

			LoginPage.EASLoginView(viewModel){
				loginSuccess()
			}
		}

		AppBar.DialogAndToast(dialogText = dialogText, toastContent = toastContent)
	}

}