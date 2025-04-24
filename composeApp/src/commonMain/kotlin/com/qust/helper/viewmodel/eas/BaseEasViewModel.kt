package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.mutableIntStateOf
import com.qust.helper.model.account.EasAccount
import com.qust.helper.utils.EasUtils
import com.qust.helper.viewmodel.RequestViewModel
import com.qust.helper.viewmodel.extend.toastWarning

open class BaseEasViewModel: RequestViewModel() {

	val pickYear = mutableIntStateOf(EasAccount.getCurrentGrade())

	protected fun getPickTerm(): Pair<String, String>{
		val entranceDate = EasAccount.entranceDate
		if(entranceDate == -1) toastWarning("未设置入学年份，可能导致查询结果异常")
		val pick = pickYear.intValue
		return EasUtils.getTermParam(entranceDate, pick)
	}

}