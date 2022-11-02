package com.qust.assistant.ui.fragment.school;

import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.model.lesson.LessonGroup;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.QustUtil.LessonUtil;
import com.qust.assistant.widget.lesson.LessonTable;

public class GetLessonTableFragment extends BaseSchoolFragment{

	private TextView weekTextView;
	
	private TextView termTextView;
	
	private LessonTable lessonTable;
	
	private FloatingActionButton saveButton;
	
	private boolean needSave;
	
	private String termText;
	
	private String startTime;
	
	private int totalWeek;
	
	private LessonGroup[][] lessonGroups;
	
	private LessonTableViewModel lessonTableViewModel;
	
	public GetLessonTableFragment(MainActivity activity){
		super(activity);
	}
	
	public GetLessonTableFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		startTime = LessonTableViewModel.getStartDay();
		
		lessonGroups = new LessonGroup[7][10];
		
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		
		weekTextView = findViewById(R.id.fragment_get_lesson_table_week);
		termTextView = findViewById(R.id.fragment_get_lesson_table_term);
		saveButton = findViewById(R.id.button_save);
		
		lessonTable = findViewById(R.id.fragment_get_lesson_table_preview);
		lessonTable.initAdapter(lessonGroups);
		lessonTable.setCurrentItem(LessonTableViewModel.getCurrentWeek() - 1);
		lessonTable.setLessonClickListener((week, count, lesson) -> { });
		lessonTable.setUpdateListener(() -> {
			int currentWeek = lessonTable.getCurrentItem();
			lessonTable.setAdapter(lessonTable.getAdapter());
			lessonTable.setCurrentItem(currentWeek);
		});
		
		lessonTable.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){ }
			@Override
			public void onPageSelected(int position){
				weekTextView.setText("第 " + (position + 1) + " 周");
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});

		weekTextView.setText("第 1 周");
		saveButton.setOnClickListener(v -> saveData());
		
		initYearAndTermPicker();
		
		setNeedSave(false);
	}
	
	@Override
	protected void doQuery(){
		
		sendMessage(App.UPDATE_DIALOG, "正在查询课表");
		
		String[] y = getYearAndTerm();
		
		LessonUtil.QueryLessonResult result = LessonUtil.queryLessonTable(loginViewModel, y[0], y[1]);
		
		if(result.message != null){
			sendMessage(App.DISMISS_TOAST, result.message);
		}else{
			termText = result.termText;
			startTime = result.startTime;
			totalWeek = result.totalWeek;
			lessonGroups = result.lessonGroups;
			
			activity.runOnUiThread(() -> {
				termTextView.setText(termText);
				lessonTable.initAdapter(lessonGroups, totalWeek, startTime);
				setNeedSave(true);
				dialog.dismiss();
				toast("获取课表成功！");
			});
		}
	}
	
	/**
	 * 设置是否需要保存课表
	 */
	private void setNeedSave(boolean _needSave){
		needSave = _needSave;
		if(needSave){
			saveButton.show();
		}else{
			saveButton.hide();
		}
	}
	
	private void saveData(){
		if(startTime == null || startTime.equals(LessonTableViewModel.getStartDay())){
			// 开学日期没查到或者与现在相同就不更新
			lessonTableViewModel.saveLessonData(null, totalWeek, lessonGroups);
			finish();
		}else{
			new MaterialDialog.Builder(activity).title("提示")
				.content("查询到的新课表开学日期与当前不一致, 是否更新开学日期？\n当前开学日期: " + LessonTableViewModel.getStartDay() + "\n查询开学日期: " + startTime)
				.positiveText("全部更新").onPositive((dialog, which) -> {
					lessonTableViewModel.saveLessonData(startTime, totalWeek, lessonGroups);
					dialog.dismiss();
					finish();
				})
				.negativeText("取消更新").onNegative((dialog, which) -> dialog.dismiss())
				.neutralText("仅更新课表").onNeutral((dialog, which) -> {
					lessonTableViewModel.saveLessonData(null, -1, lessonGroups);
					dialog.dismiss();
					finish();
				}).show();
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
		}else{
			return super.onBackPressed();
		}
	}

}
