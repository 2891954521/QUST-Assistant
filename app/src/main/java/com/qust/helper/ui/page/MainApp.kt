package com.qust.helper.ui.page

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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

@Composable
fun MainApp(activity: ComponentActivity) {

	val navController = rememberNavController()
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	Surface {
		ModalNavigationDrawer(
			drawerState = drawerState,
			drawerContent = { ModalDrawerSheet { MainAppDrawer(drawerState = drawerState) } },
		) {

			Scaffold(
				topBar = {
					TopBar(title = stringResource(id = R.string.app_name), navigationIcon = Icons.Rounded.Menu){
						scope.launch { drawerState.open() }
					}
				},
			) { innerPadding ->
				NavHost(
					navController = navController,
					startDestination = "termLesson",
					modifier = Modifier.fillMaxSize().padding(innerPadding)
				) {
					for(page in Data.Pages.values){
						composable(route = page.key) {
							page.content(activity)
						}
					}
				}
			}
		}
	}
}