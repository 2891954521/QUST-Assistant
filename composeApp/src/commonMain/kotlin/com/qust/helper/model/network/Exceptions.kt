package com.qust.helper.model.network

import com.qust.helper.data.i18n.Strings

class NeedLoginException : RuntimeException(Strings.MSG_NEED_LOGIN)

class WrongAccountException: RuntimeException(Strings.MSG_ERROR_ACCOUNT)

class WrongLogicException(msg: String) : RuntimeException(Strings.MSG_ERROR_LOGIC + ": $msg")