package com.university.assistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.MingDeUtil;

import androidx.annotation.Nullable;

public class NavigationActivity extends BaseActivity{
	
	private TextView text;
	
	private String start,destination;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		
		Intent i = getIntent();
		start = i.getStringExtra("start");
		destination = i.getStringExtra("destination");
		
		text = findViewById(R.id.activity_navigation_text);
		
		((RadioButton)findViewById(R.id.activity_navigation_current)).setText("当前教室:" + start);
		
		((TextView)findViewById(R.id.activity_navigation_destination)).setText(destination);
		
		RadioGroup startPlace = findViewById(R.id.activity_navigation_start);
		
		startPlace.setOnCheckedChangeListener((group,checkedId) -> {
			switch(checkedId){
				case R.id.activity_navigation_current:
					if(start.startsWith("明")){
						text.setText(MingDeUtil.class2class(start,destination));
					}else{
						text.setText("无法导航！");
					}
					break;
				case R.id.activity_navigation_north:
					text.setText(MingDeUtil.gate2class("学院楼",destination));
					break;
				case R.id.activity_navigation_south:
					text.setText(MingDeUtil.gate2class("弘毅楼",destination));
					break;
			}
		});
		
		String s = "";
		
		if(start.startsWith("明")){
			startPlace.check(R.id.activity_navigation_current);
			s = MingDeUtil.class2class(start,destination);
		}else if(start.startsWith("弘")){
			startPlace.check(R.id.activity_navigation_south);
			s = MingDeUtil.gate2class("弘毅楼",destination);
		}else if("".equals(start)){
			startPlace.check(R.id.activity_navigation_north);
			s = MingDeUtil.gate2class("学院楼",destination);
		}
		
		text.setText(s);
	}
}
