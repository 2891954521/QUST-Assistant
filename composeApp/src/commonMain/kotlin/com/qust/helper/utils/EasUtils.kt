package com.qust.helper.utils

object EasUtils {

	/**
	 * 计算查询的学期参数
	 */
	fun getTermParam(entranceTime: Int, pickYear: Int): Pair<String, String>{
		return Pair(
			(pickYear / 2 + entranceTime).toString(),
			if(pickYear % 2 == 0) "3" else "12"
		)
	}
}