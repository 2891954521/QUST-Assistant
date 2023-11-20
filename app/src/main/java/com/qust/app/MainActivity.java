package com.qust.app;

import android.content.Intent;
import android.os.Bundle;

import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.ui.QFragmentActivity;
import com.qust.model.AutoQueryModel;
import com.qust.utils.UpdateUtils;

public class MainActivity extends QFragmentActivity{
	
	@Override
	protected void onCreate(Bundle paramBundle){
		super.onCreate(paramBundle);
		
		// 第一次使用跳转到引导页
		if(SettingUtil.getBoolean(getString(R.string.isFirstUse), true)){
			startActivity(new Intent(this, GuideActivity.class));
			return;
		}
		
		// 检查更新
		UpdateUtils.checkUpdateAsync(this);
		
		// 自动检查工具
		AutoQueryModel.startAutoQuery(this);
	}

	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
}