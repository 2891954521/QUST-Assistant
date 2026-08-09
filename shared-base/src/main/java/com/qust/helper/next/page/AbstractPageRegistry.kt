package com.qust.helper.next.page

import androidx.annotation.Keep
import kotlin.reflect.KClass

abstract class AbstractPageRegistry {

    @Keep
    abstract fun register(key: String, pageClass: KClass<out AbstractPage>)
}