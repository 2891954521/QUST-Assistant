package com.qust.fragment.lesson;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.RelativeGuide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qust.assistant.R;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.widget.ColorPicker;
import com.qust.assistant.widget.lesson.LessonTime;
import com.qust.assistant.widget.swipe.SwipeTextView;
import com.qust.base.fragment.BaseFragment;
import com.qust.lesson.Lesson;
import com.qust.lesson.LessonGroup;
import com.qust.lesson.LessonTableModel;
import com.qust.lesson.LessonTableViewModel;
import com.qust.lesson.view.LessonTableView;
import com.qust.widget.BottomDialog;

public class TermLessonFragment extends BaseFragment{
	
	/**
	 * 周数显示
 	 */
	private SwipeTextView weekText;
	
	/**
	 * 周课表
 	 */
	private LessonTableView lessonTableView;
	
	// 课程编辑相关
	private TextView lessonLen;
	
	private LessonTime lessonTime;
	
	private ColorPicker lessonColor;
	
	private EditText lessonName, lessonPlace, lessonTeacher;

	private int week, count;
	
	private Lesson editLesson;
	
	private BottomDialog lessonEdit;
	
	private InputMethodManager inputManager;
	
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
		inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		
		// 显示第几周的TextView
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
			}else{
				if(lessonTableView.getCurrentItem() > 0){
					lessonTableView.setCurrentItem(lessonTableView.getCurrentItem() - 1);
				}
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
		lessonTableView.setLessonClickListener(this::showLessonInfoDialog);
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
				weekText.setText("第 " + (position + 1) + " 周");
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});
		
		lessonTableViewModel.getLessonTableLiveData().observe(this, lessonTable -> lessonTableView.initAdapter(lessonTable));
		
		lessonTableView.postDelayed(() -> {
			int week = lessonTableViewModel.getCurrentWeek();
			lessonTableView.setCurrentItem(week - 1, false);
			weekText.setText("第 " + week + " 周");
		}, 100);
	}
	
	/**
	 * 初始化课程编辑界面
	 */
	private void initLessonInfoDialog(){
		View back = LayoutInflater.from(activity).inflate(R.layout.layout_lesson_edit, findViewById(R.id.fragment_term_lesson_base));
		
		lessonEdit = new BottomDialog(activity, back, back.findViewById(R.id.layout_lesson_info), 0.75f);
		
		lessonEdit.findViewById(R.id.layout_lesson_back).setOnClickListener(v -> {
			inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
			lessonEdit.hide();
		});
		
		lessonEdit.findViewById(R.id.layout_lesson_done).setOnClickListener(v -> {
			
			boolean[] booleans = lessonTime.getBooleans();
			boolean hasLesson = false;
			for(boolean b : booleans){
				if(b){
					hasLesson = true;
					break;
				}
			}
			if(!hasLesson){
				toast("请选择上课时间！");
				return;
			}
			
			int len = editLesson.len;
			long weeks = editLesson.week;
			
			editLesson.len = Integer.parseInt(lessonLen.getText().toString());
			editLesson.week = lessonTime.getLong();
			
			if(LessonTableModel.isConflict(lessonTableViewModel.getLessonTable(), week, count, editLesson)){
				editLesson.len = len;
				editLesson.week = weeks;
				toast("课程时间冲突！");
				return;
			}
			
			String name = lessonName.getText().toString();
			if("".equals(name)){
				toast("请输入课程名称！");
				return;
			}
			editLesson.name = name;
			
			editLesson.place = lessonPlace.getText().toString();
			editLesson.teacher = lessonTeacher.getText().toString();
			
			editLesson.color = lessonColor.getChoose();
			
			lessonEdit.hide();
			
			updateLesson();
			
			inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
		});
		
		lessonName = lessonEdit.findViewById(R.id.layout_lesson_name);
		lessonPlace = lessonEdit.findViewById(R.id.layout_lesson_place);
		lessonTeacher = lessonEdit.findViewById(R.id.layout_lesson_teacher);
		
		lessonLen = lessonEdit.findViewById(R.id.layout_lesson_len);
		
		lessonEdit.findViewById(R.id.layout_lesson_len_add).setOnClickListener(v -> {
			int n = Integer.parseInt(lessonLen.getText().toString()) + 1;
			if(n <= lessonTableViewModel.getLessonGroups()[0].length){
				lessonLen.setText(String.valueOf(n));
			}
		});
		
		lessonEdit.findViewById(R.id.layout_lesson_len_remove).setOnClickListener(v -> {
			int n = Integer.parseInt(lessonLen.getText().toString()) - 1;
			if(n > 0){
				lessonLen.setText(String.valueOf(n));
			}
		});
		
		lessonTime = lessonEdit.findViewById(R.id.layout_lesson_time);
		
		RadioGroup lessonType = lessonEdit.findViewById(R.id.layout_lesson_type);
		lessonType.setOnCheckedChangeListener((group, checkedId) -> {
			if(checkedId == R.id.layout_lesson_all){
				lessonTime.setFill();
			}else if(checkedId == R.id.layout_lesson_single){
				lessonTime.setSingle();
			}else if(checkedId == R.id.layout_lesson_double){
				lessonTime.setDouble();
			}
		});
		
		lessonColor = lessonEdit.findViewById(R.id.layout_lesson_color);
	}
	
	/**
	 * 显示课程编辑框
 	 */
	private void showLessonInfoDialog(int _week, int _count, Lesson lesson){
		if(lessonEdit == null){
			initLessonInfoDialog();
		}
		
		week = _week - 1;
		count = _count - 1;
		
		LessonGroup l = lessonTableViewModel.getLessonGroups()[week][count];
		
		if(l == null) l = new LessonGroup(_week, _count);
		
		if(lesson == null){
			editLesson = new Lesson();
			l.addLesson(editLesson);
		}else{
			editLesson = lesson;
		}
		
		lessonName.setText(editLesson.name);
		lessonPlace.setText(editLesson.place);
		lessonTeacher.setText(editLesson.teacher);
		
		lessonLen.setText(String.valueOf(editLesson.len));
		
		lessonTime.setLong(editLesson.week);
		
		lessonColor.setChoose(editLesson.color);
		
		lessonEdit.show();
	}
	
	/**
	 * 更新并保存总课表
 	 */
	public void updateLesson(){
		lessonTableViewModel.saveLessonData(lessonTableViewModel.getLessonTable());
		int currentWeek = lessonTableView.getCurrentItem();
		weekText.setText("第 " + (currentWeek + 1) + " 周");
		lessonTableView.initAdapter(lessonTableViewModel.getLessonTable());
		lessonTableView.setCurrentItem(currentWeek);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(lessonTableView != null){
			int time = SettingUtil.getInt(activity, NewbieGuide.TAG, getClass().getName(), 0);
			if(time == 0){
				lessonTableView.post(() -> NewbieGuide.with(activity).setLabel(getClass().getName())
					.setOnPageChangedListener(page -> {
						if(page == 0) floatingActionButton.show();
					})
					.addGuidePage(GuidePage.newInstance()
							.addHighLight(weekText, new RelativeGuide(R.layout.layout_welcome_term_lesson, Gravity.BOTTOM, 50))
					).addGuidePage(GuidePage.newInstance()
							.addHighLight(floatingActionButton, HighLight.Shape.CIRCLE, 20)
							.setLayoutRes(R.layout.layout_welcome_term_lesson2)
					).addGuidePage(GuidePage.newInstance()
							.setLayoutRes(R.layout.layout_welcome_term_lesson3)
					).show());
			}
		}
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
