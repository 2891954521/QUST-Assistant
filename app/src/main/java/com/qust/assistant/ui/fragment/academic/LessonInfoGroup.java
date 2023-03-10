package com.qust.assistant.ui.fragment.academic;

import android.os.Build;

import java.io.Serializable;
import java.util.ArrayList;

public class LessonInfoGroup implements Serializable{

	private static final long serialVersionUID = 7758456151008743453L;
	
	/**
	 * 要求学分
	 */
	public float requireCredits;
	
	/**
	 * 已获得学分
	 */
	public float obtainedCredits;
	
	/**
	 * 未通过学分
	 */
	public float creditNotEarned;
	
	/**
	 * 已通过门数
	 */
	public int passedCounts;
	
	/**
	 * 分组名称
	 */
	public String groupName;
	
	/**
	 * 课程索引
	 */
	public int[] lessonIndex;
	
	public LessonInfoGroup(){
		lessonIndex = new int[0];
	}
	
	
	public static class Builder{
		
		public LessonInfoGroup group;
		
		public ArrayList<Integer> lessonIndex;
		
		public Builder(){
			group = new LessonInfoGroup();
			lessonIndex = new ArrayList<>(32);
		}
		
		/**
		 * 添加一条成绩明细
		 */
		public void addLesson(int index){
			lessonIndex.add(index);
		}
		
		public LessonInfoGroup build(){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
				group.lessonIndex = lessonIndex.stream().mapToInt(Integer::valueOf).toArray();
			}else{
				int[] index = new int[lessonIndex.size()];
				for(int i = 0; i < index.length; i++) index[i] = lessonIndex.get(i);
				group.lessonIndex = index;
			}
			return group;
		}
	}
	
}
