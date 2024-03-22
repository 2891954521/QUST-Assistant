package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.qust.helper.model.account.EASAccount
import com.qust.helper.ui.common.ToastContent
import com.qust.helper.ui.common.toastWarning

abstract class BaseEasViewModel(application: Application) : AndroidViewModel(application) {

	protected val easAccount = EASAccount.getInstance()

	val pickYear: MutableState<Int> = mutableIntStateOf(easAccount.getCurrentGrade())

	var dialogText by mutableStateOf("")
	var toastContent = mutableStateOf(ToastContent())

	/**
	 * 获取选择的学期参数
	 */
	protected fun getYearAndTerm(): Pair<String, String>{
		val entranceTime = easAccount.entranceTime
		if(entranceTime == -1) toastContent.value = toastWarning("未设置入学年份，可能导致查询结果异常")
		val pick = pickYear.value
		return Pair(
			(pick / 2 + entranceTime).toString(),
			if(pick % 2 == 0) "3" else "12"
		)
	}
}


