package com.qust.helper.data

import android.os.Bundle
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import com.qust.helper.ui.activity.BaseActivity

/**
 * App界面
 * @param key
 * @param name         显示的标题
 * @param iconRes      图标
 * @param image        图片类型的图标
 * @param enableDrawer 这个界面是否允许侧滑
 */
open class Page(
	val key: String,
	val name: String,
	val iconRes: Int = 0,
	val image: ImageVector? = null,
	val enableDrawer: Boolean = true,
	val arguments: List<NamedNavArgument> = emptyList(),
	val content: @Composable (BaseActivity, PaddingValues, NavController, Bundle?) -> Unit = { _, _, _, _ -> }
){
	companion object{
		fun navigate(navController: NavController, key: String) {
			if(navController.currentDestination?.route != key) {
				navController.navigate(key) {
					popUpTo(key) { inclusive = true }
					launchSingleTop = true
				}
			}
		}
	}

}

sealed interface DrawerPageGroup {

	class Page(val name: String): DrawerPageGroup

	class PageGroup(val name: String, val iconRes: Int = 0, val image: ImageVector? = null, val pages: Array<String>): DrawerPageGroup {
		val expand = mutableStateOf(false)
	}
}