package com.university.assistant.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.university.assistant.R;
import com.university.assistant.util.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;

public class TimePicker implements NumberPicker.OnValueChangeListener{
	
	private Calendar calendar;
	
	private NumberPicker yearPicker;
	private NumberPicker monthPicker;
	private NumberPicker datePicker;
	private NumberPicker hourPicker;
	private NumberPicker minutePicker;
	
	private LinearLayout layout;
	
	private SimpleDateFormat simpleDateFormat;
	
	public TimePicker(Context context,@Nullable Calendar _calendar){
		
		if(calendar!=null) calendar = _calendar;
		else calendar = Calendar.getInstance();
		
		layout = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.layout_time_picker,null);
		
		yearPicker = layout.findViewById(R.id.layout_time_picker_year);
		monthPicker = layout.findViewById(R.id.layout_time_picker_month);
		datePicker = layout.findViewById(R.id.layout_time_picker_date);
		hourPicker = layout.findViewById(R.id.layout_time_picker_hour);
		minutePicker = layout.findViewById(R.id.layout_time_picker_minute);
		
		//限制年份范围为前后五年
		int yearNow = calendar.get(Calendar.YEAR);
		yearPicker.setMinValue(yearNow - 5);
		yearPicker.setMaxValue(yearNow + 5);
		yearPicker.setValue(yearNow);
		// 关闭选择器循环
		yearPicker.setWrapSelectorWheel(false);
		
		//设置月份范围为1~12
		monthPicker.setMinValue(1);
		monthPicker.setMaxValue(12);
		monthPicker.setValue(calendar.get(Calendar.MONTH) + 1);
		monthPicker.setWrapSelectorWheel(false);
		
		// 日期限制存在变化，需要根据当月最大天数来调整
		datePicker.setMinValue(1);
		datePicker.setMaxValue(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		datePicker.setValue(calendar.get(Calendar.DATE));
		datePicker.setWrapSelectorWheel(false);
		
		//24小时制，限制小时数为0~23
		hourPicker.setMinValue(0);
		hourPicker.setMaxValue(23);
		hourPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));
		hourPicker.setWrapSelectorWheel(false);
		
		//限制分钟数为0~59
		minutePicker.setMinValue(0);
		minutePicker.setMaxValue(59);
		minutePicker.setValue(calendar.get(Calendar.MINUTE));
		minutePicker.setWrapSelectorWheel(false);
		
		simpleDateFormat = new SimpleDateFormat("yyyy-MM",Locale.CHINA);
		
		//为年份和月份设置监听
		yearPicker.setOnValueChangedListener(this);
		monthPicker.setOnValueChangedListener(this);
		
	}
	
	@Override
	public void onValueChange(NumberPicker numberPicker, int i, int i1) {
		Calendar calendar = Calendar.getInstance();
		try{
			calendar.setTime(simpleDateFormat.parse(yearPicker.getValue() + "-" + monthPicker.getValue()));
		}catch(ParseException e){
			LogUtil.Log(e);
		}
		int maxValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		datePicker.setMaxValue(maxValue);
		//重设日期值，防止月份变动时超过最大值
		datePicker.setValue(Math.min(datePicker.getValue(), maxValue));
	}
	
	public Calendar getTime(){
		calendar.set(yearPicker.getValue(),monthPicker.getValue() - 1,datePicker.getValue(),hourPicker.getValue(),minutePicker.getValue());
		return calendar;
	}
	
	public LinearLayout getLayout(){
		return layout;
	}
}
