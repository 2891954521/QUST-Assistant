package com.qust.assistant.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

public class SettingUtil {
	
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
