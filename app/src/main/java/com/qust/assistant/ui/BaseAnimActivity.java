package com.qust.assistant.ui;

import android.content.Intent;

import com.qust.assistant.R;

/**
 * 具有进入和退出动画的 Activity
  */
public class BaseAnimActivity extends BaseActivity{
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(0, R.anim.anim_rigth_out);
	}
}
