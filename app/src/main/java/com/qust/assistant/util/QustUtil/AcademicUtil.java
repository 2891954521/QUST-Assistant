package com.qust.assistant.util.QustUtil;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.ui.fragment.academic.LessonInfo;
import com.qust.assistant.ui.fragment.academic.LessonInfoGroup;
import com.qust.assistant.ui.fragment.academic.LessonResult;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.ParamUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学业情况查询工具类
 */
public class AcademicUtil{
	
	/**
	 * 匹配课程类别
	 */
	private static final Pattern xfyqjd_id = Pattern.compile(" xfyqjd_id='(.*?)'");
	
	/**
	 * 匹配要求学分
	 */
	private static final Pattern xfyqjd_id_yxxf_yqzdxf = Pattern.compile(" xfyqjd_id='([a-zA-Z\\d]+)'.*?yxxf='([\\d.]+)' yqzdxf='([\\d.]+)'");
	
	
	@NonNull
	public static LessonResult getLessons(@NonNull LoginViewModel loginViewModel){
		
		LessonResult lessonResult = new LessonResult();
		lessonResult.lessonInfo = new LessonInfo[0];
		lessonResult.lessonInfoGroup = new LessonInfoGroup[0];
		
		try{
			ArrayList<LessonInfo> lessonInfo = new ArrayList<>(64);
			
			String response = loginViewModel.doGet(QustAPI.ACADEMIC_PAGE);
			if(TextUtils.isEmpty(response)) return lessonResult;
			
			HashMap<String, LessonInfoGroup> xfyqjd = new HashMap<>();
			Matcher matcher = xfyqjd_id.matcher(response);
			while(matcher.find()){
				String id = matcher.group(1);
				if(!xfyqjd.containsKey(id)){
					xfyqjd.put(id, new LessonInfoGroup());
				}
			}
			
			matcher = xfyqjd_id_yxxf_yqzdxf.matcher(response);
			while(matcher.find()){
				String id = matcher.group(1);
				if(xfyqjd.containsKey(id)){
					LessonInfoGroup group = xfyqjd.get(id);
					float obtain = ParamUtil.parseFloat(matcher.group(2));
					float require = ParamUtil.parseFloat(matcher.group(3));
					group.obtainedCredits = obtain;
					group.requireCredits = require;
				}
			}
			
			for(String param : xfyqjd.keySet()){
				response = loginViewModel.doPost(QustAPI.ACADEMIC_INFO,
						"xfyqjd_id=" + param + "&xh_id=" + loginViewModel.name
				);
				if(TextUtils.isEmpty(response)) continue;
				
				JSONArray array = new JSONArray(response);
				if(array.length() == 0) continue;
				
				LessonInfoGroup group = xfyqjd.get(param);
				group.lessonIndex = new int[array.length()];
				for(int j = 0; j < array.length(); j++){
					group.lessonIndex[j] = lessonInfo.size();
					LessonInfo info = new LessonInfo(array.getJSONObject(j));
					
					if(info.status == 2){
						// 统计未过课程的学分
						group.creditNotEarned += ParamUtil.parseFloat(info.credit);
					}else if(info.status == 4){
						// 统计已修门数
						group.passedCounts++;
					}
					
					lessonInfo.add(info);
				}
				group.groupName = lessonInfo.get(group.lessonIndex[0]).category;
			}
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
				lessonResult.lessonInfoGroup = xfyqjd.values().stream().filter(lessonInfoGroup -> lessonInfoGroup.lessonIndex.length > 0).toArray(LessonInfoGroup[]::new);
			}else{
				Iterator<LessonInfoGroup> it = xfyqjd.values().iterator();
				ArrayList<LessonInfoGroup> array = new ArrayList<>(xfyqjd.size());
				while(it.hasNext()){
					LessonInfoGroup group = it.next();
					if(group.lessonIndex.length > 0){
						array.add(group);
					}
				}
				lessonResult.lessonInfoGroup = array.toArray(new LessonInfoGroup[0]);
			}
			lessonResult.lessonInfo = lessonInfo.toArray(new LessonInfo[0]);
		}catch(IOException | JSONException | IllegalStateException | NumberFormatException e){
			LogUtil.Log(e);
		}
		return lessonResult;
	}
	
}
