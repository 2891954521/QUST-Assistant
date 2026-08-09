package com.qust.helper.next.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.common.utils.AndroidUtils
import com.qust.helper.next.ui.component.Windows
import com.qust.helper.next.ui.page.base.BasePage
import com.qust.helper.next.ui.page.base.BasePage.PageParamKey
import com.qust.helper.next.ui.page.EmptyPage
import com.qust.helper.next.ui.router.AndroidAppRouter
import com.qust.helper.next.ui.router.IntentPageParam
import com.qust.helper.next.ui.router.LocalRouter
import com.qust.helper.next.ui.router.params.EmptyPageParam
import com.qust.helper.next.ui.viewmodel.AppViewModelFactory
import com.qust.helper.next.ui.viewmodel.BaseViewModel
import java.io.Serializable

open class BasePageActivity: BaseActivity() {

    lateinit var contentPage: BasePage<*>

    lateinit var viewModel: BaseViewModel

    override val defaultViewModelCreationExtras: CreationExtras
        get() = (super.defaultViewModelCreationExtras as MutableCreationExtras).also { extras ->
            extras[PageParamKey] = intent.extras?.let { IntentPageParam(it) } ?: EmptyPageParam
        }

    private val router = AndroidAppRouter(this, onBackPressedDispatcher)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        contentPage = getPage()

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, AppViewModelFactory)[contentPage.viewModelClass]

        setContent {
            val window = LocalWindowInfo.current

            CompositionLocalProvider(
                Windows.LocalWindowsPixels provides window.containerSize,
                Windows.LocalWindowsDpSize provides window.containerDpSize,
                Windows.LocalWindowsSize provides WindowSizeClass.calculateFromSize(window.containerDpSize),
                LocalRouter provides router
            ) {
                contentPage.PageContent(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val param = intent.extras?.let { IntentPageParam(it) } ?: EmptyPageParam
        contentPage.params = param
        if (::viewModel.isInitialized) viewModel.onCreate(param)
    }

    open fun getPage(): BasePage<*> {
        val clazz = AndroidUtils.getSerializableExtra<Serializable>(intent, "class")

        if(clazz !is Class<*>) return EmptyPage

        if(!BasePage::class.java.isAssignableFrom(clazz)) {
            Logger.w("页面不是BasePage的子类：${clazz}")
            return EmptyPage
        }

        val page = try {
            clazz.getDeclaredConstructor().newInstance() as BasePage<*>
        }catch(e: Exception){
            Logger.e(e)
            return EmptyPage
        }

        intent.extras?.let {
            page.params = IntentPageParam(it)
        }

        return page
    }
}