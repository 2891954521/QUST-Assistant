package com.qust.helper.data.i18n

import androidx.compose.ui.text.intl.Locale
import com.qust.helper.Res
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

open class I18nStrings {

	open val TEXT_OK = "确定"
	open val TEXT_CANCEL = "取消"

	open val MSG_NEED_LOGIN: String = "需要登录"

	open val MSG_ERROR_ACCOUNT: String = "用户名或密码错误"

	open val MSG_ERROR_LOGIC: String = "逻辑错误"


}

@OptIn(ExperimentalResourceApi::class)
class I18nStringBuilder(
	/** ISO 639 语言代码 */
	val code: String = Locale.current.language
){
	init {
		GlobalScope.launch {
			Res.readBytes("/i18n/strings_${code}.properties")
		}
	}
}

val Strings: I18nStrings = I18nStrings()