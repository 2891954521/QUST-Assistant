package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qust.helper.model.Logger
import com.qust.helper.model.account.EASAccount
import com.qust.helper.model.account.NeedLoginException
import com.qust.helper.ui.widget.DialogAble
import com.qust.helper.ui.widget.DialogAbleImpl
import com.qust.helper.ui.widget.ToastAble
import com.qust.helper.ui.widget.ToastAbleImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseEasViewModel(application: Application) : AndroidViewModel(application), ToastAble by ToastAbleImpl(), DialogAble by DialogAbleImpl() {

	protected val easAccount = EASAccount.getInstance()

	val pickYear = mutableIntStateOf(easAccount.getCurrentGrade())

	var needLogin by mutableStateOf(false)

	/**
	 * 获取选择的学期参数
	 */
	protected fun getYearAndTerm(): Pair<String, String>{
		val entranceTime = easAccount.entranceTime
		if(entranceTime == -1) toastWarning("未设置入学年份，可能导致查询结果异常")
		val pick = pickYear.intValue
		return Pair(
			(pick / 2 + entranceTime).toString(),
			if(pick % 2 == 0) "3" else "12"
		)
	}

	fun request(
		block: suspend EASAccount.() -> Unit,
		final: () -> Unit = { },
		onError: (Exception) -> Unit = {
			toastError("网络错误: ${it.message}")
		}
	) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				try {
					block(easAccount)
				}catch(e: NeedLoginException){
					toastWarning("请先登录")
					needLogin = true
				}catch(e: Exception){
					Logger.e(e)
					onError(e)
				}finally {
					final()
				}
			}
		}
	}
}


