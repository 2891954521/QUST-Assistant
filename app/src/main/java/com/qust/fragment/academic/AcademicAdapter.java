package com.qust.fragment.academic;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qust.assistant.R;
import com.qust.assistant.widget.TripleProgressBar;

public class AcademicAdapter extends BaseExpandableListAdapter{
	
	private static final String[] TYPE = {
			"", "在修", "未过", "未修", "已修",
			"校内被替代课程",
			"校内课程替代",
			"校内课程替代节点",
			"校外课程替换节点/校外认定课程",
			"校内被认定课程",
			"学业预警不审核课程"
	};
	
	private final Context context;

	/**
	 * 每组课程
	 */
	private LessonInfoGroup[] groups;
	
	/**
	 * 全部课程
	 */
	private LessonInfo[] lessons;
	
	public AcademicAdapter(Context context){
		this.context = context;
		groups = new LessonInfoGroup[0];
		lessons = new LessonInfo[0];
	}
	
	public void setLessons(LessonInfoGroup[] groups){
		this.groups = groups;
		notifyDataSetChanged();
	}
	
	public void setLessons(LessonInfoGroup[] groups, @NonNull LessonInfo[] lessons){
		this.groups = groups;
		this.lessons = lessons;
		notifyDataSetChanged();
	}
	
	public LessonInfo[] getLessons(){
		return lessons;
	}
	
	@Override
	public int getGroupCount(){ return groups.length; }
	
	@Override
	public int getChildrenCount(int groupPosition){
		return groups.length == 0 ? 0 : groups[groupPosition].lessonIndex.length;
	}
	
	@Override
	public Object getGroup(int groupPosition){ return null; }
	
	@Override
	public Object getChild(int groupPosition, int childPosition){ return null; }
	
	@Override
	public long getGroupId(int groupPosition){ return 0; }
	
	@Override
	public long getChildId(int groupPosition, int childPosition){ return 0; }
	
	@Override
	public boolean hasStableIds(){ return false; }
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_academic_group, null);
		}
		
		if(isExpanded){
			convertView.findViewById(R.id.item_academic_background).setBackgroundResource(R.drawable.bg_stroke_top);
		}else{
			convertView.findViewById(R.id.item_academic_background).setBackgroundResource(R.drawable.bg_stroke);
		}
		
		LessonInfoGroup group = groups[groupPosition];
		((TextView)convertView.findViewById(R.id.item_academic_name)).setText(groups[groupPosition].groupName);
		((TextView)convertView.findViewById(R.id.item_academic_require_credits)).setText("要求学分：" + group.requireCredits);
		
		TripleProgressBar progressBar = convertView.findViewById(R.id.item_academic_trip_progress);
		progressBar.setValues(group.requireCredits, group.obtainedCredits, group.creditNotEarned);
		
		((TextView)convertView.findViewById(R.id.item_academic_passed_count)).setText("共 " + group.lessonIndex.length + " 门，通过 " + group.passedCounts + " 门");
		return convertView;
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
		AcademicViewHolder viewHolder;
		
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_academic_child, null);
			viewHolder = new AcademicViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (AcademicViewHolder)convertView.getTag();
		}
		
		int index = groups[groupPosition].lessonIndex[childPosition];
		LessonInfo lesson = lessons[index];
		
		viewHolder.background.setBackgroundResource(isLastChild ? R.drawable.bg_stroke_bottom : R.drawable.bg_stroke_center);
		
		viewHolder.status.setTextColor(context.getResources().getColor(lesson.status == 2 ? R.color.colorError : R.color.colorPrimaryText));
		viewHolder.status.setText(TYPE[lesson.status]);
		
		viewHolder.name.setText(lesson.name);
		
		viewHolder.content.setText(lesson.content);
		viewHolder.credit.setText("学分：" + lesson.credit);
		
		if(!TextUtils.isEmpty(lesson.score)){
			viewHolder.score.setText("成绩：" + lesson.score);
			viewHolder.gpa.setText("绩点：" + lesson.gpa);
		}else{
			viewHolder.score.setText("");
			viewHolder.gpa.setText("");
		}
		
		return convertView;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition,int childPosition){ return false; }
	
	private static class AcademicViewHolder{
		
		public View background;
		
		public TextView status;
		
		public TextView name, content, credit, score, gpa;
		
		public AcademicViewHolder(@NonNull View convertView){
			background = convertView.findViewById(R.id.item_academic_background);
			
			name = convertView.findViewById(R.id.item_academic_name);
			content = convertView.findViewById(R.id.item_academic_content);
			credit = convertView.findViewById(R.id.item_academic_credit);
			score = convertView.findViewById(R.id.item_academic_score);
			gpa = convertView.findViewById(R.id.item_academic_gpa);
			
			status = convertView.findViewById(R.id.item_academic_status);
		}
		
	}
}
