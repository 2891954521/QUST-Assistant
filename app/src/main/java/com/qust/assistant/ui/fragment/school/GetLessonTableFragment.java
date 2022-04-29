package com.qust.assistant.ui.fragment.school;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
	
	/**
	 * 匹配学年信息
	 */
	private static final Pattern TIME_MATCHER = Pattern.compile("([0-9]{4}-[0-9]{4})学年([0-9])学期\\((\\d{4}-\\d{2}-\\d{2})至(\\d{4}-\\d{2}-\\d{2})\\)");
	
	private static final Pattern WEEK_TABLE_MATCHER = Pattern.compile("<tr class=\"tab-th-2\">(.*?)</tr>", Pattern.DOTALL);
	
	private static final Pattern WEEK_MATCHER = Pattern.compile("<th style=\"text-align: center\">(\\d+)</th>");
	
	private static final Pattern DAY_MATCHER = Pattern.compile("<tbody>\\s+<tr>(.*?)</tr>", Pattern.DOTALL);
	
	private static final Pattern DATE_MATCHER = Pattern.compile("<td id='(\\d{4}-\\d{2}-\\d{2})");

	private TextView termTextView;
	
	private TextView startTextView;
	
	private LessonTable lessonTable;
	
	private LessonGroup[][] lessonGroups;
	
	private boolean needSave;
	
	private String startTime;
	
	private String termText;
	
	private int totalWeek;
	
	public GetLessonTableFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		initYearAndTermPicker();
		
		termTextView = findViewById(R.id.fragment_get_lesson_table_term);
		
		startTextView = findViewById(R.id.fragment_get_lesson_table_start);
		
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
		
		addMenuItem(inflater, R.drawable.ic_done, v -> saveData());
		
	}
	
	@Override
	protected void doQuery(String session){
		
		sendMessage(App.UPDATE_DIALOG, "正在查询课表");
		
		String[] y = getYearAndTerm();
		
		try{
			// 从教务获取本学年信息
			String response = WebUtil.doGet(LoginUtil.HOST + "/jwglxt/xtgl/index_cxAreaFive.html?localeKey=zh_CN&gnmkdm=index",
				"JSESSIONID=" + session
			);
			if(!TextUtils.isEmpty(response)){
				// 学年信息
				Matcher matcher = TIME_MATCHER.matcher(response);
				if(matcher.find()){
					termText = matcher.group();
					startTime = matcher.group(3);
					try{
						totalWeek = DateUtil.calcWeekOffset(DateUtil.YMD.parse(startTime), DateUtil.YMD.parse(matcher.group(4)));
					}catch(ParseException ignored){
						startTime = null;
						totalWeek = -1;
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
									startTime = d.group(1);
								}
							}
						}
					}
				}
			}
			// 从教务查询课表
			response = WebUtil.doPost(LoginUtil.HOST + "/jwglxt/kbcx/xskbcx_cxXsKb.html",
				"JSESSIONID=" + session,
				"xnm=" + y[0] +"&xqm=" + y[1] + "&kzlx=ck"
			);
			
			if(TextUtils.isEmpty(response)){
				sendMessage(App.DISMISS_TOAST, "获取课表失败！");
				return;
			}
			
			lessonGroups = new LessonGroup[7][10];
			if(LessonData.getInstance().loadFromJson(new JSONObject(response), lessonGroups)){
				FileUtil.writeFile(new File(activity.getExternalFilesDir("LessonTable"),"data.json"), response);
				activity.runOnUiThread(() -> {
					needSave = true;
					termTextView.setText(termText);
					if(startTime != null){
						startTextView.setText("开学日期: " + startTime);
					}
					lessonTable.initAdapter(lessonGroups);
					dialog.dismiss();
					toast("获取课表成功！");
				});
			}else{
				sendMessage(App.DISMISS_TOAST, "获取课表失败！");
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
			new MaterialDialog.Builder(activity)
					.title("提示")
					.content("查询到的新课表开学日期与当前不一致, 是否更新开学日期？\n当前开学日期: " + LessonData.getInstance().getStartDay() + "\n查询开学日期: " + startTime)
					.positiveText("全部更新")
					.onPositive((dialog, which) -> {
						LessonData.getInstance().setStartDay(startTime);
						updateLesson();
						dialog.dismiss();
					}).negativeText("取消更新")
					.onNegative((dialog, which) -> {
						dialog.dismiss();
					}).neutralText("仅更新课表").onNeutral((dialog, which) -> {
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
				dialog.dismiss();
				saveData();
			}).onNegative((dialog, which) -> {
				dialog.dismiss();
				finish();
			}).show();
			return false;
		}else return super.onBackPressed();
	}

}
