package com.qust.model;

import android.os.Build;

import androidx.annotation.NonNull;

import com.qust.QustAPI;
import com.qust.account.ea.EAViewModel;
import com.qust.fragment.academic.LessonInfo;
import com.qust.fragment.academic.LessonInfoGroup;
import com.qust.fragment.academic.LessonResult;
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

import okhttp3.FormBody;
import okhttp3.Response;

/**
 * 学业情况查询工具类
 */
public class AcademicModel{
	
	/**
	 * 匹配课程类别
	 */
	private static final Pattern xfyqjd_id = Pattern.compile(" xfyqjd_id='(.*?)'");
	
	/**
	 * 匹配要求学分
	 */
	private static final Pattern xfyqjd_id_yxxf_yqzdxf = Pattern.compile(" xfyqjd_id='([a-zA-Z\\d]+)'.*?yxxf='([\\d.]+)' yqzdxf='([\\d.]+)'");
	
	
	@NonNull
	public static LessonResult getLessons(@NonNull EAViewModel eaViewModel){
		
		LessonResult lessonResult = new LessonResult();
		lessonResult.lessonInfo = new LessonInfo[0];
		lessonResult.lessonInfoGroup = new LessonInfoGroup[0];
		
		ArrayList<LessonInfo> lessonInfo = new ArrayList<>(64);
		
		try(Response response =  eaViewModel.getSync(QustAPI.ACADEMIC_PAGE)){
			String html = response.body().string();
			
			HashMap<String, LessonInfoGroup> xfyqjd = new HashMap<>();
			Matcher matcher = xfyqjd_id.matcher(html);
			while(matcher.find()){
				String id = matcher.group(1);
				if(!xfyqjd.containsKey(id)){
					xfyqjd.put(id, new LessonInfoGroup());
				}
			}
			
			matcher = xfyqjd_id_yxxf_yqzdxf.matcher(html);
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
				try(Response response1 = eaViewModel.postSync(QustAPI.ACADEMIC_INFO, new FormBody.Builder().add("xfyqjd_id", param).add("xh_id", eaViewModel.getAccountName()).build())){
					html = response1.body().string();
				}
		
				JSONArray array = new JSONArray(html);
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
