package com.qust.helper.next.ui.viewmodel

import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.log.Logger
import com.qust.helper.next.ui.component.overlay.OverlayEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


interface AsyncAble {

    /**
     * 异步执行 block 的内容
     *
     * 当 block 抛出异常时会弹出错误消息
     */
    fun runInBackground(block: suspend CoroutineScope.() -> Unit)

    /**
     * 异步执行 block 的内容
     *
     * 异步执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 当 block 抛出异常时会弹出错误消息
     */
    fun loading(msg: String = "加载中", block: suspend CoroutineScope.() -> Unit)

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时会弹出错误消息，同时函数返还 null
     */
    suspend fun <T> loadingSuspend(msg: String = "加载中", block: suspend () -> T?): T?

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时会弹出错误消息，同时函数直接返还 default 的值
     *
     * @param default block 抛出异常时的默认值
     */
    suspend fun <T> loadingSuspend(msg: String = "加载中", default: T, block: suspend () -> T): T

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时，执行 onError 函数，函数返还两个值：是否覆盖默认异常处理，默认值
     *
     * @param onError block 抛出异常时的回调函数
     */
    suspend fun <T> loadingSuspend(msg: String = "加载中", onError: (Throwable) -> Pair<Boolean, T>, block: suspend () -> T): T


    fun handleException(catchBy: String?, e: Throwable)
}


open class AsyncAbleImpl(
    val scope: CoroutineScope,
    val overlay: OverlayEventBus
): AsyncAble {

    /**
     * 异步执行 block 的内容
     *
     * 当 block 抛出异常时会弹出错误消息
     */
    override fun runInBackground(block: suspend CoroutineScope.() -> Unit){
        scope.launch {
            withContext(Dispatchers.IO){
                try {
                    block()
                }catch(e: Throwable){
                    handleException("BaseAsyncAble.runInBackground", e)
                }
            }
        }
    }

    /**
     * 异步执行 block 的内容
     *
     * 异步执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 当 block 抛出异常时会弹出错误消息
     */
    override fun loading(msg: String, block: suspend CoroutineScope.() -> Unit){
        scope.launch {
            showLoading(msg)
            withContext(Dispatchers.IO){
                try {
                    block()
                }catch(e: Throwable){
                    handleException("BaseAsyncAble.loading", e)
                }
            }
            clearLoading()
        }
    }

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时会弹出错误消息，同时函数返还 null
     */
    override suspend fun <T> loadingSuspend(msg: String, block: suspend () -> T?): T? {
        showLoading(msg)
        try {
            return block()
        }catch(e: Throwable){
            this.handleException("BaseAsyncAble.loadingSuspend", e)
            return null
        } finally {
            clearLoading()
        }
    }

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时会弹出错误消息，同时函数直接返还 default 的值
     *
     * @param default block 抛出异常时的默认值
     */
    override suspend fun <T> loadingSuspend(msg: String, default: T, block: suspend () -> T): T {
        showLoading(msg)
        try {
            return block()
        }catch(e: Throwable){
            this.handleException("BaseAsyncAble.loadingSuspend", e)
            return default
        } finally {
            clearLoading()
        }
    }

    /**
     * 在协程中执行 block 的内容
     *
     * 执行过程中会展示一个内容为 msg 的 loading 框
     *
     * 函数的返还值与 block 的返还值相同
     *
     * 当 block 抛出异常时，执行 onError 函数，函数返还两个值：是否覆盖默认异常处理，默认值
     *
     * @param onError block 抛出异常时的回调函数
     */
    override suspend fun <T> loadingSuspend(msg: String, onError: (Throwable) -> Pair<Boolean, T>, block: suspend () -> T): T {
        showLoading(msg)
        try {
            return block()
        }catch(e: Throwable){
            val (isHandled, defValue) = onError(e)
            if(!isHandled) this.handleException("BaseAsyncAble.loadingSuspend", e)
            return defValue
        } finally {
            clearLoading()
        }
    }


    override fun handleException(catchBy: String?, e: Throwable){
        val catchBy = catchBy ?: "BaseAsyncAble.handleException"
        when(e){
            is UserDisplayException -> {
                e.cause?.let { Logger.e(catchBy, it) }

                val message = e.message
                if(message != null){
                    overlay.toastError(message)
                }else{
                    overlay.toastError("应用发生错误")
                }
            }

            is IllegalStateException -> {
                if(e.message?.startsWith("A migration from") == true){
                    overlay.toastError("应用数据异常，请前往设置清空应用数据后再使用")
                }else{
                    Logger.e(catchBy, e)
                    overlay.toastError("应用发生错误")
                }
            }

            else -> {
                Logger.e(catchBy, e)
                overlay.toastError("应用发生错误")
            }
        }
    }


    private fun showLoading(message: String) = overlay.showLoading(message)

    private fun clearLoading() = overlay.clearLoading()
}