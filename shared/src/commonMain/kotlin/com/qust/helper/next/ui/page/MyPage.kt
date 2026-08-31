package com.qust.helper.next.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.App
import com.qust.helper.next.repository.AccountRepository
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.onClick
import com.qust.helper.next.ui.drawables.Drawables
import com.qust.helper.next.ui.drawables.GridView
import com.qust.helper.next.ui.page.account.AccountManagerPage
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.page.lesson.LessonTablePage
import com.qust.helper.next.ui.page.setting.SettingPage
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.Colors
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import org.jetbrains.compose.resources.painterResource
import qust_helper_next.shared.generated.resources.Res
import qust_helper_next.shared.generated.resources.icon_login_user_male
import qust_helper_next.shared.generated.resources.icon_no_login_user
import kotlin.reflect.KClass


@Preview
@Composable
private fun MyPreview() = AppPreview(::MyUI)

class MyPage : AppPage<MyViewModel>(title = "我的", viewModelClass = MyViewModel::class) {
	@Composable
	override fun Content(viewModel: MyViewModel) = MyUI(viewModel)
}

@Composable
private fun MyUI(viewModel: MyViewModel) {
	val router = App.router
	val scrollState = rememberScrollState()

	val account by AccountRepository.qustAccountInfo.collectAsStateWithLifecycle()
	LaunchedEffect(Unit){
		if(account.easAccount.isNotEmpty()){
			viewModel.hasLogin = true
			viewModel.userName = account.easAccount
		}else if(account.ipassAccount.isNotEmpty()){
			viewModel.hasLogin = true
			viewModel.userName = account.ipassAccount
		}else{
			viewModel.hasLogin = false
			viewModel.userName = "未登录"
		}
	}

	Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
		Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {
			router.startPage<AccountManagerPage>()
		}){
			Row(verticalAlignment = Alignment.CenterVertically) {
				Image(painter = painterResource(if(viewModel.hasLogin) Res.drawable.icon_login_user_male else Res.drawable.icon_no_login_user), contentDescription = null, modifier = Modifier.size(64.dp))
				Column(modifier = Modifier.padding(start = 8.dp)){
					Text(text = viewModel.userName, color = LocalContentColor.current)
					Text(text = if(viewModel.hasLogin) "" else "请先登录", style = Theme.textStyles.caption, color = Theme.color.textSecondary)
				}
			}
			Icon(contentDescription = null, imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, tint = Theme.color.textSecondary, modifier = Modifier.align(Alignment.CenterEnd))
		}

		ContentGridView("课表", viewModel.lesson){ router.startPage(it) }

		ContentGridView("其他", viewModel.other){ router.startPage(it) }
	}
}

@Composable
private fun ContentGridView(title: String, pages: List<PageEntrance>, clickItem: (KClass<out BasePage<*>>) -> Unit) {
	Card(modifier = Modifier.padding(8.dp)) {
		Text(text = title, style = Theme.textStyles.bodyStrong, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 4.dp))

		repeat((pages.size + 3) / 4){ row ->
			Row(Modifier.fillMaxWidth()) {
				repeat(4){ col ->
					if(row * 4 + col < pages.size){
						val page = pages[row * 4 + col]
						Column(
							modifier = Modifier.weight(1F).padding(8.dp).onClick { clickItem(page.page) },
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							if(page.icon != null){
								Icon(
									painter = rememberVectorPainter(page.icon),
									contentDescription = page.title,
									tint = Colors.LESSON_TEXT_COLORS[(row * 4 + col) % (Colors.LESSON_TEXT_COLORS.size - 1) + 1]
								)
							}
							Text(text = page.title, style = Theme.textStyles.caption)
						}
					}else{
						Spacer(modifier = Modifier.weight(1F))
					}
				}

			}

		}
		Spacer(modifier = Modifier.height(12.dp))
	}
}

class MyViewModel : BaseViewModel() {
	var hasLogin by mutableStateOf(false)
	var userName by mutableStateOf("未登录")

	val lesson = listOf(PageEntrance(title = "课表", icon = Drawables.GridView, page = LessonTablePage::class))

	val other = listOf(PageEntrance(title = "设置", icon = Icons.Default.Settings, page = SettingPage::class))
}

data class PageEntrance(
	val title: String,
	val icon: ImageVector? = null,
	val page: KClass<out BasePage<*>>,
)