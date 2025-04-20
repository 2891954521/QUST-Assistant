package com.qust.helper.viewmodel

import com.qust.helper.viewmodel.extend.RequestAble
import com.qust.helper.viewmodel.extend.RequestsImpl
import com.qust.helper.viewmodel.extend.toastError

open class RequestViewModel: BaseViewModel() {

	val request = RequestsImpl(this)

	fun request(
		block: suspend RequestAble.() -> Unit,
		onError: (Exception) -> Unit = {
			it.printStackTrace()
			toastError("网络错误: ${it.message}")
		}
	) {
		runBackGround {
			try {
				request.request(block)
			}catch(e: Exception){
				onError(e)
			}
		}
	}
}

