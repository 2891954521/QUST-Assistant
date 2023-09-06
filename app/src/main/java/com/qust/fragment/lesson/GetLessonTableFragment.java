package com.qust.fragment.lesson;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qust.account.NeedLoginException;
import com.qust.assistant.R;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.base.fragment.BaseEAFragment;
import com.qust.lesson.LessonTable;
import com.qust.lesson.LessonTableModel;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.QueryLessonResult;
import com.qust.lesson.view.LessonTableView;
import com.qust.widget.BottomDialog;

public class GetLessonTableFragment extends BaseEAFragment{
	
	private boolean needSave;
	
	private String termText;
	
	private TextView weekTextView, termTextView, startTimeTextView;
	
	/**
	 * 查询方案
	 */
	private NumberPicker typePicker;
	
	private FloatingActionButton saveButton;
	
	private LessonTable lessonTable;
	
	private BottomDialog bottomDialog;
	
	private LessonTableView lessonTableView;
	
	private LessonTableViewModel lessonTableViewModel;
	
	public GetLessonTableFragment(){
		super();
	}
	
	public GetLessonTableFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		lessonTable = lessonTableViewModel.getLessonTable();
		
		startTimeTextView = findViewById(R.id.get_lesson_start_time);
		weekTextView = findViewById(R.id.fragment_get_lesson_table_week);
		termTextView = findViewById(R.id.fragment_get_lesson_table_term);
		saveButton = findViewById(R.id.button_save);
		
		lessonTableView = findViewById(R.id.fragment_get_lesson_table_preview);
		lessonTableView.setCurrentItem(lessonTableViewModel.getCurrentWeek() - 1);
		lessonTableView.setUpdateListener(() -> {
			int currentWeek = lessonTableView.getCurrentItem();
			lessonTableView.setAdapter(lessonTableView.getAdapter());
			lessonTableView.setCurrentItem(currentWeek);
		});
		
		lessonTableView.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){ }
			@Override
			public void onPageSelected(int position){
				weekTextView.setText(getString(R.string.text_week, String.valueOf(position + 1)));
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});

		weekTextView.setText(getString(R.string.text_week, "1"));
		saveButton.setOnClickListener(v -> showSaveData());
		
		typePicker = findViewById(R.id.fragment_school_type);
		typePicker.setWrapSelectorWheel(false);
		typePicker.setDisplayedValues(new String[]{ "个人课表", "班级课表" });
		typePicker.setMinValue(0);
		typePicker.setMaxValue(1);
	
		bottomDialog = new BottomDialog(activity, findViewById(R.id.layout_bottom_back), findViewById(R.id.layout_bottom_content), 0f);
		findViewById(R.id.btn_cancel).setOnClickListener(v -> {
			bottomDialog.hide();
			saveButton.show();
		});
		findViewById(R.id.btn_ok).setOnClickListener(this::saveData);
		
		initYearAndTermPicker();
		
		setNeedSave(false);
	}
	
	@Override
	protected void doQuery() throws NeedLoginException{
		
		String[] y = getYearAndTerm();
		
		QueryLessonResult result;
		if(typePicker.getValue() == 0){
			result = LessonTableModel.queryLessonTable(eaViewModel, y[0], y[1]);
		}else{
			result = LessonTableModel.queryClassLessonTable(eaViewModel, entranceTime, y[0], y[1]);
		}
		
		if(result.message != null){
			activity.runOnUiThread(() -> {
				dialog.dismiss();
				DialogUtil.getBaseDialog(activity).title("提示").content(result.message).onPositive((dialog, which) -> dialog.dismiss()).show();
			});
		}else{
			termText = result.termText;
			lessonTable = result.lessonTable;
			
			activity.runOnUiThread(() -> {
				termTextView.setText(termText);
				
				startTimeTextView.setText(getString(R.string.text_get_lesson_start_time,
						DateUtil.YMD.format(lessonTableViewModel.getStartDay()),
						DateUtil.YMD.format(lessonTable.getStartDay())
				));
				
				lessonTableView.initAdapter(lessonTable);
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
	
	private void showSaveData(){
		if(lessonTableViewModel.getTotalWeek() == 0){
			// 当前没有课表，直接保存
			lessonTableViewModel.saveLessonData(lessonTable);
			finish();
			return;
		}
		
		saveButton.hide();
		bottomDialog.show();
	}
	
	private void saveData(View v){
		LessonTable newLessonTable = lessonTable;
		
		if(!((CheckBox)findViewById(R.id.get_lesson_save_start_time)).isChecked()){
			newLessonTable.setStartDay(lessonTableViewModel.getStartDay());
		}
		
		if(((CheckBox)findViewById(R.id.get_lesson_keep_lesson)).isChecked()){
			newLessonTable.mergeUserDefinedLesson(lessonTableViewModel.getLessonTable());
		}
		
		lessonTableViewModel.saveLessonData(newLessonTable);
		
		finish();
	}
	
	@Override
	public String getName(){
		return "课表查询";
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
				showSaveData();
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
