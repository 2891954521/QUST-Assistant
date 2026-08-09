package com.qust.helper.next.ui.router

import android.app.Activity
import android.content.Intent
import androidx.activity.OnBackPressedDispatcher
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass

open class AndroidAppRouter(
    val activity: Activity,
    private val dispatcher: OnBackPressedDispatcher?
): BaseAppRouter() {

    override fun <T : BasePage<*>> startPage(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)?) {
        ActivityCollector.startPage(activity, clazz, intent = null, block = block)
    }

    override fun back() {
        dispatcher?.onBackPressed()
    }

    override fun finish() {
        activity.finish()
    }

    override fun finishWithResult(block: PageParamBuilder.() -> Unit) {
        val builder = AndroidPageParamBuilder()
        builder.block()
        activity.setResult(Activity.RESULT_OK, Intent().apply { putExtras(builder.build().bundle) })
        activity.finish()
    }

    override fun finishAllPage() {
        ActivityCollector.finishAllPage()
    }

    override fun <T : BasePage<*>> finishAllAndJumpTo(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)?) {
        ActivityCollector.finishAllAndJumpTo(clazz, block)
    }
}