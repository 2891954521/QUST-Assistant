package com.qust.helper.ui.page

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qust.helper.R
import com.qust.helper.data.Data
import com.qust.helper.ui.common.AppBar.TopBar
import com.qust.helper.ui.common.MainAppDrawer
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi", "StateFlowValueCalledInComposition")
@Composable
fun MainApp(activity: ComponentActivity) {

	val navController = rememberNavController()
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	Surface {
		ModalNavigationDrawer(
			drawerState = drawerState,
			drawerContent = { ModalDrawerSheet() { MainAppDrawer(drawerState = drawerState, navController = navController) } },
		) {
			NavHost(
				navController = navController,
				startDestination = "home"
			) {
				composable(route = "home"){
					Scaffold(
						topBar = {
							TopBar(title = stringResource(id = R.string.app_name), navigationIcon = Icons.Rounded.Menu) {
								scope.launch { drawerState.open() }
							}
						},
					) { padding ->
						Box(modifier = Modifier.padding(padding)){
							HomePage.HomePage(activity)
						}
					}
				}

				for(page in Data.Pages.values) {
					composable(
						route = page.key,
						enterTransition = {
							slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween())
					    },
						exitTransition = { ExitTransition.None },
						popEnterTransition = { EnterTransition.None },
						popExitTransition = {
//							if(initialState.destination.route == page.key) {
								slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween())
//							}else null
						}
					) {
						Scaffold(
							topBar = {
								TopBar(title = page.name, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack){
									navController.popBackStack()
								}
							},
						) { padding ->
							Box(modifier = Modifier.padding(padding)){
								page.content(activity)
							}
						}
					}
				}
			}
		}
	}
}