package com.qust.helper.next.module.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.qust.helper.next.common.exception.UserDisplayException
import com.qust.helper.next.common.log.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * WebView js运行器，用于向webview注入和执行js
 * @param defaultTimeout 默认超时时间，单位毫秒
 * @param onError 调用发生错误时的回调，（回调的返还值会作为调用结果）
 * @param onResultIsEmpty 调用结果为空时的回调，（回调的返还值会作为调用结果）
 */
class WebViewJsRunner(
	val jsInterface: JsInterface = JsInterface(),
	val defaultTimeout: Long = 10_000,
    val onError: (String?) -> String = { """{ "code": 400, "msg": "$it"}""" },
	val onResultIsEmpty: () -> String = { """{ "code": 400, "msg": "result is empty"}""" }
) {

	init {
		jsInterface.runner = this
	}

    val callbacks = ConcurrentHashMap<String, Continuation<String>>()

	/**
	 * 执行js方法
	 * @return js方法返回值
	 */
	@Throws(UserDisplayException::class)
	suspend fun execJs(webView: WebView, method: String, params: Array<out Any?>): String? {
		Logger.d("runner execJs: $method, params: ${params.joinToString()}")
		val id = UUID.randomUUID().toString()
		val result = withTimeoutOrNull(defaultTimeout) {
			suspendCancellableCoroutine { continuation ->
				callbacks[id] = continuation

				webView.post {
					val sb = StringBuilder("javascript: $method(\"$id\",")
					params.forEach {
						when(it) {
							is Int, Float, Double -> sb.append(it.toString())

							null -> sb.append("null")

							is List<*> -> {
								sb.append("[")
								sb.append(it.joinToString(",") { item ->
									when(item){
										is Int, Float, Double -> item.toString()
										is String -> "'$item'"
                                        else -> "'$item'"
									}
								})
								sb.append(']')
							}
							else -> {
								sb.append('\'')
								sb.append(it.toString())
								sb.append('\'')
							}
						}
						sb.append(',')
					}
					sb.append(')')

					Logger.d("exec: $sb")

					webView.evaluateJavascript(sb.toString(), null)
				}
			}
		}
		if(result == null) throw UserDisplayException("请求超时")
		return result
	}


	fun callFinish(id: String, result: String?) {
		Logger.d("call result: $result")
		callbacks[id]?.resume(result ?: onResultIsEmpty())
		callbacks.remove(id)
	}

	fun callError(id: String, result: String?){
		Logger.d("call error: $result")
		callbacks[id]?.resume(onError(result))
		callbacks.remove(id)
	}

    fun injectJsRunner(webView: WebView) {
	    webView.addJavascriptInterface(jsInterface, "android")
    }

	open class JsInterface {
		lateinit var runner: WebViewJsRunner

		@JavascriptInterface
		open fun jsCallFinish(id: String, result: String?) {
			runner.callFinish(id, result)
		}

		@JavascriptInterface
		open fun jsCallError(id: String, result: String?){
			runner.callError(id, result)
		}
	}
}