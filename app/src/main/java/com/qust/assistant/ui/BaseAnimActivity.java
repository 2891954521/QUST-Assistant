package com.qust.assistant.ui;

import android.content.Intent;

import com.qust.assistant.R;

// 具有进入和退出动画的 Activity
public class BaseAnimActivity extends BaseActivity{
	
	protected void activityAnim(){
		overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		activityAnim();
	}
	
	@Override
	public void finish(){
		super.finish();
		activityAnim();
	}
}
