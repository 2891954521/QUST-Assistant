package com.qust.helper.next.ui.router

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.activity.BasePageActivity
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.params.PageParamBuilder
import kotlin.reflect.KClass


@SuppressLint("StaticFieldLeak")
object ActivityCollector: BaseAppRouter() {

    lateinit var context: Context

    var pageActivityClass: Class<*> = BasePageActivity::class.java

    val activities: MutableList<Activity> = mutableListOf()

    override fun <T : BasePage<*>> startPage(clazz: KClass<T>, block: (PageParamBuilder.() -> Unit)?) {
        startPage(context, clazz, intent = {
            // 由于 ActivityCollector 持有的是 Application Context，因此 startActivity 要加 FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }, block = block)
    }

    override fun back() {
        activities.lastOrNull()?.onBackPressed()
    }

    override fun finish() {
        activities.lastOrNull()?.finish()
    }

    override fun finishWithResult(block: PageParamBuilder.() -> Unit) {
        Logger.w("finishWithResult 不能在 ActivityCollector 中调用")
    }

    override fun finishAllPage() {
        val tmp = activities.map { it }.toList()
        for(activity in tmp) if(!activity.isFinishing) activity.finish()
        activities.clear()
    }

    fun <T : BasePage<*>> startPage(context: Context, clazz: KClass<T>, intent: (Intent.() -> Unit)? = null, block: (PageParamBuilder.() -> Unit)? = null){
        try {
            val intent = Intent(context, pageActivityClass).also {
                if(intent != null) it.intent()
            }

            if(block != null){
                val builder = AndroidPageParamBuilder()
                builder.block()
                intent.putExtras(builder.build().bundle)
            }

            intent.putExtra("class", clazz.java)

            context.startActivity(intent)
        }catch(e: Exception){
            Logger.e(e)
        }
    }

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }
}