package com.qust.helper.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qust.helper.ui.theme.LESSON_TEXT_COLORS
import com.qust.helper.ui.theme.LocalColor
import com.qust.helper.ui.widget.layout.AppContent
import com.qust.helper.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

object HomePage: BasePage<HomeViewModel>("主页", Icons.Default.Home) {

	@Composable
	override fun ComposePage() {
		val viewModel = getViewModel()
		val scope = rememberCoroutineScope()
		val pagerState = rememberPagerState(initialPage = 0, pageCount = { viewModel.pages.size })

		Scaffold { contentPadding ->
			AppContent(title = viewModel.pages[pagerState.currentPage].title, contentPadding = contentPadding) {
				HorizontalPager(state = pagerState, modifier = Modifier.weight(1F)) { index ->
					viewModel.pages[index].BaseContent()
				}

				BottomBar(viewModel.pages, pagerState.currentPage){
					scope.launch { pagerState.animateScrollToPage(it) }
				}
			}
		}
	}

	@Composable
	override fun BaseContent() {
		// 这个函数在此处无意义
	}

	@Composable
	override fun Content(viewModel: HomeViewModel) {
		// 这个函数在此处无意义
	}

	@Composable
	fun BottomBar(pages: List<BasePage<*>>, currentPage: Int, changePage: (Int) -> Unit) {
		Column(Modifier.fillMaxWidth()) {
			BottomNavigation(backgroundColor = LocalColor.current.surfaceVariant, elevation = 4.dp) {
				pages.forEachIndexed { i, page ->
					BottomNavigationItem(
						selected = currentPage == i,
						icon = {
							Column(
								modifier = Modifier.padding(top = 4.dp),
								horizontalAlignment = Alignment.CenterHorizontally
							) {
								Icon(
									painter = rememberVectorPainter(page.icon),
									contentDescription = page.title,
									tint = LESSON_TEXT_COLORS[i % LESSON_TEXT_COLORS.size]
								)
								Text(
									page.title,
									color = LESSON_TEXT_COLORS[i % LESSON_TEXT_COLORS.size],
									modifier = Modifier.padding(top = 4.dp),
								)
							}
						},
						onClick = { changePage(i) },
					)
				}
			}
			Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
		}
	}

	@Composable override fun getViewModel() = viewModel<HomeViewModel>()
}