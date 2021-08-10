package com.university.assistant.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class RequiredTextView extends AppCompatTextView{
	
	
	public RequiredTextView(Context context){
		super(context);
	}
	
	public RequiredTextView(Context context,AttributeSet attrs){
		super(context,attrs);
	}
	
	public RequiredTextView(Context context,AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
	}
	
	@Override
	public void setText(CharSequence text,BufferType type){
		Spannable span = new SpannableString("* " + text);
		span.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		super.setText(span,type);
	}
}
