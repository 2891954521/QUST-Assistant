package com.qust.assistant.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

public class SettingUtil {
	
	/**
	 * 自动检查更新
	 */
	public static final String KEY_AUTO_UPDATE = "key_auto_update";
	
	/**
	 * 入学时间
	 */
	public static final String KEY_ENTRANCE_TIME = "key_entrance_time";
	
	/**
	 * 隐藏已结课课程
	 */
	public static final String KEY_HIDE_FINISH_LESSON = "key_hide_finish_lesson";

	/**
	 * 是否快捷显示饮水码
	 */
	public static final String KEY_SHOW_DRINK_CODE = "key_quick_display_drink_code";
	
	/**
	 * 显示所有课程
	 */
	public static final String KEY_SHOW_ALL_LESSON = "key_show_all_lesson";
	
	/**
	 * 开学时间
	 */
	public static final String KEY_START_DAY = "key_start_day";
	
	/**
	 * 课表时间表 0：冬季， 1：夏季
	 */
	public static final String KEY_TIME_TABLE = "key_time_table";
	
	/**
	 * 总周数
	 */
	public static final String KEY_TOTAL_WEEK = "key_total_week";
	
	/**
	 * 更新开发版
	 */
	public static final String KEY_UPDATE_DEV = "key_update_dev";
	
	/**
	 * 上次检查更新的时间
	 */
	public static final String LAST_UPDATE_TIME = "last_update_time";
	
	/**
	 * 是否首次使用
	 */
	public static final String IS_FIRST_USE = "isFirstUse";
	
	/**
	 * 主页
	 */
	public static final String HOME_PAGE = "defaultHome";
	
	/**
	 * 教务学号
	 */
	public static final String SCHOOL_NAME = "user";
	
	/**
	 * 教务密码
	 */
	public static final String SCHOOL_PASSWORD = "password";

	/**
	 * 健康打卡相关
	 */
	public static final String HEALTH_CHECK_COOKIE = "healthCheckInCookie";
	public static final String HEALTH_CHECK_USER = "healthCheckInUser";
	public static final String HEALTH_CHECK_PASSWORD = "healthCheckInPassword";
	public static final String HEALTH_CHECK_NSFYJZXGYM = "healthCheckInNsfyjzxgym";
	
	public static SharedPreferences defaultSharedPreferences;
	
	public static void init(Context context) { defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context); }
	
	/**
	 * 保存数据到 SharedPreferences
	 * @param key 键
	 * @param object 值
	 */
	public static void put(String key, Object object){
		put(null, null, key, object);
	}
	
	/**
	 * 保存数据到 SharedPreferences
	 * @param context Context
	 * @param name SharedPreferences 名称
	 * @param key 键
	 * @param object 值
	 */
	public static void put(Context context, String name, String key, Object object){
		
		SharedPreferences.Editor editor = name == null ? defaultSharedPreferences.edit() : context.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
		
		if(object == null){
			editor.putString(key, null);
		}else if(object instanceof String){
			editor.putString(key, (String)object);
		}else if(object instanceof Integer){
			editor.putInt(key, (Integer)object);
		}else if(object instanceof Boolean){
			editor.putBoolean(key, (Boolean)object);
		}else if(object instanceof Float){
			editor.putFloat(key, (Float)object);
		}else if(object instanceof Long){
			editor.putLong(key, (Long)object);
		}else{
			return;
		}
		
		editor.apply();
	}
	
	/**
	 * 获取 SharedPreferences 保存的值
	 * @param key 键
	 * @param defaultObject 默认值
	 * @return 值
	 */
	public static Object get(String key, @NonNull Object defaultObject){
		return get(null, null, key, defaultObject);
	}
	
	/**
	 * 获取 SharedPreferences 保存的值
	 * @param context Context
	 * @param name SharedPreferences 名称
	 * @param key 键
	 * @param defaultObject 默认值
	 * @return 值
	 */
	public static Object get(Context context, String name, String key, @NonNull Object defaultObject){
		
		SharedPreferences sharedPreferences = name == null ? defaultSharedPreferences : context.getSharedPreferences(name, Context.MODE_PRIVATE);
		
		if(defaultObject instanceof Boolean){
			return sharedPreferences.getBoolean(key, (Boolean)defaultObject);
		}else if(defaultObject instanceof Float){
			return sharedPreferences.getFloat(key, (Float)defaultObject);
		}else if(defaultObject instanceof Long){
			return sharedPreferences.getLong(key, (Long)defaultObject);
		}else if(defaultObject instanceof String){
			return sharedPreferences.getString(key, (String)defaultObject);
		}else if(defaultObject instanceof Integer){
			return sharedPreferences.getInt(key, (Integer)defaultObject);
		}else {
			return null;
		}
	}
	
	public static String getString(String key, @Nullable String defaultString){
		return getString(null, null, key, defaultString);
	}
	
	public static String getString(Context context, String name, String key, @Nullable String defaultString){
		SharedPreferences sharedPreferences = name == null ? defaultSharedPreferences : context.getSharedPreferences(name, Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, defaultString);
	}
	
	public static int getInt(String key, int defaultValue){
		return getInt(null, null, key, defaultValue);
	}
	
	public static int getInt(Context context, String name, String key, int defaultValue){
		SharedPreferences sharedPreferences = name == null ? defaultSharedPreferences : context.getSharedPreferences(name, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(key, defaultValue);
	}
	
	public static boolean getBoolean(String key, boolean defaultValue){
		return getBoolean(null, null, key, defaultValue);
	}
	
	public static boolean getBoolean(Context context, String name, String key, boolean defaultValue){
		SharedPreferences sharedPreferences = name == null ? defaultSharedPreferences : context.getSharedPreferences(name, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(key, defaultValue);
	}
	
	
	public static SharedPreferences.Editor edit(){
		return defaultSharedPreferences.edit();
	}
	
}
