package com.qust.fragment.lesson;

import android.view.LayoutInflater;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qust.assistant.R;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.base.fragment.BaseEAFragment;
import com.qust.lesson.LessonTable;
import com.qust.lesson.LessonTableModel;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.QueryLessonResult;
import com.qust.lesson.view.LessonTableView;

public class GetLessonTableFragment extends BaseEAFragment{
	
	private boolean needSave;
	
	private String termText;
	
	private TextView weekTextView, termTextView;
	
	/**
	 * 查询方案
	 */
	private NumberPicker typePicker;
	
	private FloatingActionButton saveButton;
	
	private LessonTable lessonTable;
	
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
		
		weekTextView = findViewById(R.id.fragment_get_lesson_table_week);
		termTextView = findViewById(R.id.fragment_get_lesson_table_term);
		saveButton = findViewById(R.id.button_save);
		
		lessonTableView = findViewById(R.id.fragment_get_lesson_table_preview);
		lessonTableView.initAdapter(lessonTable);
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
				weekTextView.setText("第 " + (position + 1) + " 周");
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});

		weekTextView.setText("第 1 周");
		saveButton.setOnClickListener(v -> saveData());
		
		typePicker = findViewById(R.id.fragment_school_type);
		typePicker.setWrapSelectorWheel(false);
		typePicker.setDisplayedValues(new String[]{ "个人课表", "班级课表" });
		typePicker.setMinValue(0);
		typePicker.setMaxValue(1);

		initYearAndTermPicker();
		
		setNeedSave(false);
	}
	
	@Override
	protected void doQuery(){
		dialog.setContent("正在查询课表");
		dialog.show();
		
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
	
	private void saveData(){
		if(lessonTable.getStartDay().equals(lessonTableViewModel.getStartDay())){
			// 开学日期没查到或者与现在相同就不更新
			lessonTableViewModel.saveLessonData(lessonTable);
			finish();
		}else{
			new MaterialDialog.Builder(activity).title("提示")
					.content("查询到的新课表开学日期与当前不一致, 是否更新开学日期？\n当前开学日期: "
							+ DateUtil.YMD.format(lessonTableViewModel.getStartDay())
							+ "\n查询开学日期: "
							+ DateUtil.YMD.format(lessonTable.getStartDay())
					).positiveText("全部更新").onPositive((dialog, which) -> {
						lessonTableViewModel.saveLessonData(lessonTable);
						dialog.dismiss();
						finish();
					})
					.negativeText("取消更新").onNegative((dialog, which) -> dialog.dismiss())
					.neutralText("仅更新课表").onNeutral((dialog, which) -> {
						lessonTable.setStartDay(lessonTableViewModel.getStartDay());
						lessonTable.setTotalWeek(lessonTable.getTotalWeek());
						lessonTableViewModel.saveLessonData(lessonTable);
						dialog.dismiss();
						finish();
					}).show();
		}
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
