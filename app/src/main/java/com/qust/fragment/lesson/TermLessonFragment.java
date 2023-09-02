package com.qust.fragment.lesson;

import android.view.LayoutInflater;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qust.assistant.R;
import com.qust.assistant.widget.swipe.SwipeTextView;
import com.qust.base.fragment.BaseFragment;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.view.LessonTableView;
import com.qust.widget.LessonEditor;

public class TermLessonFragment extends BaseFragment{
	
	/**
	 * 周数显示
 	 */
	private SwipeTextView weekText;
	
	/**
	 * 周课表
 	 */
	private LessonTableView lessonTableView;
	
	private LessonEditor lessonEdit;
	
	private FloatingActionButton floatingActionButton;
	
	private LessonTableViewModel lessonTableViewModel;
	
	
	
	public TermLessonFragment(){
		super();
	}
	
	public TermLessonFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		lessonEdit = new LessonEditor(activity, findViewById(R.id.fragment_term_lesson_base), this::updateLesson);
		
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		
		weekText = findViewById(R.id.layout_timetable_week);
		lessonTableView = findViewById(R.id.fragment_timetable_pager);
		floatingActionButton = findViewById(R.id.fragment_term_lesson_current);
		
		floatingActionButton.setVisibility(View.GONE);
		floatingActionButton.setOnClickListener(v -> lessonTableView.setCurrentItem(lessonTableViewModel.getCurrentWeek() - 1));
		
		weekText.setOnSwipeListener(left -> {
			if(left){
				if(lessonTableView.getCurrentItem() < lessonTableViewModel.getTotalWeek()){
					lessonTableView.setCurrentItem(lessonTableView.getCurrentItem() + 1);
				}
			}else if(lessonTableView.getCurrentItem() > 0){
				lessonTableView.setCurrentItem(lessonTableView.getCurrentItem() - 1);
			}
		});
		
		findViewById(R.id.fragment_term_lesson_left).setOnClickListener(v -> {
			if(lessonTableView.getCurrentItem() > 0){
				lessonTableView.setCurrentItem(lessonTableView.getCurrentItem() - 1);
			}
		});
		findViewById(R.id.fragment_term_lesson_right).setOnClickListener(v -> {
			if(lessonTableView.getCurrentItem() < lessonTableViewModel.getTotalWeek()){
				lessonTableView.setCurrentItem(lessonTableView.getCurrentItem() + 1);
			}
		});
		
		lessonTableView.setUpdateListener(this::updateLesson);
		lessonTableView.setLessonClickListener((week, count, lesson) -> lessonEdit.showLessonInfoDialog(week, count, lesson));
		lessonTableView.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){ }
			@Override
			public void onPageSelected(int position){
				if(lessonTableViewModel.getCurrentWeek() == position + 1){
					if(floatingActionButton.getVisibility() == View.VISIBLE) floatingActionButton.hide();
				}else{
					if(floatingActionButton.getVisibility() == View.GONE) floatingActionButton.show();
				}
				weekText.setText(getString(R.string.text_week, String.valueOf(position + 1)));
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});
		
		lessonTableViewModel.getLessonTableLiveData().observe(this, lessonTable -> lessonTableView.initAdapter(lessonTable));
		
		lessonTableView.postDelayed(() -> {
			int week = lessonTableViewModel.getCurrentWeek();
			lessonTableView.setCurrentItem(week - 1, false);
			weekText.setText(getString(R.string.text_week, String.valueOf(week)));
		}, 100);
	}
	
	/**
	 * 更新并保存总课表
 	 */
	public void updateLesson(){
		lessonTableViewModel.saveLessonData(lessonTableViewModel.getLessonTable());
		int currentWeek = lessonTableView.getCurrentItem();
		weekText.setText(getString(R.string.text_week, String.valueOf(currentWeek + 1)));
		lessonTableView.initAdapter(lessonTableViewModel.getLessonTable());
		lessonTableView.setCurrentItem(currentWeek);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(lessonTableView != null) lessonTableView.clearMenu();
	}
	
	@Override
	public boolean onBackPressed(){
		lessonTableView.clearMenu();
		if(lessonEdit.isShowing()){
			lessonEdit.hide();
			return false;
		}else{
			return super.onBackPressed();
		}
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_term_lesson;
	}
	
	@Override
	public String getName(){
		return "学期课表";
	}
}
