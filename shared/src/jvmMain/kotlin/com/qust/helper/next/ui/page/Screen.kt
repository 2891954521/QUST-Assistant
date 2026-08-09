package com.qust.helper.next.ui.page

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.DesktopPageResultStore
import com.qust.helper.next.ui.router.PageResultKey

/**
 * 屏幕，相当于 Android 端的 Activity
 */
class Screen(
	val id: String,
	val page: BasePage<*>,
	val resultRequest: DesktopResultRequest? = null,
): ViewModelStoreOwner {

    val resultStore = DesktopPageResultStore()

    override val viewModelStore = ViewModelStore()

    fun exit(){
        resultStore.clear()
        viewModelStore.clear()
    }

    data class DesktopResultRequest(
        val callerId: String,
        val resultKey: PageResultKey,
    )
}