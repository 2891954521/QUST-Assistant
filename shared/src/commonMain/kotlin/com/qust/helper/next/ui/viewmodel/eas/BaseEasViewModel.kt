package com.qust.helper.next.ui.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.qust.helper.next.repository.AccountRepository
import com.qust.helper.next.ui.viewmodel.BaseViewModel

open class BaseEasViewModel: BaseViewModel() {

	var pickYear by mutableIntStateOf(AccountRepository.getCurrentGrade())

	protected fun getPickTerm(): Pair<String, String>{
		val entranceDate = AccountRepository.entranceDate
		if(entranceDate == -1) toastWarning("未设置入学年份，可能导致查询结果异常")
		val pick = pickYear
		return getTermParam(entranceDate, pick)
	}

	/**
	 * 计算查询的学期参数
	 */
	private fun getTermParam(entranceTime: Int, pickYear: Int): Pair<String, String>{
		return Pair(
			(pickYear / 2 + entranceTime).toString(),
			if(pickYear % 2 == 0) "3" else "12"
		)
	}

}