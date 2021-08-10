package com.university.assistant.ui.third.fake;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LeaveInfoActivity extends Activity{
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("当前时间:yyyy-MM-dd HH:mm:ss",Locale.CHINA);
	
	private ImageView info, code;
	
	private TextView infoText, codeText;
	
	private TextView time, codeTime;
	
	private ViewGroup infoLayout, codeLayout;
	
	private Runnable runnable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_info);
		findViewById(R.id.activity_back).setOnClickListener(v -> onBackPressed());
		int position = getIntent().getIntExtra("data",-1);
		if(position == -1){
			finish();
			return;
		}
		LeaveData.Data leave = LeaveData.getInstance().data.get(position);
		
		((TextView)findViewById(R.id.activity_leave_info_type)).setText(leave.type);
		
		((TextView)findViewById(R.id.activity_leave_info_need_leave)).setText(leave.needLeave ? "是" : "否");
		
		((TextView)findViewById(R.id.activity_leave_info_start)).setText(leave.start.substring(leave.start.indexOf("-") + 1));
		
		((TextView)findViewById(R.id.activity_leave_info_end)).setText(leave.end.substring(leave.end.indexOf("-") + 1));
		
		((TextView)findViewById(R.id.activity_leave_info_total_time)).setText(DateUtil.timeDifference(leave.start, leave.end));
		
		((TextView)findViewById(R.id.activity_leave_info_reason)).setText(leave.reason);
		
		((TextView)findViewById(R.id.activity_leave_info_gps)).setText(leave.gps);
		;
		((TextView)findViewById(R.id.activity_leave_info_name)).setText(getSharedPreferences("leave",Context.MODE_PRIVATE).getString("name","") + " - 发起申请");
		
		((TextView)findViewById(R.id.activity_leave_info_sTime)).setText(leave.sTime.substring(leave.sTime.indexOf("-") + 1));
		
		((TextView)findViewById(R.id.activity_leave_info_teacher)).setText(Html.fromHtml("一级：" + leave.teacher + " - 审批<font color='#14bc7f'>通过</font>"));
		
		((TextView)findViewById(R.id.activity_leave_info_pTime)).setText(leave.pTime.substring(leave.pTime.indexOf("-") + 1));
		
		((TextView)findViewById(R.id.activity_leave_info_teacherOpinion)).setText("审批意见：" + leave.teacherOpinion);
		
		if(leave.needLeave){
			((TextView)findViewById(R.id.activity_leave_info_phone)).setText(leave.phone);
			((TextView)findViewById(R.id.activity_leave_info_destination)).setText(leave.destination);
		}else{
			findViewById(R.id.activity_leave_info_phone_layout).setVisibility(View.GONE);
			findViewById(R.id.activity_leave_info_destination_layout).setVisibility(View.GONE);
		}
		
		infoText = findViewById(R.id.activity_leave_info);
		codeText = findViewById(R.id.activity_leave_info_code);
		
		info = findViewById(R.id.activity_leave_info_img);
		code = findViewById(R.id.activity_leave_code_img);
		
		infoLayout = findViewById(R.id.activity_leave_info_layout);
		codeLayout = findViewById(R.id.activity_leave_info_code_layout);
		
		codeTime = findViewById(R.id.activity_leave_info_code_time);
		
		infoText.setOnClickListener(v -> {
			infoText.setTextColor(Color.parseColor("#4096ed"));
			codeText.setTextColor(Color.parseColor("#657181"));
			info.setVisibility(View.VISIBLE);
			code.setVisibility(View.INVISIBLE);
			infoLayout.setVisibility(View.VISIBLE);
			codeLayout.setVisibility(View.GONE);
			
		});
		
		codeText.setOnClickListener(v -> {
			codeText.setTextColor(Color.parseColor("#4096ed"));
			infoText.setTextColor(Color.parseColor("#657181"));
			code.setVisibility(View.VISIBLE);
			info.setVisibility(View.INVISIBLE);
			codeLayout.setVisibility(View.VISIBLE);
			infoLayout.setVisibility(View.GONE);
		});
		
		time = findViewById(R.id.activity_leave_info_time);
		
		runnable = this::updateTime;
		
		updateTime();
	}
	
	private void updateTime(){
		String s = FORMAT.format(new Date());
		time.setText(s);
		codeTime.setText(s);
		time.postDelayed(runnable,1000);
	}
	
}
