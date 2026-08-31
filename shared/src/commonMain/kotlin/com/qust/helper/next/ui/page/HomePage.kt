package com.qust.helper.next.ui.page

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qust.helper.next.ui.component.AppPreview
import com.qust.helper.next.ui.component.layout.AppContent
import com.qust.helper.next.ui.page.base.AppPage
import com.qust.helper.next.ui.page.lesson.LessonTablePage
import com.qust.helper.next.ui.theme.AppThemeProvider
import com.qust.helper.next.ui.theme.Theme
import com.qust.helper.next.ui.theme.color.Colors
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch


@Preview
@Composable
private fun HomePreview() = AppPreview(::HomeUI)

class HomePage : AppPage<HomeViewModel>(title = "主页", viewModelClass = HomeViewModel::class) {

	@Composable
	override fun PageContent() {
		AppThemeProvider {
			BaseContent(getViewModel())
		}
	}

	@Composable
	@Suppress("UNCHECKED_CAST")
	override fun PageContent(viewModel: BaseViewModel) {
		AppThemeProvider {
			BaseContent(viewModel as HomeViewModel)
		}
	}

	@Composable
	override fun Content(viewModel: HomeViewModel) = HomeUI(viewModel)
}

@Composable
private fun HomeUI(viewModel: HomeViewModel) {
	val scope = rememberCoroutineScope()
	val pagerState = rememberPagerState(initialPage = 0, pageCount = { viewModel.pages.size })

	AppContent(title = viewModel.pages[pagerState.currentPage].title) {
		HorizontalPager(state = pagerState, modifier = Modifier.weight(1F)) { index ->
			val page = viewModel.pages[index]
			page.BaseContent(page.getViewModel())
		}

		BottomBar(viewModel.pages, pagerState.currentPage){
			scope.launch { pagerState.animateScrollToPage(it) }
		}
	}
}

@Composable
private fun BottomBar(pages: List<AppPage<*>>, currentPage: Int, changePage: (Int) -> Unit) {
	Column(Modifier.fillMaxWidth()) {
		BottomNavigation(backgroundColor = Theme.color.surfaceVariant, elevation = 4.dp) {
			pages.forEachIndexed { i, page ->
				BottomNavigationItem(
					selected = currentPage == i,
					icon = {
						Column(
							modifier = Modifier.padding(top = 4.dp),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							if(page.icon != null) {
								Icon(
									painter = rememberVectorPainter(page.icon),
									contentDescription = page.title,
									tint = Colors.LESSON_TEXT_COLORS[i % Colors.LESSON_TEXT_COLORS.size]
								)
							}
							Text(
								page.title,
								color = Colors.LESSON_TEXT_COLORS[i % Colors.LESSON_TEXT_COLORS.size],
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

class HomeViewModel : BaseViewModel() {

	val pages: List<AppPage<*>> by mutableStateOf(listOf(LessonTablePage(), MyPage()))

}