package com.qust.assistant.ui.school;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.widget.LessonTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;

public class GetLessonTableActivity extends BaseSchoolActivity{
	
	private static final Pattern TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})");
	
	private LessonTable lessonTable;
	
	private LessonGroup[][] lessonGroups;
	
	private boolean needSave;
	
	private String startTime;
	
	private int totalWeek;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		initYearAndTermPicker();
		
		lessonGroups = new LessonGroup[7][10];
		
		findViewById(R.id.activity_get_lesson_table_done).setOnClickListener(v -> updateLesson());
		
		lessonTable = findViewById(R.id.activity_get_lesson_table_preview);
		lessonTable.initAdapter(lessonGroups);
		lessonTable.setCurrentItem(LessonData.getInstance().getCurrentWeek() - 1);
		lessonTable.setLessonClickListener((week, count, lesson) -> { });
		lessonTable.setUpdateListener(() -> {
			int currentWeek = lessonTable.getCurrentItem();
			lessonTable.setAdapter(lessonTable.getAdapter());
			lessonTable.setCurrentItem(currentWeek);
		});
		
	}
	
	@Override
	protected String getName(){
		return "查课表";
	}
	
	@Override
	protected int getLayout(){
		return R.layout.activity_get_lesson_table;
	}
	
	@Override
	protected void doQuery(String session){
		Message message = new Message();
		message.obj = "正在查询课表";
		handler.sendMessage(message);
		
		try{
			String[] y = getYearAndTerm();
			String response = WebUtil.doGet(
				"http://jwglxt.qust.edu.cn/jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index",
				"JSESSIONID=" + session
			);
			if(response != null && !"".equals(response)){
				Matcher matcher = TIME_MATCHER.matcher(response);
				if(matcher.find()){
					startTime = matcher.group(3);
					String endTime = matcher.group(4);
					try{
						totalWeek = DateUtil.calcWeekOffset(DateUtil.YMD.parse(startTime), DateUtil.YMD.parse(endTime));
					}catch(ParseException ignored){
						startTime = null;
						totalWeek = -1;
					}
				}
			}
				
			response = WebUtil.doPost(
				"http://jwglxt.qust.edu.cn/jwglxt/kbcx/xskbcx_cxXsKb.html",
				"JSESSIONID=" + session ,
				"xnm=" + y[0] +"&xqm=" + y[1] + "&kzlx=ck"
			);
			if(response != null && !"".equals(response)){
				lessonGroups = new LessonGroup[7][10];
				if(LessonData.getInstance().loadFromJson(new JSONObject(response),lessonGroups)){
					FileUtil.writeFile(new File(getExternalFilesDir("LessonTable"),"data.json"),response);
					runOnUiThread(() -> {
						needSave = true;
						lessonTable.initAdapter(lessonGroups);
						dialog.dismiss();
						toast("获取课表成功！");
					});
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			runOnUiThread(() -> {
				dialog.dismiss();
				toast("获取课表失败！");
			});
		}
	}
	
	private void updateLesson(){
		if(startTime != null && totalWeek != -1){
			LessonData.getInstance().setStartDay(startTime);
			LessonData.getInstance().setTotalWeek(totalWeek);
		}
		LessonData data = LessonData.getInstance();
		data.setLessonGroups(lessonGroups);
		data.saveLessonData();
		sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
		finish();
	}
	
	@Override
	public void onBackPressed(){
		if(needSave){
			DialogUtil.getBaseDialog(this).title("提示").content("课表信息未保存，是否保存？")
				.onPositive((dialog,which) -> {
					updateLesson();
					dialog.dismiss();
				})
				.onNegative((dialog, which) -> super.onBackPressed())
				.show();
		}else super.onBackPressed();
	}
}
