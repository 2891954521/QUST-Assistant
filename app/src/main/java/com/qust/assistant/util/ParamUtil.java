package com.qust.assistant.util;

import android.content.Context;

import java.util.regex.Pattern;

public class ParamUtil{
	
	public static final Pattern FLOAT_PATTERN = Pattern.compile("[0-9\\\\.]+");
	
	public static boolean isFloat(String str){
		return FLOAT_PATTERN.matcher(str).matches();
	}
	
	/**
	 * 字符串转Float，字符串非法时返还0
	 */
	public static float parseFloat(String str){
		return isFloat(str) ? Float.parseFloat(str) : 0f;
	}
	
	/**
	 * dp 转 px
	 */
	public static int dp2px(Context context,float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	/**
	 * px 转 dp
	 */
	public static int px2dp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
