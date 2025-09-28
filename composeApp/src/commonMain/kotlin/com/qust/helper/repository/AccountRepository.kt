package com.qust.helper.repository

import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.account.IPassAccount
import com.qust.helper.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 账号信息仓库
 */
object AccountRepository {

	val _qustAccountInfo = MutableStateFlow<QUSTAccount>(QUSTAccount())

	val qustAccountInfo: StateFlow<QUSTAccount> = _qustAccountInfo

	/**
	 * 刷新账号信息
	 */
	fun refreshQustAccountInfo() {
		_qustAccountInfo.value = QUSTAccount()
	}

	/**
	 * 智慧青科大登录
	 * @param account 学号
	 * @param password 密码
	 * @return 是否登录成功
	 */
	suspend fun ipassLogin(account: String, password: String): Boolean {
		val result = IPassAccount.login(account, password, true)
		if(result){
			_qustAccountInfo.update { it.copy(ipassAccount = account) }
			return true
		}else{
			return false
		}
	}

	/**
	 * 教务系统登录
	 * @param account 学号
	 * @param password 密码
	 * @return 是否登录成功
	 */
	suspend fun easLogin(account: String, password: String): Boolean {
		val result = EasAccount.login(account, password, true)
		if(result){
			val year = DateUtils.today().year.toString()
			// 取入学年份（【年份前2位】+【学号前2位】）, eg. 2025 + 20xxxxxx = 2020
			// 理论上前两位写死20也可，75年内不会出问题
			val currentYear = (year.substring(0, year.length - 2) + account.substring(0, 2)).toInt()
			EasAccount.entranceDate = currentYear
			_qustAccountInfo.update { it.copy(easAccount = account) }
			return true
		}else{
			return false
		}
	}


	suspend fun ipassLogout() {
		IPassAccount.logout()
		_qustAccountInfo.update { it.copy(ipassAccount = "") }
	}

	suspend fun easLogout() {
		EasAccount.logout()
		_qustAccountInfo.update { it.copy(easAccount = "") }
	}
	
	data class QUSTAccount(
		val ipassAccount: String = IPassAccount.getAccountName(),
		val easAccount: String = EasAccount.getAccountName(),
	)
}