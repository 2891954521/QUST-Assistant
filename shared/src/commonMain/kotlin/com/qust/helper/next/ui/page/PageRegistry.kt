package com.qust.helper.next.ui.page

import androidx.annotation.Keep
import com.qust.helper.next.page.AbstractPage
import com.qust.helper.next.page.AbstractPageRegistry
import com.qust.helper.next.ui.page.base.BasePage
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object PageRegistry: AbstractPageRegistry() {

    private var initialized = false

    private val _pageClasses = mutableMapOf<String, KClass<out AbstractPage>>()

    val pageClasses: Map<String, KClass<out AbstractPage>> by lazy {
        if(!initialized) initialize()
        _pageClasses
    }

    private fun initialize() {
        if (initialized) return

        initialized = true

        try {
            registerPlatformPage(this)
        } catch (throwable: Throwable) {
            initialized = false
            _pageClasses.clear()
            throw throwable
        }
    }

    @Keep
    override fun register(key: String, pageClass: KClass<out AbstractPage>) {
        _pageClasses[key] = pageClass
    }

    @Suppress("UNCHECKED_CAST")
    fun getPage(key: String): KClass<BasePage<*>>? {
        val clazz = pageClasses[key] ?: return null
        return if(clazz.isSubclassOf(BasePage::class)){
            clazz as KClass<BasePage<*>>
        }else{
            null
        }
    }
}

expect fun registerPlatformPage(registry: PageRegistry)
