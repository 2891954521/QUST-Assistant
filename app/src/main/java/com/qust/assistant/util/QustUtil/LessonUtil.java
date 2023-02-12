package com.qust.assistant.util.QustUtil;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.model.lesson.LessonGroup;
import com.qust.assistant.ui.fragment.school.BaseSchoolFragment;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.vo.QueryLessonResult;

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
	
	private static final Pattern XQHID_MATCHER = Pattern.compile("<select name=\"xqh_id\".*?</select>", Pattern.DOTALL);
	private static final Pattern ZYHID_MATCHER = Pattern.compile("<select name=\"zyh_id\".*?</select>", Pattern.DOTALL);
	private static final Pattern BHID_MATCHER = Pattern.compile("<select name=\"bh_id\".*?</select>", Pattern.DOTALL);
	
	private static final Pattern OPTION_MATCHER = Pattern.compile("<option value=\"(.*?)\" selected=\"selected\">");
	
	/**
	 * 查询课表信息
	 * @param year 学年
	 * @param term 学期
	 */
	@NonNull
	public static QueryLessonResult queryLessonTable(LoginViewModel loginViewModel, String year, String term){
		
		QueryLessonResult result = getSchoolYearData(loginViewModel, new QueryLessonResult());
		
		try{
			// 从教务查询课表
			String response = WebUtil.doPost(loginViewModel.host + QustAPI.GET_LESSON_TABLE,
					"JSESSIONID=" + loginViewModel.getCookie(),
					"xnm=" + year + "&xqm=" + term + "&kzlx=ck"
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
	 * 查询课表信息备用方案
	 * @param entranceTime 入学年份（年级，有备用方案可以从html里获取）
	 * @param year 学年
	 * @param term 学期
	 */
	@NonNull
	public static QueryLessonResult queryClassLessonTable(LoginViewModel loginViewModel, int entranceTime, String year, String term){
		
		QueryLessonResult result = getSchoolYearData(loginViewModel, new QueryLessonResult());
		
		try{
			// 推荐课表打印界面
			String response = WebUtil.doGet(loginViewModel.host + QustAPI.RECOMMENDED_LESSON_TABLE_PRINTING, "JSESSIONID=" + loginViewModel.getCookie());
			
			// 从HTML里获取校区ID，专业号ID，班级ID
			String xqh_id = null;
			String zyh_id = null;
			String bh_id = null;
			
			Matcher matcher = XQHID_MATCHER.matcher(response);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) xqh_id = option.group(1);
			}
			matcher = ZYHID_MATCHER.matcher(response);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) zyh_id = option.group(1);
			}
			matcher = BHID_MATCHER.matcher(response);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) bh_id = option.group(1);
			}
			
			if(xqh_id == null || zyh_id == null || bh_id == null){
				result.message = "获取查询参数失败";
				return result;
			}
			
			String param = "tjkbzdm=1&tjkbzxsdm=1&xnm=" + year + "&xqm=" + term + "&njdm_id=" + entranceTime + "&xqh_id=" + xqh_id + "&zyh_id=" + zyh_id+ "&bh_id=" + bh_id;
			
			response = WebUtil.doPost(loginViewModel.host + QustAPI.GET_CLASS_LESSON_TABLE,
					"JSESSIONID=" + loginViewModel.getCookie(),
					param
			);
			
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
	 * 获取学年信息
	 */
	private static QueryLessonResult getSchoolYearData(LoginViewModel loginViewModel, @NonNull QueryLessonResult result){
		try{
			// 从教务获取本学年信息
			String response = WebUtil.doGet(loginViewModel.host + QustAPI.SCHOOL_YEAR_DATA, "JSESSIONID=" + loginViewModel.getCookie());
			if(TextUtils.isEmpty(response)){
				return result;
			}
			
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
				
				Matcher w = WEEK_MATCHER.matcher(matcher.group());
				if(!w.find()) return result;
				
				int count = 0;
				do{
					if("1".equals(w.group(1))) break;
					count++;
				}while(w.find());
				
				Matcher m = DAY_MATCHER.matcher(response);
				if(m.find()){
					int c = 0;
					Matcher d = DATE_MATCHER.matcher(m.group(1));
					while(d.find()){
						if(c++ == count){
							result.startTime = d.group(1);
							break;
						}
					}
				}
			}
		}catch(IOException ignored){
		}catch(NullPointerException e){
			LogUtil.Log(e);
		}
		return result;
	}
	
}
