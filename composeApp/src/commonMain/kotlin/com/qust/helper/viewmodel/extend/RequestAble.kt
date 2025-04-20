package com.qust.helper.viewmodel.extend

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


interface RequestAble {
	suspend fun request(block: suspend RequestAble.() -> Unit)
}

/**
 * 控制联网加载时展示Dialog的类
 */
class RequestsImpl(
	private val dialogAble: LoadingAble,
): RequestAble {

	private var count = 0
	private val mutex = Mutex()

	override suspend fun request(block: suspend RequestAble.() -> Unit){
		try {
			mutex.withLock {
				if(count == 0) dialogAble.showDialog("加载中")
				count++
			}
			block()
		} catch(e: Exception) {
			throw e
		} finally {
			mutex.withLock {
				if(count > 0) {
					count--
					if(count == 0) dialogAble.clearDialog()
				}
			}
		}
	}
}