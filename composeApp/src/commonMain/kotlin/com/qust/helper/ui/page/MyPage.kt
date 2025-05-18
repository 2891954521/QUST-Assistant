package com.qust.helper.ui.page

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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.Res
import com.qust.helper.icon_login_user_male
import com.qust.helper.icon_no_login_user
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.ui.drawables.Drawables
import com.qust.helper.ui.drawables.IconLogin
import com.qust.helper.ui.page.account.AccountManagerPage
import com.qust.helper.ui.page.app.SettingPage
import com.qust.helper.ui.page.eas.QueryAcademicPage
import com.qust.helper.ui.page.eas.QueryExamPage
import com.qust.helper.ui.page.eas.QueryLessonPage
import com.qust.helper.ui.page.eas.QueryMarkPage
import com.qust.helper.ui.page.eas.QueryNoticePage
import com.qust.helper.ui.page.lesson.LessonTablePage
import com.qust.helper.ui.page.third.DrinkPage
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.click
import com.qust.helper.viewmodel.MyViewModel
import org.jetbrains.compose.resources.painterResource

object MyPage: BasePage<MyViewModel>("我的", Drawables.IconLogin) {

    private val lesson = arrayOf<BasePage<*>>(LessonTablePage)

    private val eas = arrayOf<BasePage<*>>(QueryLessonPage, QueryMarkPage, QueryExamPage, QueryAcademicPage, QueryNoticePage)

    private val business = arrayOf<BasePage<*>>(EmptyPage)

    private val otherSystem = arrayOf<BasePage<*>>(DrinkPage)

    private val web = arrayOf<BasePage<*>>(EmptyPage)

    private val other = arrayOf<BasePage<*>>(SettingPage)


    @Composable
    override fun getViewModel() = viewModel<MyViewModel>()

    @Composable
    override fun Content(viewModel: MyViewModel) {
        val scrollState = rememberScrollState()
        val pageController = rememberPageController()

        LaunchedEffect(Unit){
            val eas = EasAccount.getAccountName()
            val ipass = IPassAccount.getAccountName()
            if(eas.isNotEmpty()){
                viewModel.hasLogin = true
                viewModel.userName = eas
            }else if(ipass.isNotEmpty()){
                viewModel.hasLogin = true
                viewModel.userName = ipass
            }else{
                viewModel.hasLogin = false
                viewModel.userName = "未登录"
            }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {
                pageController.startPage(AccountManagerPage)
            }){
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(if(viewModel.hasLogin) Res.drawable.icon_login_user_male else Res.drawable.icon_no_login_user), contentDescription = null, modifier = Modifier.size(84.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)){
                        Text(text = viewModel.userName, style = MaterialTheme.typography.titleLarge)
                        Text(text = if(viewModel.hasLogin) "" else "请先登录", style = MaterialTheme.typography.titleSmall, color = colorSecondaryText)
                    }
                }
                Icon(contentDescription = null, imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, tint = colorSecondaryText, modifier = Modifier.align(Alignment.CenterEnd))
            }

            ContentGridView("课表", lesson, pageController::startPage)
            ContentGridView("教务系统", eas, pageController::startPage)
            ContentGridView("业务系统", business, pageController::startPage)
            ContentGridView("其他系统", otherSystem, pageController::startPage)
            ContentGridView("网页入口", web, pageController::startPage)
            ContentGridView("其他", other, pageController::startPage)
        }
    }

    @Composable
    private fun ContentGridView(title: String, pages: Array<BasePage<*>>, clickItem: (BasePage<*>) -> Unit) {
        Card(modifier = Modifier.padding(8.dp)) {
            Text(text = title, fontWeight = FontWeight.W700, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 4.dp))

            repeat((pages.size + 3) / 4){ row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(4){ col ->
                        if(row * 4 + col < pages.size){
                            val page = pages[row * 4 + col]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1F).padding(8.dp).click { clickItem(page) }
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(page.icon),
                                    contentDescription = page.title,
                                    tint = LESSON_TEXT_COLORS[(row * 4 + col) % (LESSON_TEXT_COLORS.size - 1) + 1]
                                )
                                Text(text = page.title, style = MaterialTheme.typography.labelMedium)
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