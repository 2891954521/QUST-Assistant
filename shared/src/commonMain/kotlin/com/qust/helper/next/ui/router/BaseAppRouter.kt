package com.qust.helper.next.ui.router

import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.page.EmptyPage
import com.qust.helper.next.ui.page.PageRegistry
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass

abstract class BaseAppRouter {

    /**
     * 打开一个新界面
     */
    inline fun <reified T: BasePage<*>> startPage(noinline block: (PageParamBuilder.() -> Unit)? = null) = startPage(T::class, block)

    /**
     * 打开一个新界面
     */
    fun startPage(key: String, block: (PageParamBuilder.() -> Unit)? = null) = startPage(PageRegistry.getPage(key) ?: EmptyPage::class, block)

    /**
     * 打开一个新界面
     */
    abstract fun <T: BasePage<*>> startPage(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)? = null)

    /**
     * 返回上一级
     */
    open fun back(){
        Logger.w("back is not implement")
    }

    /**
     * 关闭当前Page
     */
    open fun finish(){
        Logger.w("finish is not implement")
    }

    /**
     * 关闭当前Page, 并将数据返回给上一界面
     */
    open fun finishWithResult(block: (PageParamBuilder.() -> Unit)) {
        Logger.w("finishWithResult is not implement")
    }

    /**
     * 关闭所有Page，相当于退出应用
     */
    open fun finishAllPage(){
        Logger.w("finishAllPage is not implement")
    }

    /**
     * 关闭所有其他Page并跳转到一个新Page
     */
    inline fun <reified T: BasePage<*>> finishAllAndJumpTo(noinline block: (PageParamBuilder.() -> Unit)? = null) = finishAllAndJumpTo(T::class, block)

    /**
     * 关闭所有其他Page并跳转到一个新Page
     */
    open fun <T: BasePage<*>> finishAllAndJumpTo(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)? = null){
        finishAllPage()
        startPage(clazz, block)
    }

}