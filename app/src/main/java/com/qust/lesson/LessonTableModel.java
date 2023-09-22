package com.qust.lesson;

import androidx.annotation.NonNull;

import com.qust.QustAPI;
import com.qust.account.NeedLoginException;
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.base.fragment.BaseEAFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Response;

/**
 * 课表功能模块
 */
public class LessonTableModel{
	
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
	public static QueryLessonResult queryLessonTable(EAViewModel eaViewModel, String year, String term) throws NeedLoginException{
		
		QueryLessonResult result = getSchoolYearData(eaViewModel, new QueryLessonResult());
		
		try(Response response = eaViewModel.post(QustAPI.GET_LESSON_TABLE, new FormBody.Builder().add("xnm", year).add("xqm", term).add("kzlx", "ck").build())){
			String html = response.body().string();
			JSONObject js = new JSONObject(html);
			
			if(!js.has("xsxx")){
				result.message = "获取课表失败：该学年学期无您的注册信息";
				return result;
			}
			
			if(Boolean.parseBoolean(js.optString("xnxqsfkz"))){
				result.message = "获取课表失败：该学年学期课表当前时间段不允许查看";
				return result;
			}
			
			int kblen = js.getJSONArray("kbList").length();
			int sjklen = js.getJSONArray("sjkList").length();
			int jxhjkclen = js.getJSONArray("jxhjkcList").length();
			boolean xkkg = js.optBoolean("xkkg", false);       // 选课开关
			boolean jfckbkg = js.optBoolean("jfckbkg", false); // 缴费查课表开关
			
			if(kblen == 0 && sjklen == 0 && jxhjkclen == 0 && xkkg && jfckbkg){
				result.message = "获取课表失败：该学年学期尚无您的课表";
				return result;
			}else if(!xkkg){
				result.message = "获取课表失败：该学年学期的课表尚未开放";
				return result;
			}else if(!jfckbkg){
				result.message = "获取课表失败：缴费后可查询";
				return result;
			}
			
			if(!result.lessonTable.loadFromJson(js)){
				result.message = "解析课表信息失败";
			}
			
		}catch(IOException e){
			result.message = "获取课表失败, 网络异常";
		}catch(JSONException e){
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
	public static QueryLessonResult queryClassLessonTable(EAViewModel eaViewModel, int entranceTime, String year, String term) throws NeedLoginException{
		
		QueryLessonResult result = getSchoolYearData(eaViewModel, new QueryLessonResult());
		
		try(Response response = eaViewModel.get(QustAPI.RECOMMENDED_LESSON_TABLE_PRINTING)){
			
			String html = response.body().string();
			
			// 从HTML里获取校区ID，专业号ID，班级ID
			String xqh_id = null;
			String zyh_id = null;
			String bh_id = null;
			
			Matcher matcher = XQHID_MATCHER.matcher(html);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) xqh_id = option.group(1);
			}
			matcher = ZYHID_MATCHER.matcher(html);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) zyh_id = option.group(1);
			}
			matcher = BHID_MATCHER.matcher(html);
			if(matcher.find()){
				Matcher option = OPTION_MATCHER.matcher(matcher.group());
				if(option.find()) bh_id = option.group(1);
			}
			
			if(xqh_id == null || zyh_id == null || bh_id == null){
				result.message = "获取查询参数失败";
				return result;
			}
			
			try(Response response1 = eaViewModel.post(QustAPI.GET_CLASS_LESSON_TABLE, new FormBody.Builder()
					.add("tjkbzdm", "1")
					.add("tjkbzxsdm", "1")
					.add("xnm", year)
					.add("xqm", term)
					.add("njdm_id", String.valueOf(entranceTime))
					.add("xqh_id", xqh_id)
					.add("zyh_id", zyh_id)
					.add("bh_id", bh_id)
					.build())
			){
				if(!result.lessonTable.loadFromJson(new JSONObject(response1.body().string()))){
					result.message = "解析课表信息失败";
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			result.message = "获取课表失败";
		}
		return result;
	}
	
	/**
	 * 根据入学年份计算当前学期索引
	 * @param entranceTime 入学年份
	 * @return 学期索引
	 */
	public static int getCurrentYear(int entranceTime){
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(y < entranceTime){
			return 0;
		}else{
			return Math.min((y - entranceTime) * 2 - (calendar.get(Calendar.MONTH) < Calendar.AUGUST ? 1 : 0), BaseEAFragment.TERM_NAME.length - 1);
		}
	}
	
	/**
	 * 获取学年信息
	 */
	private static QueryLessonResult getSchoolYearData(EAViewModel eaViewModel, @NonNull QueryLessonResult lessonResult) throws NeedLoginException{
		try{
			// 从教务获取本学年信息
			String response = eaViewModel.get(QustAPI.EA_YEAR_DATA).body().string();
			
			// 学年信息
			Matcher matcher = TIME_MATCHER.matcher(response);
			if(matcher.find()){
				lessonResult.termText = matcher.group();
				Date startDay = new Date();
				try{
					startDay = DateUtil.YMD.parse(matcher.group(3));
				}catch(ParseException | NullPointerException ignored){ }
				
				lessonResult.lessonTable.setStartDay(startDay);
				
				try{
					lessonResult.lessonTable.setTotalWeek(DateUtil.calcWeekOffset(startDay, DateUtil.YMD.parse(matcher.group(4))));
				}catch(ParseException ignored){
					lessonResult.lessonTable.setTotalWeek(1);
				}
			}
			
			// 根据校历查找开学日期
			matcher = WEEK_TABLE_MATCHER.matcher(response);
			if(matcher.find()){
				
				Matcher w = WEEK_MATCHER.matcher(matcher.group());
				if(!w.find()) return lessonResult;
				
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
							lessonResult.lessonTable.setStartDay(d.group(1));;
							break;
						}
					}
				}
			}
		}catch(IOException ignored){
		
		}catch(NullPointerException | ParseException e){
			LogUtil.Log(e);
		}
		
		return lessonResult;
	}
	
}
