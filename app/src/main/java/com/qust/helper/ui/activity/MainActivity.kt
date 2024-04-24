package com.qust.helper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.data.DrawerPageGroup
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.data.Setting
import com.qust.helper.model.AutoQueryRepository
import com.qust.helper.model.UpdateRepository
import com.qust.helper.ui.page.lesson.TermLesson
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.theme.colorSecondaryText
import com.qust.helper.ui.widget.AppWidgets
import com.qust.helper.ui.widget.Dialogs
import com.qust.helper.utils.UmengUtils
import com.qust.helper.viewmodel.TermLessonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

	private var updateMessage by mutableStateOf("")

	private var mainDrawerEnable by mutableStateOf(true)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// 第一次使用跳转到引导页
		if(Setting.getBoolean(Keys.IS_FIRST_USE, true)) {
			startActivity(Intent(this, GuideActivity::class.java))
			return
		}

		lifecycleScope.launch {
			// 检查更新
			withContext(Dispatchers.IO) {
				UpdateRepository.checkUpdate()
			}?.let { updateMessage = it.message }

			// 自动检查工具
			AutoQueryRepository.startAutoQuery(this@MainActivity.applicationContext)
		}
	}

	@Composable
	override fun Content() {
		val scope = rememberCoroutineScope()
		val drawerState = rememberDrawerState(DrawerValue.Closed)
		val navController = rememberNavController().also {
			it.addOnDestinationChangedListener { _, destination, _ ->
				UmengUtils.route(this.applicationContext, destination.route)
			}
		}

		ModalNavigationDrawer(
			drawerState = drawerState,
			gesturesEnabled = mainDrawerEnable,
			drawerContent = {
				ModalDrawerSheet(modifier = Modifier.requiredWidth((LocalConfiguration.current.screenWidthDp.dp * 0.75F).coerceAtMost(DrawerDefaults.MaximumDrawerWidth))) {
					MainAppDrawer(scope, drawerState, navController)
				}
			}
		) {
			AppWidgets.NavigationHost(navController = navController, startDestination = "home"){
				composable(route = "home") { HomePage(scope, drawerState, navController) }

				for(page in Data.Pages.values) {
					composable(route = page.key) {
						setDrawerEnable(page.enableDrawer)
						Scaffold(
							topBar = {
								AppWidgets.TopBar(title = page.name, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack) {
									navController.popBackStack()
								}
							},
						) { padding ->
							page.content(this@MainActivity, padding, navController)
						}
					}
				}
			}
		}
	}

	@Composable
	fun HomePage(scope: CoroutineScope, drawerState: DrawerState, navController: NavController) {
		val viewModel by viewModels<TermLessonViewModel>()
		Scaffold(
			topBar = {
				AppWidgets.TopBar(title = stringResource(id = R.string.app_name), navigationIcon = Icons.Rounded.Menu) {
					scope.launch { drawerState.open() }
				}
			},
		) { padding ->
			TermLesson.TermLessonUI(padding = padding, uiState = viewModel.uiState, uiEvent = viewModel.uiEvent, toast = toast)

			// 显示检查到更新Dialog
			if(updateMessage.isNotEmpty()){
				Dialogs.AskDialog(title = "更新", "检查到新版本，是否更新？\n$updateMessage", onConfirm = {
					navController.navigate("update"); updateMessage = ""
				}, onDismiss = { updateMessage = "" })
			}
		}
	}

	@Composable
	fun MainAppDrawer(scope: CoroutineScope, drawerState: DrawerState, navController: NavController){
		Column(modifier = Modifier.padding(16.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.app_name),
					modifier = Modifier.padding(16.dp).weight(1F),
					style = MaterialTheme.typography.titleLarge,
					maxLines = 1,
				)
				Image(
					painter = painterResource(id = R.mipmap.ic_launcher),
					contentDescription = null,
					modifier = Modifier.padding(16.dp)
				)
			}
			
			fun clickItem(key: String){
				scope.launch { drawerState.apply { if(isOpen) close() } }
				if(navController.currentDestination?.route != key){
					navController.navigate(key){
						popUpTo(key) { inclusive = true }
						launchSingleTop = true
					}
				}
			}

			Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
				Data.pageGroups.forEachIndexed { index, item ->
					when(item){
						is DrawerPageGroup.Page -> {
							DrawerItem(page = Data.Pages[item.name]!!, color = Color(TEXT_COLORS[index % (TEXT_COLORS.size - 1) + 1]), onClick = { clickItem(it) })
						}

						is DrawerPageGroup.PageGroup -> {
							Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).clickable { item.expand.value = !item.expand.value }){
								Row(modifier = Modifier.padding(16.dp)) {
									Icon(painter = if(item.image != null) rememberVectorPainter(item.image) else painterResource(id = item.iconRes), contentDescription = item.name, tint = Color(TEXT_COLORS[index % (TEXT_COLORS.size - 1) + 1]))
									Text(text = item.name, modifier = Modifier.padding(start = 16.dp).weight(1F))
									Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null, tint = colorSecondaryText, modifier = Modifier.rotate( if(item.expand.value) 180F else 0F ))
								}
							}

							AnimatedVisibility(visible = item.expand.value) {
								Column {
									item.pages.forEachIndexed { index, page ->
										DrawerItem(modifier = Modifier.padding(start = 16.dp), page = Data.Pages[page]!!, color = Color(TEXT_COLORS[index % (TEXT_COLORS.size - 1) + 1]), onClick = { clickItem(it) })
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Composable
	fun DrawerItem(modifier: Modifier = Modifier, page: Page, color: Color, onClick: (String) -> Unit) {
		NavigationDrawerItem(
			label = { Text(text = page.name) },
			selected = false,
			icon = {
				Icon(
					painter = if(page.image != null) rememberVectorPainter(page.image) else painterResource(id = page.iconRes),
					contentDescription = page.name,
					tint = color
				)
			},
			onClick = { onClick(page.key) },
			modifier = modifier
		)
	}

	fun setDrawerEnable(boolean: Boolean){
		mainDrawerEnable = boolean
	}
}

