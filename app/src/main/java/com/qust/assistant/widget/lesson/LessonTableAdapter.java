package com.qust.assistant.widget.lesson;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.qust.assistant.R;

public class LessonTableAdapter extends PagerAdapter{
	
	private int totalWeeks;
	
	private LessonTable lessonTable;
	
	public LessonTableAdapter(LessonTable lessonTable, int totalWeeks){
		this.lessonTable = lessonTable;
		this.totalWeeks = totalWeeks;
	}
	
	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position){

		ViewGroup layout = (ViewGroup)LayoutInflater.from(lessonTable.getContext()).inflate(R.layout.layout_timetable_week, container, false);
		
		LessonView lessonView = new LessonView(lessonTable.getContext());
		lessonView.setData(lessonTable, position);
		lessonView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		((LinearLayout)layout.findViewById(R.id.layout_timetable_week_contain)).addView(lessonView);
		container.addView(layout);

		return layout;
	}
	
	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object){ container.removeView((View)object); }
	
	@Override
	public int getCount(){ return totalWeeks; }
	
	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object){ return view == object; }
	
	@Override
	public int getItemPosition(@NonNull Object object){
		return POSITION_NONE;
	}
}