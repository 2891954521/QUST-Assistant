package com.qust.widget;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.app.hubert.guide.listener.AnimationListenerAdapter;
import com.qust.assistant.R;

/**
 * 从底部弹出的dialog
 */
public class BottomDialog{
	
	private boolean isAnim, isShowing;
	
	private View backView, contentView;
	
	private Animation animIn, animOut;
	
	public BottomDialog(@NonNull Activity activity, View _backView, View _contentView, float height){
		backView = _backView;
		contentView = _contentView;
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		contentView.getLayoutParams().height = (int)(displayMetrics.heightPixels * height);
		
		animIn = AnimationUtils.loadAnimation(activity, R.anim.anim_bottom_in);
		animIn.setAnimationListener(new AnimationListenerAdapter(){
			@Override
			public void onAnimationEnd(Animation animation){
				isAnim = false;
				isShowing = true;
			}
		});
		
		animOut = AnimationUtils.loadAnimation(activity, R.anim.anim_bottom_out);
		animOut.setAnimationListener(new AnimationListenerAdapter(){
			@Override
			public void onAnimationEnd(Animation animation){
				isAnim = false;
				isShowing = false;
				backView.setVisibility(View.GONE);
			}
		});
	}
	
	public void show(){
		if(!isShowing || !isAnim){
			isAnim = true;
			backView.setVisibility(View.VISIBLE);
			contentView.startAnimation(animIn);
		}
	}
	
	public void hide(){
		if(isShowing || !isAnim){
			isAnim = true;
			contentView.startAnimation(animOut);
		}
	}
	
	public <T extends View> T findViewById(@IdRes int id){
		return backView.findViewById(id);
	}
	
	public boolean isShowing(){
		return isShowing;
	}
}
