package com.qust.helper.data.api

object QustApi {
	/**
	 * 智慧青科大VPN HOST
	 */
	const val VPN_HOST = "wvpn.qust.edu.cn"

	/**
	 * 智慧青科大VPN登录入口
	 *
	 * Get:
	 * - 登录界面
	 *
	 * Post:
	 * - ul: 	用户名长度
	 * - pl: 	密码长度
	 * - lt:	HTML里拿
	 * - rsa:	加密后的用户名密码
	 * - execution: e1s1
	 * - _eventId: submit
	 */
	const val VPN_LOGIN = "https://wvpn.qust.edu.cn/"

	/**
	 * 智慧青科大 HOST
	 */
	const val IPASS_HOST = "ipass.qust.edu.cn"

	/**
	 * 智慧青科大登录入口
	 *
	 * Get:
	 * - 登录界面
	 *
	 * Post:
	 * - ul: 	用户名长度
	 * - pl: 	密码长度
	 * - lt:	HTML里拿
	 * - rsa:	加密后的用户名密码
	 * - execution: e1s1
	 * - _eventId: submit
	 */
	const val IPASS_LOGIN = "http://ipass.qust.edu.cn/tpass/login/"

	/**
	 * 教务系统HOST
	 */
	val EA_HOSTS = arrayOf(
		"jwglxt.qust.edu.cn",
		"jwglxt1.qust.edu.cn",
		"jwglxt2.qust.edu.cn",
		"jwglxt3.qust.edu.cn",
		"jwglxt4.qust.edu.cn",
		"jwglxt5.qust.edu.cn",
		"jwglxt6.qust.edu.cn"
	)


	/**
	 * 教务登录
	 *
	 * Get:
	 * - 登录界面
	 *
	 * Post:
	 * - csrftoken: HTML里拿
	 * - language: 	zh_CN
	 * - yhm:		用户名
	 * - mm:		RSA加密后的密码
	 */
	const val EA_LOGIN = "jwglxt/xtgl/login_slogin.html"

	/**
	 * 教务登录，获取RSA公钥
	 */
	const val EA_LOGIN_PUBLIC_KEY = "jwglxt/xtgl/login_getPublicKey.html"

	/**
	 * 教务系统消息查询
	 *
	 * Post:
	 * - queryModel.showCount: 一页显示几条
	 * - queryModel.currentPage: 第几页
	 * - queryModel.sortName: cjsj
	 * - queryModel.sortOrder: desc
	 */
	const val EA_SYSTEM_NOTICE = "jwglxt/xtgl/index_cxDbsy.html?doType=query"

	/**
	 * 学年信息
	 */
	const val EA_YEAR_DATA = "jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index"

	/**
	 * 查询学生课表
	 */
	const val GET_LESSON_TABLE = "jwglxt/kbcx/xskbcx_cxXsgrkb.html"

	/**
	 * 查询班级课表
	 */
	const val GET_CLASS_LESSON_TABLE = "jwglxt/kbdy/bjkbdy_cxBjKb.html"

	/**
	 * 推荐课表打印页面
	 */
	const val RECOMMENDED_LESSON_TABLE_PRINTING = "jwglxt/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=0&layout=default"

	/**
	 * 成绩查询
	 */
	const val GET_MARK = "jwglxt/cjcx/cjcx_cxXsgrcj.html?doType=query"

	/**
	 * 成绩明细查询
	 */
	const val GET_MARK_DETAIL = "jwglxt/cjcx/cjcx_cxXsKccjList.html"

	/**
	 * 考试查询
	 */
	const val GET_EXAM = "jwglxt/kwgl/kscx_cxXsksxxIndex.html?doType=query"

	/**
	 * 学业情况查询界面
	 */
	const val ACADEMIC_PAGE = "jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&layout=default"

	/**
	 * 学业情况查询 - 课程信息
	 */
	const val ACADEMIC_INFO = "jwglxt/xsxy/xsxyqk_cxJxzxjhxfyqKcxx.html"

}
