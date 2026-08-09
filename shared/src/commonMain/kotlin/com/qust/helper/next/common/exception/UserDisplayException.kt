package com.qust.helper.next.common.exception

/**
 * 用于给用户展示的 Exception
 */
open class UserDisplayException: RuntimeException {

	constructor(message: String) : super(message)

	constructor(message: String, error: Throwable) : super(message, error)
}