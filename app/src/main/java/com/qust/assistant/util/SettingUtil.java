package com.qust.assistant.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SettingUtil {
	public static SharedPreferences setting;
	
	public static void init(Context context) { setting = PreferenceManager.getDefaultSharedPreferences(context); }
}
