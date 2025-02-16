package com.qust.helper.utils

import android.content.Context
import com.qust.helper.BuildConfig
import com.qust.helper.data.Keys
import com.qust.helper.data.Setting
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

/**
 * 友盟的工具类
 */
object UmengUtils {

	private var currentPage: String? = null

	fun init(context: Context){
		if(BuildConfig.UMENG_APP_KEY.isNotEmpty()){
			UMConfigure.setLogEnabled(BuildConfig.DEBUG)
			UMConfigure.preInit(context.applicationContext, BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_APP_CHANNEL)
			if(!Setting.getBoolean(Keys.IS_FIRST_USE, true)) {
				UMConfigure.init(context.applicationContext, BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_APP_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, "")
				MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
			}
		}
	}

	/**
	 * 页面路由监听
	 */
	fun route(context: Context, name: String?){
		if(currentPage == name || name == "home") return

		if(currentPage != null){
			MobclickAgent.onPageEnd(currentPage)
		}

		if(name != null){
//			MobclickAgent.onEventObject(context, "Navigation", mapOf("route" to name))
			MobclickAgent.onPageStart(name)
		}
		currentPage = name
	}
}