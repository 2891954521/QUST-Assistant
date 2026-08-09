package com.qust.helper.next.common.exception

/**
 * 需要登陆 Exception
 */
class NeedLoginException: UserDisplayException {

    constructor(message: String) : super(message)

    constructor(message: String, error: Throwable) : super(message, error)
}