package com.qust.model;

import android.os.Build;

import androidx.annotation.NonNull;

import com.qust.QustAPI;
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.util.LogUtil;
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

import okhttp3.FormBody;
import okhttp3.Response;

public class QUSTQueryModel{
	
	/**
	 * 查询一条教务通知
	 *
	 * @param eaViewModel 登录
	 */
	@NonNull
	public static Notice[] queryNotice(@NonNull EAViewModel eaViewModel){
		return queryNotice(eaViewModel, 1, 1);
	}
	
	/**
	 * 查询教务通知
	 *
	 * @param eaViewModel 	 登录
	 * @param page 			 第几页
	 * @param pageSize  	 每页数量
	 */
	@NonNull
	public static Notice[] queryNotice(@NonNull EAViewModel eaViewModel, int page, int pageSize){
		String json = null;
		try(Response response = eaViewModel.postSync(QustAPI.EA_SYSTEM_NOTICE, new FormBody.Builder()
				.add("queryModel.showCount", String.valueOf(pageSize))
				.add("queryModel.currentPage", String.valueOf(page))
				.add("queryModel.sortName", "cjsj")
				.add("queryModel.sortOrder", "desc")
				.build())){
			
			json = response.body().string();
			
			ArrayList<Notice> array = new ArrayList<>();
			JSONArray item = new JSONObject(json).getJSONArray("items");
			for(int i = 0; i < item.length(); i++) array.add(new Notice(item.getJSONObject(i)));
			
			return array.toArray(new Notice[0]);
			
		}catch(IOException ignored){
		}catch(JSONException e){
			LogUtil.Log(json, e);
		}
		return new Notice[0];
	}
	
	/**
	 * 查询考试
	 *
	 * @param eaViewModel    登录
	 * @param xnm            学年代码 20xx
	 * @param xqm            学期代码 12 | 3
	 */
	@NonNull
	public static Exam[] queryExam(@NonNull EAViewModel eaViewModel, String xnm, String xqm){
		String json = null;
		
		try(Response response = eaViewModel.postSync(QustAPI.GET_EXAM, new FormBody.Builder()
				.add("xnm", xnm)
				.add("xqm", xqm)
				.add("queryModel.showCount", "50")
				.build())){
			json = response.body().string();
			ArrayList<Exam> array = new ArrayList<>();
			JSONArray item = new JSONObject(json).getJSONArray("items");
			for(int i = 0; i < item.length(); i++){
				JSONObject j = item.getJSONObject(i);
				Exam exam = new Exam();
				exam.name = j.getString("kcmc");
				exam.time = j.getString("kssj");
				exam.place = j.getString("cdmc");
				array.add(exam);
			}
			return array.toArray(new Exam[0]);
			
		}catch(IOException ignore){
		}catch(JSONException e){
			LogUtil.Log(json, e);
		}
		return new Exam[0];
	}
	
	/**
	 * 查询成绩
	 *
	 * @param eaViewModel 登录
	 * @param xnm            学年代码 20xx
	 * @param xqm            学期代码 12 | 3
	 */
	@NonNull
	public static Mark[] queryMark(@NonNull EAViewModel eaViewModel, String xnm, String xqm){
		
		HashMap<String, Mark.Builder> markMap = new HashMap<>(8);
		
		String json = null;
		
		try(Response response = eaViewModel.postSync(QustAPI.GET_MARK, new FormBody.Builder()
				.add("xnm", xnm)
				.add("xqm", xqm)
				.add("queryModel.showCount", "50")
				.build())){
			
			json = response.body().string();
			
			JSONArray item = new JSONObject(json).getJSONArray("items");
			for(int i = 0; i < item.length(); i++){
				JSONObject js = item.getJSONObject(i);
				String name = js.getString("kcmc");
				if(markMap.containsKey(name)) continue;
				markMap.put(name, Mark.Builder.createFromJson(js));
			}
			
			try(Response response1 = eaViewModel.postSync(QustAPI.GET_MARK_DETAIL, new FormBody.Builder()
					.add("xnm", xnm)
					.add("xqm", xqm)
					.add("queryModel.showCount", "100")
					.build())){
				
				json = response1.body().string();
				item = new JSONObject(json).getJSONArray("items");
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
			
		}catch(IOException ignored){
		}catch(JSONException e){
			LogUtil.Log(json, e);
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
