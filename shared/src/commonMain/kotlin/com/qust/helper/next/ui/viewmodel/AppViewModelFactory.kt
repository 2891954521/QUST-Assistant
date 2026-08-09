package com.qust.helper.next.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.router.params.EmptyPageParam
import com.qust.helper.next.ui.router.params.PageParam
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object AppViewModelFactory : ViewModelProvider.Factory {

    override fun <T: ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        if (!modelClass.isSubclassOf(BaseViewModel::class)) {
            throw IllegalArgumentException("ViewModel $modelClass 未继承自 BaseViewModel，无法创建实例")
        }

        val params = extras[BasePage.PageParamKey] ?: EmptyPageParam

        val constructor = modelClass.primaryConstructor
            ?: throw IllegalArgumentException("ViewModel $modelClass 缺少主构造函数")

        val args = buildMap<KParameter, Any?> {
            for (parameter in constructor.parameters) {
                when {
                    parameter.type.classifier == PageParam::class -> put(parameter, params)
                    parameter.isOptional -> { }
                    else -> throw IllegalArgumentException("ViewModel $modelClass 的构造函数参数 ${parameter.name}（${parameter.type}）无法自动注入")
                }
            }
        }

        val viewModel = constructor.callBy(args)

        (viewModel as BaseViewModel).onCreate(params)

        return viewModel
    }
}