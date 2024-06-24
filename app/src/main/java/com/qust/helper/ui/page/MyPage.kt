package com.qust.helper.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qust.helper.R
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.business.ElectricRecharge
import com.qust.helper.ui.page.business.SportTest
import com.qust.helper.ui.page.eas.GetAcademic
import com.qust.helper.ui.page.eas.GetExams
import com.qust.helper.ui.page.eas.GetLessonTable
import com.qust.helper.ui.page.eas.GetMarks
import com.qust.helper.ui.page.eas.GetNotice
import com.qust.helper.ui.page.lesson.DailyLesson
import com.qust.helper.ui.page.lesson.TermLesson
import com.qust.helper.ui.page.web.WebPage
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.Toast

object MyPage {

	private val lesson = arrayOf(
		DailyLesson.DailyLessonPage,
		TermLesson.TermLessonPage,
		Page(key = "${Keys.Page.SettingPage}?type=${SettingPage.LessonTable}", name = "课表设置", image = Icons.Rounded.Settings)
	)

	private val eas = arrayOf(
		GetNotice.GetNoticePage,
		GetLessonTable.GetLessonPage,
		GetMarks.GetMarksPage,
		GetAcademic.GetAcademicPage,
		GetExams.GetExamsPage,
		Page(key = "${Keys.Page.SettingPage}?type=${SettingPage.Eas}", name = "教务查询设置", image = Icons.Rounded.Settings){ _,_,_,_ -> }
	)

	private val business = arrayOf(
		ElectricRecharge.ElectricPage,
		SportTest.SportTestPage
	)

	private val otherSystem = arrayOf(
		DrinkPage.DrinkPage
	)

	private val web = arrayOf(
		WebPage.EasWebPage,
		WebPage.IpassWebPage
	)

	private val other = arrayOf(
		SettingPage.SettingPage
	)

	val MyPage = Page(Keys.Page.MyPage, "我", iconRes = R.drawable.ic_login) { activity, padding, navController, _ ->
		MyPageUI(padding, activity.toast, navController)
	}

	@Composable
	fun MyPageUI(padding: PaddingValues, toast: Toast, navController: NavController) {
		val scrollState = rememberScrollState()

		val account = EASAccount.getInstance().getAccount().ifEmpty { IPassAccount.getInstance().getAccount() }.ifEmpty { "" }

		Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState)) {
			Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { Page.navigate(navController, Keys.Page.AccountManager) }){
				Row(verticalAlignment = Alignment.CenterVertically) {
					Image(painter = painterResource(if(account.isEmpty()) R.drawable.icon_no_login_user else R.drawable.icon_login_user_male), contentDescription = null, modifier = Modifier.size(84.dp))
					Column(modifier = Modifier.padding(start = 8.dp)){
						Text(text = account.ifEmpty { "未登录" }, style = MaterialTheme.typography.titleLarge)
						Text(text = account.ifEmpty { "请先登录" }, style = MaterialTheme.typography.titleSmall, color = colorSecondaryText)
					}
				}
				Icon(contentDescription = null, imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, tint = colorSecondaryText, modifier = Modifier.align(Alignment.CenterEnd))
			}

			ContentGridView("课表", lesson, clickItem = { Page.navigate(navController, it) })
			ContentGridView("教务系统", eas, clickItem = { Page.navigate(navController, it) })
			ContentGridView("业务系统", business, clickItem = { Page.navigate(navController, it) })
			ContentGridView("其他系统", otherSystem, clickItem = { Page.navigate(navController, it) })
			ContentGridView("网页入口", web, clickItem = { Page.navigate(navController, it) })
			ContentGridView("其他", other, clickItem = { Page.navigate(navController, it) })
		}
	}

	@Composable
	private fun ContentGridView(title: String, pages: Array<Page>, clickItem: (String) -> Unit) {
		Card(modifier = Modifier.padding(8.dp)) {
			Text(text = title, fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 4.dp))

			repeat((pages.size + 3) / 4){ row ->
				Row(Modifier.fillMaxWidth()) {
					repeat(4){ col ->
						if(row * 4 + col < pages.size){
							val page = pages[row * 4 + col]
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								modifier = Modifier.weight(1F).padding(8.dp).clickable { clickItem(page.key) }
							) {
								Icon(
									painter = if(page.image != null) rememberVectorPainter(page.image) else painterResource(id = page.iconRes),
									contentDescription = page.name,
									tint = Color(TEXT_COLORS[(row * 4 + col) % (TEXT_COLORS.size - 1) + 1])
								)
								Text(text = page.name, style = MaterialTheme.typography.labelMedium)
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
}