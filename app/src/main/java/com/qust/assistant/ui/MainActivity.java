package com.qust.assistant.ui;

import android.content.Intent;
import android.os.Bundle;

import com.qust.assistant.R;
import com.qust.assistant.ui.app.GuideActivity;
import com.qust.assistant.ui.base.QFragmentActivity;
import com.qust.assistant.util.QustUtil.NoticeUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.UpdateUtil;

public class MainActivity extends QFragmentActivity{
	
	@Override
	protected void onCreate(Bundle paramBundle){
		super.onCreate(paramBundle);
		
		// 第一次使用跳转到引导页
		if(SettingUtil.getBoolean(getString(R.string.isFirstUse), true)){
			startActivity(new Intent(this, GuideActivity.class));
			finish();
			return;
		}
		
		// 检查更新
		UpdateUtil.checkUpdate(this);
		
		// 检查教务通知
		NoticeUtil.checkNotice(this);
	}

	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
}