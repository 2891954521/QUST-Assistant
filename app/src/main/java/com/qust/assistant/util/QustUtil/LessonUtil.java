package com.qust.assistant.util.QustUtil;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.ui.fragment.school.BaseSchoolFragment;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课表工具
 */
public class LessonUtil{
	
	/**
	 * 匹配学年信息
	 */
	private static final Pattern TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})\\)");
	
	private static final Pattern WEEK_TABLE_MATCHER = Pattern.compile("<tr class=\"tab-th-2\">(.*?)</tr>", Pattern.DOTALL);
	
	private static final Pattern WEEK_MATCHER = Pattern.compile("<th style=\"text-align: center\">(\\d+)</th>");
	
	private static final Pattern DAY_MATCHER = Pattern.compile("<tbody>\\s+<tr>(.*?)</tr>", Pattern.DOTALL);
	
	private static final Pattern DATE_MATCHER = Pattern.compile("<td id='(\\d{4}-\\d{2}-\\d{2})");
	
	/**
	 * 查询课程信息
	 * @param session 教务Cookie
	 * @param year 学年
	 * @param term 学期
	 */
	@NonNull
	public static QueryLessonResult queryLessonTable(String session, String year, String term){
		
		QueryLessonResult result = new QueryLessonResult();
		
		try{
			// 从教务获取本学年信息
			String response = WebUtil.doGet(LoginUtil.HOST + "/jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index",
					"JSESSIONID=" + session
			);
			if(!TextUtils.isEmpty(response)){
				// 学年信息
				Matcher matcher = TIME_MATCHER.matcher(response);
				if(matcher.find()){
					result.termText = matcher.group();
					result.startTime = matcher.group(3);
					try{
						result.totalWeek = DateUtil.calcWeekOffset(DateUtil.YMD.parse(result.startTime), DateUtil.YMD.parse(matcher.group(4)));
					}catch(ParseException ignored){
						result.startTime = null;
						result.totalWeek = -1;
					}
				}
				
				// 根据校历查找开学日期
				matcher = WEEK_TABLE_MATCHER.matcher(response);
				if(matcher.find()){
					int count = -1;
					Matcher w = WEEK_MATCHER.matcher(matcher.group());
					while(w.find()){
						count++;
						if("1".equals(w.group(1))){
							break;
						}
					}
					if(count != -1){
						Matcher m = DAY_MATCHER.matcher(response);
						if(m.find()){
							int c = 0;
							Matcher d = DATE_MATCHER.matcher(m.group(1));
							while(d.find()){
								if(c++ == count){
									result.startTime = d.group(1);
								}
							}
						}
					}
				}
			}
			// 从教务查询课表
			response = WebUtil.doPost(LoginUtil.HOST + "/jwglxt/kbcx/xskbcx_cxXsKb.html",
					"JSESSIONID=" + session,
					"xnm=" + year +"&xqm=" + term + "&kzlx=ck"
			);
			
			if(TextUtils.isEmpty(response)){
				result.message = "获取课表失败";
				return result;
			}
			
			result.lessonGroups = new LessonGroup[7][10];
			if(!LessonTableViewModel.loadFromJson(new JSONObject(response), result.lessonGroups)){
				result.message = "解析课表信息失败";
			}
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			result.message = "获取课表失败";
		}
		return result;
	}
	
	/**
	 * 获取当前学期
	 * @param entranceTime 入学年份
	 * @return 学年索引
	 */
	public static int getCurrentYear(int entranceTime){
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(y < entranceTime){
			return 0;
		}else{
			return Math.min((y - entranceTime) * 2 - (calendar.get(Calendar.MONTH) < Calendar.AUGUST ? 1 : 0), BaseSchoolFragment.TERM_NAME.length - 1);
		}
	}
	
	/**
	 * 查询课表完成后的结果
	 */
	public static class QueryLessonResult{
		/**
		 * 消息
		 */
		@Nullable
		public String message;
		/**
		 * 学期文本
		 */
		public String termText;
		/**
		 * 开学时间
		 */
		public String startTime;
		/**
		 * 总周数
		 */
		public int totalWeek;
		/**
		 * 课表
		 */
		public LessonGroup[][] lessonGroups;
		
	}
	
}
