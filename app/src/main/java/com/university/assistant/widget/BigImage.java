package com.university.assistant.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.university.assistant.R;

import java.io.File;

import androidx.annotation.Nullable;

public class BigImage extends androidx.appcompat.widget.AppCompatImageView implements View.OnClickListener{
	
	private int smallSize;
	
	private int x,y;
	
	private int top;
	
	public BigImage(Context context){
		this(context,null);
	}
	
	public BigImage(Context context,@Nullable AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public BigImage(Context context,@Nullable AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		setVisibility(GONE);
		setFocusable(true);
		setOnClickListener(this);
		smallSize = getResources().getDisplayMetrics().widthPixels/3;
		int[] location = new int[2];
		getLocationOnScreen(location);
		top = location[1];
	}
	
	public void setImage(ImageView image,String path){
		//从相对位置 的起点开始缩放动画
		//ScaleAnimation scale = new ScaleAnimation(0, 1, 0,1);
		//从相对位置的100px x 100px的位置开始动画
		int[] location = new int[2];
		image.getLocationOnScreen(location);
		
		x = location[0] + smallSize/2;
		y = location[1] - top;
		ScaleAnimation scale = new ScaleAnimation(0.3f, 1f, 0.3f,1f,x,y);
		//从按钮自身的中心点开始缩放动画
		//ScaleAnimation scale=new ScaleAnimation(0, 1, 0,1,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		scale.setDuration(200);
		setVisibility(View.VISIBLE);
		if(new File(path).exists()){
			setImageBitmap(BitmapFactory.decodeFile(path));
		}else{
			setImageResource(R.drawable.ic_image);
		}
		startAnimation(scale);
		
	}
	
	public void removeImage(){
		ScaleAnimation scale = new ScaleAnimation(1f, 0.3f, 1f,0.3f,x,y);
		scale.setDuration(200);
		scale.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				setVisibility(GONE);
				setImageBitmap(null);
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
		startAnimation(scale);
	}
	@Override
	public void onClick(View v){
		removeImage();
	}
}
