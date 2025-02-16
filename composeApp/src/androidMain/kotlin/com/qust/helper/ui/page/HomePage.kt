package com.qust.helper.ui.page

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qust.helper.data.Data
import com.qust.helper.ui.activity.MainActivity
import com.qust.helper.ui.theme.TEXT_COLORS
import com.qust.helper.ui.colorSecondaryText
import com.qust.helper.ui.widget.AppWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object HomePage {

	@OptIn(ExperimentalFoundationApi::class)
	@Composable
	fun HomePage(activity: MainActivity, scope: CoroutineScope, drawerState: DrawerState, navController: NavController) {

		val pagerState = rememberPagerState(initialPage = 0, pageCount = { Data.bottomPage.size })

		Scaffold(
			topBar = {
				AppWidgets.TopBar(title = Data.bottomPage[pagerState.currentPage].name, navigationIcon = Icons.Rounded.Menu) {
					scope.launch { drawerState.open() }
				}
			},
			bottomBar = {
				Column(Modifier.fillMaxWidth()) {
					BottomNavigation(backgroundColor = MaterialTheme.colorScheme.surfaceVariant, elevation = 0.dp) {
						Data.bottomPage.forEachIndexed { i, page ->
							BottomNavigationItem(
								selected = pagerState.currentPage == i,
								icon = {
									Column(
										modifier = Modifier.padding(top = 4.dp),
										horizontalAlignment = Alignment.CenterHorizontally
									) {
										Icon(
											painter = if(page.image != null) rememberVectorPainter(page.image) else painterResource(id = page.iconRes),
											contentDescription = page.name,
											tint = Color(TEXT_COLORS[i % (TEXT_COLORS.size - 1) + 1])
										)
										Text(
											page.name,
											modifier = Modifier.padding(top = 4.dp),
											color = if(pagerState.currentPage == i) Color(TEXT_COLORS[i % (TEXT_COLORS.size - 1) + 1])
											else colorSecondaryText
										)
									}
							   },
								onClick = { scope.launch { pagerState.animateScrollToPage(i) } }
							)
						}
					}
					Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
				}
			},
		) { padding ->
			HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
				Data.bottomPage[index].content(activity, padding, navController, Bundle.EMPTY)
			}
		}
	}

}