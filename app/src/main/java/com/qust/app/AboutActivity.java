package com.qust.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.qust.assistant.BuildConfig;
import com.qust.assistant.R;
import com.qust.assistant.util.LogUtil;
import com.qust.base.ui.BaseActivity;
import com.qust.base.ui.BaseAnimActivity;

public class AboutActivity extends BaseAnimActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.activity_setting_contain, new PrefsFragment(this)).commit();
		
		initToolBar("关于");
	}
	
	public static class PrefsFragment extends PreferenceFragmentCompat{
		
		private BaseActivity activity;
		public PrefsFragment(BaseActivity _activity){
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
				
				findPreference("join_QQ_group").setOnPreferenceClickListener(preference -> {
					Intent intent = new Intent();
					intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DnNFMNaZ_BUcnlckdeOqXI2XcIHJ-7Onx"));
					try{ startActivity(intent); }catch(Exception e){ activity.toastError("未安装手机QQ或QQ版本过低"); }
					return true;
				});
				
			}catch(PackageManager.NameNotFoundException e){
				LogUtil.Log(e);
			}
		}
	}
}
