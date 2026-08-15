package com.qust.helper.next.repository

import com.qust.helper.next.common.setting.AppSetting
import com.qust.helper.next.entity.DataKeys
import com.qust.helper.next.entity.SettingKeys
import com.qust.helper.next.entity.Strings
import com.qust.helper.next.network.client.EasHttpClient
import com.qust.helper.next.network.client.IPassHttpClient
import com.qust.helper.next.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

/**
 * 账号信息仓库
 */
object AccountRepository {

	val _qustAccountInfo = MutableStateFlow<QUSTAccount>(QUSTAccount())

	val qustAccountInfo: StateFlow<QUSTAccount> = _qustAccountInfo

	/**
	 * 入学年份
	 * eg. 2020
	 */
	var entranceDate: Int = AppSetting[SettingKeys.ENTRANCE_TIME, -1]
		set(date) {
			AppSetting[SettingKeys.ENTRANCE_TIME] = date
			field = date
		}

	/**
	 * 获取当前年级
	 */
	fun getCurrentGrade(): Int {
		val current: LocalDateTime = DateUtils.currentTime()
		val y = current.year
		return if(y < entranceDate) {
			0
		} else {
			((y - entranceDate) * 2 - if(current.month < Month.AUGUST) 1 else 0).coerceAtMost(Strings.ARRAY_TERM_NAME.size - 1)
		}
	}

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
		val result = IPassHttpClient.login(account, password, true)
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
		val result = EasHttpClient.login(account, password, true)
		if(result){
			val year = DateUtils.today().year.toString()
			// 取入学年份（【年份前2位】+【学号前2位】）, eg. 2025 + 20xxxxxx = 2020
			// 理论上前两位写死20也可，75年内不会出问题
			val currentYear = (year.substring(0, year.length - 2) + account.substring(0, 2)).toInt()
			entranceDate = currentYear
			_qustAccountInfo.update { it.copy(easAccount = account) }
			return true
		}else{
			return false
		}
	}


	suspend fun ipassLogout() {
		IPassHttpClient.logout()
		_qustAccountInfo.update { it.copy(ipassAccount = "") }
	}

	suspend fun easLogout() {
		EasHttpClient.logout()
		_qustAccountInfo.update { it.copy(easAccount = "") }
	}

	data class QUSTAccount(
		val ipassAccount: String = AppSetting[DataKeys.IPASS_ACCOUNT, ""],
		val easAccount: String = AppSetting[DataKeys.EAS_ACCOUNT, ""],
	)
}