package com.qust.account;


/**
 * 请求不成功时的回调
 */
public interface RequestErrorCallback{
	
	/**
	 * 需要输入账号密码登录
	 */
	default void onNeedLogin(){ }
	
	/**
	 * 网络错误
	 */
	default void onNetworkError(Exception e){ }
	
}