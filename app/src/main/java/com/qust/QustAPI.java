package com.qust;

/**
 * 教务API
 */
public class QustAPI{
	

	
	/**
	 * 智慧青科大HOST
	 */
	public static final String VPN_HOST = "wvpn.qust.edu.cn";
	
	/**
	 * 智慧青科大登录入口
	 * <p>
	 * Get:
	 * <p>
	 * - 登录界面
	 * <p>
	 * Post:
	 * <p>
	 * - ul: 	用户名长度
	 * <p>
	 * - pl: 	密码长度
	 * <p>
	 * - lt:	HTML里拿
	 * <p>
	 * - rsa:	加密后的用户名密码
	 * <p>
	 * - execution: e1s1
	 * <p>
	 * - _eventId: submit
	 */
	public static final String VPN_LOGIN = "https://wvpn.qust.edu.cn/";
	
	
	/**
	 * 教务系统HOST
	 */
	public static final String[] EA_HOSTS = {
			"jwglxt.qust.edu.cn",
			"jwglxt1.qust.edu.cn",
			"jwglxt2.qust.edu.cn",
			"jwglxt3.qust.edu.cn",
			"jwglxt4.qust.edu.cn",
			"jwglxt5.qust.edu.cn",
			"jwglxt6.qust.edu.cn",
	};
	
	/**
	 * 教务登录 	<p>
	 * Get: 		<p>
	 * - 登录界面 	<p>
	 * Post: 		<p>
	 * - csrftoken: HTML里拿
	 * <p>
	 * - language: 	zh_CN
	 * <p>
	 * - yhm:		用户名
	 * <p>
	 * - mm:		RSA加密后的密码
	 */
	public static final String EA_LOGIN = "jwglxt/xtgl/login_slogin.html";
	
	/**
	 * 教务登录，获取RSA公钥
	 */
	public static final String EA_LOGIN_PUBLIC_KEY = "jwglxt/xtgl/login_getPublicKey.html";
	
	
	/**
	 * 教务系统消息查询
	 * <p>
	 * Post:
	 * <p>
	 * - queryModel.showCount: 一页显示几条
	 * <p>
	 * - queryModel.currentPage: 第几页
	 * <p>
	 * - queryModel.sortName: cjsj
	 * <p>
	 * - queryModel.sortOrder: desc
	 */
	public static final String EA_SYSTEM_NOTICE = "jwglxt/xtgl/index_cxDbsy.html?doType=query";
	
	/**
	 * 学年信息
	 */
	public static final String EA_YEAR_DATA = "jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index";
	
	/**
	 * 查询学生课表
	 */
	public static final String GET_LESSON_TABLE = "jwglxt/kbcx/xskbcx_cxXsgrkb.html";
	
	/**
	 * 查询班级课表
	 */
	public static final String GET_CLASS_LESSON_TABLE = "jwglxt/kbdy/bjkbdy_cxBjKb.html";
	
	/**
	 * 推荐课表打印页面
	 */
	public static final String RECOMMENDED_LESSON_TABLE_PRINTING = "jwglxt/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=0&layout=default";
	
	
	/**
	 * 成绩查询
	 */
	public static final String GET_MARK = "jwglxt/cjcx/cjcx_cxDgXscj.html?doType=query";
	
	/**
	 * 成绩明细查询
	 */
	public static final String GET_MARK_DETAIL = "jwglxt/cjcx/cjcx_cxXsKccjList.html";
	
	
	/**
	 * 考试查询
	 */
	public static final String GET_EXAM = "jwglxt/kwgl/kscx_cxXsksxxIndex.html?doType=query";
	
	
	/**
	 * 学业情况查询界面
	 */
	public static final String ACADEMIC_PAGE = "jwglxt/xsxy/xsxyqk_cxXsxyqkIndex.html?gnmkdm=N105515&layout=default";
	
	/**
	 * 学业情况查询 - 课程信息
	 */
	public static final String ACADEMIC_INFO = "jwglxt/xsxy/xsxyqk_cxJxzxjhxfyqKcxx.html";
}
