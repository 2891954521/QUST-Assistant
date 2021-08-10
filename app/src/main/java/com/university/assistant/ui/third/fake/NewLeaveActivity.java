package com.university.assistant.ui.third.fake;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.university.assistant.R;
import com.university.assistant.util.DateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class NewLeaveActivity extends Activity{
	
	private EditText leaveType;
	
	private TextView startDate;
	
	private TextView startTime;
	
	private TextView endDate;
	
	private TextView endTime;
	
	private TextView sDate;
	
	private TextView sTime;
	
	private TextView pDate;
	
	private TextView pTime;
	
	private Switch needOut;
	
	private EditText phone;
	
	private EditText destination;
	
	private EditText destinationInfo;
	
	private EditText reason;
	
	private EditText teacher;
	
	private EditText opinion;
	
	private EditText gps;
	
	private EditText name;
	
	private TextView submit;
	
	private LinearLayout needOutLayout;
	
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_leave);
		
		sp = getSharedPreferences("leave",Context.MODE_PRIVATE);
		
		findViewById(R.id.activity_back).setOnClickListener(v -> onBackPressed());
		
		leaveType = findViewById(R.id.leave_type);
		
		startDate = findViewById(R.id.leave_start_date);
		startTime = findViewById(R.id.leave_start_time);
		endDate = findViewById(R.id.leave_end_date);
		endTime = findViewById(R.id.leave_end_time);
		
		sDate = findViewById(R.id.leave_s_date);
		sTime = findViewById(R.id.leave_s_time);
		pDate = findViewById(R.id.leave_p_date);
		pTime = findViewById(R.id.leave_p_time);
		
		needOut = findViewById(R.id.leave_need_out);
		
		needOut.setOnCheckedChangeListener((buttonView,isChecked) -> needOutLayout.setVisibility(isChecked?View.VISIBLE:View.GONE));
		
		needOutLayout = findViewById(R.id.activity_new_leave_need_out);
		
		phone = findViewById(R.id.leave_phone);
		destination = findViewById(R.id.leave_destination);
		destinationInfo = findViewById(R.id.leave_destination_info);
		reason = findViewById(R.id.leave_reason);
		teacher = findViewById(R.id.leave_teacher);
		opinion = findViewById(R.id.leave_teacher_opinion);
		gps = findViewById(R.id.leave_gps);
		
		name = findViewById(R.id.leave_name);
		
		submit = findViewById(R.id.leave_submit);
		submit.setOnClickListener(v -> submit());
		
		((CheckBox)findViewById(R.id.leave_agree)).setOnCheckedChangeListener((buttonView,isChecked) -> {
			if(isChecked)submit.setBackgroundColor(Color.parseColor("#3399fe"));
			else submit.setBackgroundColor(Color.parseColor("#c3cbd6"));
		});
		
		name.setText(sp.getString("name",""));
		teacher.setText(sp.getString("teacher",""));
		phone.setText(sp.getString("phone",""));
		
		// 时间
		{
			Calendar calendar = Calendar.getInstance();
			
			calendar.set(Calendar.HOUR_OF_DAY,7);
			calendar.set(Calendar.MINUTE,30);
			
			sDate.setText(DateUtil.YMD.format(calendar.getTime()));
			sTime.setText(DateUtil.HM.format(calendar.getTime()));
			
			sDate.setOnClickListener(v -> showDatePickerDialog(sDate));
			sTime.setOnClickListener(v -> showTimePickerDialog(sTime));
			
			calendar.set(Calendar.HOUR_OF_DAY,7);
			calendar.set(Calendar.MINUTE,40);
			
			pDate.setText(DateUtil.YMD.format(calendar.getTime()));
			pTime.setText(DateUtil.HM.format(calendar.getTime()));
			
			pDate.setOnClickListener(v -> showDatePickerDialog(pDate));
			pTime.setOnClickListener(v -> showTimePickerDialog(pTime));
			
			calendar.set(Calendar.HOUR_OF_DAY,8);
			calendar.set(Calendar.MINUTE,0);
			
			startDate.setText(DateUtil.YMD.format(calendar.getTime()));
			startTime.setText(DateUtil.HM.format(calendar.getTime()));
			
			startDate.setOnClickListener(v -> showDatePickerDialog(startDate));
			startTime.setOnClickListener(v -> showTimePickerDialog(startTime));
			
			calendar.set(Calendar.HOUR_OF_DAY,22);
			calendar.set(Calendar.MINUTE,0);
			
			endDate.setText(DateUtil.YMD.format(calendar.getTime()));
			endTime.setText(DateUtil.HM.format(calendar.getTime()));
			
			endDate.setOnClickListener(v -> showDatePickerDialog(endDate));
			endTime.setOnClickListener(v -> showTimePickerDialog(endTime));
		}
		
	}
	
	private void submit(){
		LeaveData.Data data = new LeaveData.Data();
		data.isFinish = false;
		data.type = leaveType.getText().toString();
		data.start = startDate.getText().toString() + " " + startTime.getText().toString();
		data.end = endDate.getText().toString() + " " + endTime.getText().toString();
		data.needLeave = needOut.isChecked();
		data.phone = phone.getText().toString();
		data.destination = destination.getText().toString();
		data.destinationInfo = destinationInfo.getText().toString();
		data.reason = reason.getText().toString();
		data.teacher = teacher.getText().toString();
		
		data.teacherOpinion = opinion.getText().toString();
		if("".equals(data.teacherOpinion)) data.teacherOpinion = "无";
		
		data.gps = gps.getText().toString();
		data.sTime = sDate.getText().toString() + " " + sTime.getText().toString();
		data.pTime = pDate.getText().toString() + " " + pTime.getText().toString();
		
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("name",name.getText().toString());
		editor.putString("teacher",data.teacher);
		editor.putString("phone",data.phone);
		editor.apply();
		
		File f = new File(getExternalFilesDir("data"),String.valueOf(System.currentTimeMillis()));
		try{
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
			stream.writeObject(data);
			stream.flush();
			stream.close();
		}catch(IOException e){
			Toast.makeText(this,"提交失败！" + e.getMessage(),Toast.LENGTH_LONG).show();
			return;
		}
		LeaveData.getInstance().data.add(data);
		LeaveData.getInstance().files.add(f.toString());
		Toast.makeText(this,"提交成功！",Toast.LENGTH_SHORT).show();
		onBackPressed();
	}
	
	private void showDatePickerDialog(final TextView tv){
		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.YMD.parse(tv.getText().toString()));
			new DatePickerDialog(this,0,(view,year,monthOfYear,dayOfMonth) -> tv.setText(String.format(Locale.CHINA,"%04d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth)),calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
		}catch(ParseException ignored){ }
	}
	
	private void showTimePickerDialog(final TextView tv){
		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.HM.parse(tv.getText().toString()));
			new TimePickerDialog(this,4,(view,hourOfDay,minute) -> tv.setText(String.format(Locale.CHINA,"%02d:%02d", hourOfDay, minute)),calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
		}catch(ParseException ignored){ }
	}
	
}
