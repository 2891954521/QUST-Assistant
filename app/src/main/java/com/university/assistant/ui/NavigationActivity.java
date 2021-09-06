package com.university.assistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.MingDeUtil;

import androidx.annotation.Nullable;

public class NavigationActivity extends BaseAnimActivity{
	
	private static final String[] FLOORS = {
		"1F", "2F", "3F", "4F", "5F", "6F"
	};
	
	private static final String[][] MING_DE_MAP = {
		{"一号门","二号门","入口1","三号门","入口2","四号门","五号门"},
		{"201","202","203","204","205","206","212","214","215","216","217","220","222","227","228","230","232","233","234","236","238","239","240","241","242"},
		{"301","302","303","304","305","306","307","308","310","312","314","315","316","317","320","322","323","324","325","327","328","330","332","333","334","336","338","339","340","341","342"},
		{"401","402","403","404","405","406","407","408","410","412","414","415","416","417","420","422","423","424","425","427","428","430","432","433","434","436","438","439","440","441","442"},
		{"501","502","503","504","505","506","507","508","510","512","514","515","516","517","520","522","523","524","525","527","528","530","532","533","534","536","538","539","540","541","542"},
		{"601","602","603","604","605","606","607","612","614","615","616","617","620","622","623","624","625","627","628","630","632","633","634","636","638","639","640","641","642"},
	};
	
	private TextView text;
	
	private String start, destination;
	
	private NumberPicker startFloor, startPlace, endFloor, endPlace;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		
		Intent i = getIntent();
		
		start = i.getStringExtra("start");
		destination = i.getStringExtra("destination");
		
		text = findViewById(R.id.activity_navigation_text);
		
		startFloor = findViewById(R.id.activity_navigation_start_floor);
		endFloor = findViewById(R.id.activity_navigation_end_floor);
		
		startPlace = findViewById(R.id.activity_navigation_start_place);
		endPlace = findViewById(R.id.activity_navigation_end_place);
		
		startFloor.setOnValueChangedListener((picker,oldVal,newVal) -> {
			if(newVal == 0){
				startPlace.setValue(0);
				startPlace.setMaxValue(0);
				startPlace.setDisplayedValues(MING_DE_MAP[0]);
				startPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			}else{
				setStart((newVal + 1) + "01");
			}
		});
		
		endFloor.setOnValueChangedListener((picker,oldVal,newVal) -> {
			if(newVal == 0){
				endPlace.setValue(0);
				endPlace.setMaxValue(0);
				endPlace.setDisplayedValues(MING_DE_MAP[0]);
				endPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			}else{
				setEnd((newVal + 1) + "01");
			}
		});
		
		findViewById(R.id.activity_navigation_navigation).setOnClickListener(v -> {
			int floor = startFloor.getValue();
			destination = "明-" + MING_DE_MAP[endFloor.getValue()][endPlace.getValue()];
			if(floor == 0){
				text.setText(MingDeUtil.gate2class(MING_DE_MAP[floor][startPlace.getValue()], destination));
			}else{
				text.setText(MingDeUtil.class2class(MING_DE_MAP[floor][startPlace.getValue()], destination));
			}
		});
		
		updatePath();
		
		initToolBar(null);
		initSliding(null, null);
	}
	
	private void setStart(String code){
		startPlace.setValue(0);
		startPlace.setMaxValue(0);
		if(code == null){
			startPlace.setDisplayedValues(new String[]{"-"});
		}else{
			int position = code.charAt(0) - 49;
			startPlace.setDisplayedValues(MING_DE_MAP[position]);
			startPlace.setMaxValue(MING_DE_MAP[position].length - 1);
			for(int i=0;i<MING_DE_MAP[position].length;i++){
				if(code.equals(MING_DE_MAP[position][i])){
					startPlace.setValue(i);
					break;
				}
			}
		}
	}
	
	private void setEnd(String code){
		endPlace.setValue(0);
		endPlace.setMaxValue(0);
		if(code == null){
			endPlace.setDisplayedValues(new String[]{"-"});
		}else{
			int position = code.charAt(0) - 49;
			endPlace.setDisplayedValues(MING_DE_MAP[position]);
			endPlace.setMaxValue(MING_DE_MAP[position].length - 1);
			for(int i=0;i<MING_DE_MAP[position].length;i++){
				if(code.equals(MING_DE_MAP[position][i])){
					endPlace.setValue(i);
					break;
				}
			}
		}
	}
	
	private void updatePath(){
		if(TextUtils.isEmpty(start) || TextUtils.isEmpty(destination)){

			startFloor.setMaxValue(FLOORS.length - 1);
			endFloor.setMaxValue(FLOORS.length - 1);
			
			startFloor.setDisplayedValues(FLOORS);
			endFloor.setDisplayedValues(FLOORS);
			
			startPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			endPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			
			startPlace.setDisplayedValues(MING_DE_MAP[0]);
			endPlace.setDisplayedValues(MING_DE_MAP[0]);
			return;
		}
		String s = "";
		if(start.startsWith("明")){
			startFloor.setValue(start.charAt(0) - 49);
			setStart(start.substring(start.length() - 3));
			s = MingDeUtil.class2class(start,destination);
		}else if(start.startsWith("弘")){
			startFloor.setValue(0);
			startPlace.setValue(0);
			startPlace.setMaxValue(0);
			startPlace.setDisplayedValues(MING_DE_MAP[0]);
			startPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			startPlace.setValue(2);
			s = MingDeUtil.gate2class("弘毅楼",destination);
		}else if("".equals(start)){
			startFloor.setValue(0);
			startPlace.setValue(0);
			startPlace.setMaxValue(0);
			startPlace.setDisplayedValues(MING_DE_MAP[0]);
			startPlace.setMaxValue(MING_DE_MAP[0].length - 1);
			startPlace.setValue(5);
			s = MingDeUtil.gate2class("学院楼",destination);
		}
		text.setText(s);
	}
}
