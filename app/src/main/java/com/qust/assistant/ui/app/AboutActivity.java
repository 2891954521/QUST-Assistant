package com.qust.assistant.ui.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.qust.assistant.BuildConfig;
import com.qust.assistant.R;
import com.qust.assistant.ui.base.BaseAnimActivity;
import com.qust.assistant.util.LogUtil;

public class AboutActivity extends BaseAnimActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.activity_setting_contain, new PrefsFragment(this)).commit();
		
		initToolBar("关于");
		
		throw new RuntimeException("test log");
	}
	
	public static class PrefsFragment extends PreferenceFragmentCompat{
		
		private final AboutActivity activity;
		
		public PrefsFragment(AboutActivity _activity){
			activity = _activity;
		}
		
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
			addPreferencesFromResource(R.xml.preference_bout);

			try{
				PackageInfo pkg = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
				findPreference("app_version").setSummary(pkg.versionName);
				findPreference("app_build_time").setSummary(BuildConfig.PACKAGE_TIME);
				
			}catch(PackageManager.NameNotFoundException e){
				LogUtil.Log(e);
			}
			
			
		}
	}
}
