package com.qust.helper.ui.page

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.ui.activity.BaseActivity
import com.qust.helper.ui.widget.AppBar.TopBar
import com.qust.helper.ui.widget.MainAppDrawer
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi", "StateFlowValueCalledInComposition")
@Composable
fun MainApp(activity: BaseActivity) {

	val navController = rememberNavController()
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = { ModalDrawerSheet { MainAppDrawer(drawerState = drawerState, navController = navController) } },
	) {
		NavHost(
			navController = navController,
			startDestination = "home"
		) {
			composable(route = "home") {
				Scaffold(
					topBar = {
						TopBar(title = stringResource(id = R.string.app_name), navigationIcon = Icons.Rounded.Menu) {
							scope.launch { drawerState.open() }
						}
					},
				) { padding ->
					HomePage.HomePage(activity, padding)
				}
			}

			for(page in Data.Pages.values) {
				composable(
					route = page.key,
					enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween()) },
					exitTransition = { ExitTransition.None },
					popEnterTransition = { EnterTransition.None },
					popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween()) }
				) {
					Scaffold(
						topBar = {
							TopBar(title = page.name, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack) {
								navController.popBackStack()
							}
						},
					) { padding ->
						page.content(activity, padding, navController)
					}
				}
			}
		}
	}
}