package com.qust.helper.ui.widget

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.qust.helper.data.Keys
import com.qust.helper.data.Page
import com.qust.helper.model.account.EASAccount
import com.qust.helper.viewmodel.eas.BaseEasViewModel

object AppWidgets{

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun TopBar(
		title: String,
		navigationIcon: ImageVector? = null,
		navigationClick: () -> Unit = { },
	) {
		if(navigationIcon == null){
			TopAppBar(
				title = { TitleText(title = title) }
			)
		}else{
			TopAppBar(
				title = { TitleText(title = title) },
				navigationIcon = {
					BackIcon(icon = navigationIcon) {
						navigationClick()
					}
				}
			)
		}
	}

	@Composable
	fun TitleText(title: String){
		Text(
			text = title,
			modifier = Modifier.padding(16.dp),
			style = MaterialTheme.typography.titleLarge,
			maxLines = 1,
		)
	}

	@Composable
	fun BackIcon(icon: ImageVector, onClick: () -> Unit){
		Box(modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(32.dp)).clickable { onClick() }){
			Icon(
				imageVector = icon,
				contentDescription = "Back",
				modifier = Modifier.padding(8.dp)
			)
		}
	}

	@Composable
	fun DialogBar(dialogText: String){
		if(dialogText.isNotEmpty()) IndeterminateProgressDialog(dialogText)
	}

	@Composable
	fun CheckEasLogin(viewModel: BaseEasViewModel, navController: NavController){
		LaunchedEffect(viewModel.needLogin){
			if(viewModel.needLogin) {
				if(EASAccount.useVpn){
					navController.navigate(Keys.Page.VpnLoginPage)
				}else{
					navController.navigate(Keys.Page.EasLogin)
				}
				viewModel.needLogin = false
			}
		}
	}

	@Composable
	fun NavigationHost(navController: NavHostController, startDestination: String, modifier: Modifier = Modifier, content: NavGraphBuilder.() -> Unit) {
		NavHost(
			navController = navController,
			startDestination = startDestination,
			modifier = modifier,
			enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween()) },
			exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween()) },
			popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween()) },
			popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween()) },
			builder = { content() }
		)
	}

	@Composable
	fun DrawerItem(modifier: Modifier = Modifier, page: Page, color: Color, onClick: (String) -> Unit) {
		NavigationDrawerItem(
			selected = false,
			label = { Text(text = page.name) },
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
}