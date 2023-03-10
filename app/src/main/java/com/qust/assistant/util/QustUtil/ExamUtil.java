package com.qust.assistant.util.QustUtil;

import androidx.annotation.NonNull;

import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.Exam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 考试查询工具类
 */
public class ExamUtil{
	
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
	
}
