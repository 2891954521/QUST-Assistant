package com.university.assistant.ui.school;

import android.content.Intent;
import android.os.Bundle;

import com.university.assistant.App;
import com.university.assistant.Lesson.LessonData;
import com.university.assistant.Lesson.LessonGroup;
import com.university.assistant.R;
import com.university.assistant.util.DialogUtil;
import com.university.assistant.util.FileUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.WebUtil;
import com.university.assistant.widget.LessonTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;

public class GetLessonTableActivity extends BaseSchoolActivity{
	
	private LessonTable lessonTable;
	
	private LessonGroup[][] lessonGroups;
	
	private boolean needSave;
	
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
		try{
			String[] y = getYearAndTerm();
			String response = WebUtil.doPost(
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
					}).show();
		}else super.onBackPressed();
	}
}
