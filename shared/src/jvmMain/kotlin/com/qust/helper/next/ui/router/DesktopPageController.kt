package com.qust.helper.next.ui.router

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.page.EmptyPage
import com.qust.helper.next.ui.page.Screen
import com.qust.helper.next.ui.page.Screen.DesktopResultRequest
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.uuid.Uuid

object DesktopPageController: BaseAppRouter() {

    private val _screens = mutableStateListOf<Screen>()

    val screens: List<Screen> get() = _screens

    var askForExit by mutableStateOf(false)

    override fun <T : BasePage<*>> startPage(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)?) {
        val pageInstance: BasePage<*> = try {
            clazz.primaryConstructor?.call() ?: EmptyPage
        }catch(e: Exception){
            Logger.e(e)
            EmptyPage
        }

        if(block != null) pageInstance.params = getPageParamBuilder().also { it.block() }.build()

        val screen = Screen(
            id = Uuid.random().toString(),
            page = pageInstance
        )

        _screens.add(screen)
    }

    fun <P : BasePage<*>> startPageForResult(clazz: KClass<P>, resultKey: PageResultKey, block: (PageParamBuilder.() -> Unit)?, ) {
        val pageInstance: BasePage<*> = try {
            clazz.primaryConstructor?.call() ?: EmptyPage
        }catch(e: Exception){
            Logger.e(e)
            EmptyPage
        }

        if(block != null) pageInstance.params = getPageParamBuilder().also { it.block() }.build()

        val caller = _screens.lastOrNull() ?: error("没有可接收结果的调用方页面")

        val screen = Screen(
            id = Uuid.random().toString(),
            page = pageInstance,
            resultRequest = DesktopResultRequest(callerId = caller.id, resultKey = resultKey)
        )

        _screens.add(screen)
    }

    override fun back() = finish()

    override fun finish() {
        if(_screens.size > 1){
            val removed = _screens.removeLast()

            if(removed.resultRequest != null){
                val request = removed.resultRequest
                _screens.firstOrNull { it.id == request.callerId }?.resultStore?.publish(request.resultKey, PageResult.Canceled)
            }

            removed.exit()
        }else{
            askForExit = true
        }
    }

    override fun finishWithResult(block: PageParamBuilder.() -> Unit) {
        val child = screens.lastOrNull() ?: return
        val request = child.resultRequest ?: error("当前页面不是通过 startPageForResult 打开的")

        val caller = _screens.firstOrNull { it.id == request.callerId }

        _screens.removeLast()
        child.exit()

        if (caller == null) {
            Logger.w("PageResult 调用方已经不存在")
            return
        }

        caller.resultStore.publish(request.resultKey, PageResult.Success(
            getPageParamBuilder().also { it.block() }.build()
        ))
    }

    override fun finishAllPage() {
        var current = _screens.size - 1
        while (current >= 0){
            _screens[current].exit()
            current--
        }
        _screens.clear()
    }
}