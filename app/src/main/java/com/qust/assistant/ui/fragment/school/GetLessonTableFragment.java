package com.qust.assistant.ui.fragment.school;

import android.content.Intent;
import android.view.LayoutInflater;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.LoginUtil;
import com.qust.assistant.util.WebUtil;
import com.qust.assistant.widget.LessonTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetLessonTableFragment extends BaseSchoolFragment{
	
	private static final Pattern TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})");
	
	private LessonTable lessonTable;
	
	private LessonGroup[][] lessonGroups;
	
	private boolean needSave;
	
	private String startTime;
	
	private int totalWeek;
	
	public GetLessonTableFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		initYearAndTermPicker();
		
		lessonGroups = new LessonGroup[7][10];
		
		lessonTable = findViewById(R.id.fragment_get_lesson_table_preview);
		lessonTable.initAdapter(lessonGroups);
		lessonTable.setCurrentItem(LessonData.getInstance().getCurrentWeek() - 1);
		lessonTable.setLessonClickListener((week, count, lesson) -> { });
		lessonTable.setUpdateListener(() -> {
			int currentWeek = lessonTable.getCurrentItem();
			lessonTable.setAdapter(lessonTable.getAdapter());
			lessonTable.setCurrentItem(currentWeek);
		});
		
		setSlidingParam(null, lessonTable);
		
		addMenuItem(inflater, R.drawable.ic_done, v -> saveData());
		
	}
	
	@Override
	protected void doQuery(String session){
		
		sendMessage(App.UPDATE_DIALOG, "正在查询课表");
		
		try{
			String[] y = getYearAndTerm();
			String response = WebUtil.doGet(
					LoginUtil.HOST + "/jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index",
				"JSESSIONID=" + session
			);
			if(!"".equals(response)){
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
					LoginUtil.HOST + "/jwglxt/kbcx/xskbcx_cxXsKb.html",
				"JSESSIONID=" + session ,
				"xnm=" + y[0] +"&xqm=" + y[1] + "&kzlx=ck"
			);
			if(!"".equals(response)){
				lessonGroups = new LessonGroup[7][10];
				if(LessonData.getInstance().loadFromJson(new JSONObject(response),lessonGroups)){
					FileUtil.writeFile(new File(activity.getExternalFilesDir("LessonTable"),"data.json"), response);
					activity.runOnUiThread(() -> {
						needSave = true;
						lessonTable.initAdapter(lessonGroups);
						dialog.dismiss();
						toast("获取课表成功！");
					});
				}
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			sendMessage(App.DISMISS_TOAST, "获取课表失败！");
		}
	}
	
	private void updateLesson(){
		LessonData data = LessonData.getInstance();
		data.setLessonGroups(lessonGroups);
		data.saveLessonData();
		activity.sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
		finish();
	}
	
	private void saveData(){
		if(totalWeek != 1){
			LessonData.getInstance().setTotalWeek(totalWeek);
		}
		if(startTime != null && !startTime.equals(LessonData.getInstance().getStartDay())){
			DialogUtil.getBaseDialog(activity).title("提示").content("开学日期不一致, 是否更新？\n当前: " + LessonData.getInstance().getStartDay() + "\n最新: " + startTime).onPositive((dialog, which) -> {
				LessonData.getInstance().setStartDay(startTime);
				updateLesson();
				dialog.dismiss();
			}).onNegative((dialog, which) -> {
				updateLesson();
				dialog.dismiss();
			}).show();
		}else{
			updateLesson();
		}
	}
	
	@Override
	protected String getName(){
		return "查课表";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_get_lesson_table;
	}
	
	@Override
	public boolean onBackPressed(){
		if(needSave){
			DialogUtil.getBaseDialog(activity).title("提示").content("课表信息未保存，是否保存？").onPositive((dialog, which) -> {
				saveData();
				dialog.dismiss();
			}).onNegative((dialog, which) -> {
				finish();
				dialog.dismiss();
			}).show();
			return false;
		}else return super.onBackPressed();
	}

}
