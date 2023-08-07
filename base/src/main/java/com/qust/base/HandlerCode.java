package com.qust.base;

/**
 * Handler 公用的 what 值
 */
public class HandlerCode{

	/**
	 * 更新 Dialog
	 */
	public static final int UPDATE_DIALOG = 0;
	
	/**
	 * 关闭 Dialog 并 Toast
	 */
	public static final int DISMISS_TOAST = 1;
	
	/**
	 * 更新 AdapterView
	 */
	public static final int NOTIFY_TOAST = 2;
	
	/**
	 * 仅 Toast
	 */
	public static final int TOAST = 3;
	
	/**
	 * HTTP请求成功
	 */
	public static final int HTTP200 = 200;
	
	/**
	 * HTTP请求错误
	 */
	public static final int HTTP500 = 500;
}
