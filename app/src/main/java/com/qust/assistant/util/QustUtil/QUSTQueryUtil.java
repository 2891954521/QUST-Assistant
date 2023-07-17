package com.qust.assistant.util.QustUtil;

import android.os.Build;

import androidx.annotation.NonNull;

import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.Exam;
import com.qust.assistant.vo.Mark;
import com.qust.assistant.vo.Notice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class QUSTQueryUtil{
	
	/**
	 * 查询一条教务通知
	 *
	 * @param loginViewModel 登录
	 */
	@NonNull
	public static Notice[] queryNotice(@NonNull LoginViewModel loginViewModel){
		return queryNotice(loginViewModel, 1, 1);
	}
	
	/**
	 * 查询教务通知
	 *
	 * @param loginViewModel 登录
	 * @param page 			 第几页
	 * @param pageSize  	 每页数量
	 */
	@NonNull
	public static Notice[] queryNotice(@NonNull LoginViewModel loginViewModel, int page, int pageSize){
		String response = null;
		try{
			response = loginViewModel.doPost(QustAPI.SCHOOL_SYSTEM_NOTICE,
					"queryModel.showCount=" + pageSize + "&queryModel.currentPage=" + page + "&queryModel.sortName=cjsj&queryModel.sortOrder=desc"
			);
			if(!"".equals(response)){
				ArrayList<Notice> array = new ArrayList<>();
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					array.add(new Notice(item.getJSONObject(i)));
				}
				return array.toArray(new Notice[0]);
			}
		}catch(IOException ignored){
		
		}catch(JSONException e){
			LogUtil.Log(response, e);
		}
		return new Notice[0];
	}
	
	/**
	 * 查询考试
	 *
	 * @param loginViewModel 登录
	 * @param xnm            学年代码 20xx
	 * @param xqm            学期代码 12 | 3
	 */
	@NonNull
	public static Exam[] queryExam(@NonNull LoginViewModel loginViewModel, String xnm, String xqm){
		try{
			String response = WebUtil.doPost(
					loginViewModel.host + QustAPI.GET_EXAM,
					"JSESSIONID=" + loginViewModel.getCookie(),
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50", xnm, xqm)
			);
			if(!"".equals(response)){
				ArrayList<Exam> array = new ArrayList<>();
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					JSONObject j = item.getJSONObject(i);
					Exam exam = new Exam();
					exam.name = j.getString("kcmc");
					exam.time = j.getString("kssj");
					exam.place = j.getString("cdmc");
					array.add(exam);
				}
				return array.toArray(new Exam[0]);
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return new Exam[0];
	}
	
	/**
	 * 查询成绩
	 *
	 * @param loginViewModel 登录
	 * @param xnm            学年代码 20xx
	 * @param xqm            学期代码 12 | 3
	 */
	@NonNull
	public static Mark[] queryMark(@NonNull LoginViewModel loginViewModel, String xnm, String xqm){
		
		HashMap<String, Mark.Builder> markMap = new HashMap<>(8);
		
		try{
			String response = WebUtil.doPost(
					loginViewModel.host + QustAPI.GET_MARK,
					"JSESSIONID=" + loginViewModel.getCookie(),
					String.format("xnm=%s&xqm=%s&queryModel.showCount=50", xnm, xqm)
			);
			
			if(!"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					JSONObject js = item.getJSONObject(i);
					String name = js.getString("kcmc");
					if(markMap.containsKey(name)) continue;
					markMap.put(name, Mark.Builder.createFromJson(js));
				}
			}
			
			response = WebUtil.doPost(
					loginViewModel.host + QustAPI.GET_MARK_DETAIL,
					"JSESSIONID=" + loginViewModel.getCookie(),
					String.format("xnm=%s&xqm=%s&queryModel.showCount=100", xnm, xqm)
			);
			
			if(!"".equals(response)){
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					JSONObject js = item.getJSONObject(i);
					String name = js.getString("kcmc");
					
					Mark.Builder mark;
					if(markMap.containsKey(name)){
						mark = markMap.get(name);
					}else{
						mark = Mark.Builder.createFromJson(js);
						markMap.put(name, mark);
					}
					
					mark.addItemMark(js);
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			return markMap.values().stream().map(Mark.Builder::build).toArray(Mark[]::new);
		}else{
			Mark[] marks = new Mark[markMap.size()];
			Iterator<Mark.Builder> it = markMap.values().iterator();
			for(int i = 0; it.hasNext(); i++){
				marks[i] = it.next().build();
			}
			return marks;
		}
	}
	
	
}
