package com.qust.assistant.ui.fragment;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.widget.ColorPicker;
import com.qust.assistant.widget.DialogRoundTop;
import com.qust.assistant.widget.LessonTable;
import com.qust.assistant.widget.LessonTime;

public class TermLessonFragment extends BaseFragment{
	// 周数显示
	private TextView weekText;
	
	// 周课表
	private LessonTable lessonTable;
	
	private InputMethodManager inputManager;
	
	private boolean isInitLessonInfo, isLessonInfoShowing;
	
	// 课程编辑相关
	private TextView lessonLen;
	
	private LessonTime lessonTime;
	
	private ColorPicker lessonColor;
	
	private ViewGroup lessonInfoBack;
	
	private DialogRoundTop lessonInfo;
	
	private EditText lessonName, lessonPlace, lessonTeacher;
	
	private Animation animIn, animOut;
	
	private int week, count;
	
	private Lesson editLesson;
	
	public TermLessonFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		// 显示第几周的TextView
		weekText = findViewById(R.id.layout_timetable_week);
		
		lessonTable = findViewById(R.id.fragment_timetable_pager);
		lessonTable.initAdapter();
		
		lessonTable.setLessonClickListener((week, count, lesson) -> {
			if(!isInitLessonInfo){
				initLessonInfoDialog();
				isInitLessonInfo = true;
			}
			showLessonInfoDialog(week, count, lesson);
		});
		
		lessonTable.setUpdateListener(this::updateLesson);
		lessonTable.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){ }
			@Override
			public void onPageSelected(int position){
				weekText.setText("第 " + (position + 1) + " 周");
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});
		
		lessonTable.setCurrentItem(LessonTableViewModel.getCurrentWeek() - 1);
		
		weekText.setText("第 " + (lessonTable.getCurrentItem() + 1) + " 周");
		
		findViewById(R.id.fragment_term_lesson_current).setOnClickListener(v -> lessonTable.setCurrentItem(LessonTableViewModel.getCurrentWeek() - 1));
		
	}
	
	/**
	 * 初始化课程编辑界面
	 */
	private void initLessonInfoDialog(){
		lessonInfoBack = findViewById(R.id.layout_lesson_info_back);
		lessonInfo = findViewById(R.id.layout_lesson_info);
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		lessonInfo.getLayoutParams().height = displayMetrics.heightPixels / 4 * 3;
		
		lessonInfo.findViewById(R.id.layout_lesson_back).setOnClickListener(v -> {
			inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
			lessonInfo.startAnimation(animOut);
		});
		
		lessonInfo.findViewById(R.id.layout_lesson_done).setOnClickListener(v -> {
			
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
			
			int len = Integer.parseInt(lessonLen.getText().toString());
			if(LessonTableViewModel.isConflict(LessonTableViewModel.getLessonGroups(), week, count, editLesson, len, lessonTime.getBooleans())){
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
			
			editLesson.len = len;
			
			editLesson.week = booleans;
			
			editLesson.color = lessonColor.getChoose();
			
			lessonInfo.startAnimation(animOut);
			
			updateLesson();
			
			inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
		});
		
		lessonName = lessonInfo.findViewById(R.id.layout_lesson_name);
		lessonPlace = lessonInfo.findViewById(R.id.layout_lesson_place);
		lessonTeacher = lessonInfo.findViewById(R.id.layout_lesson_teacher);
		
		lessonLen = lessonInfo.findViewById(R.id.layout_lesson_len);
		
		lessonInfo.findViewById(R.id.layout_lesson_len_add).setOnClickListener(v -> {
			int n = Integer.parseInt(lessonLen.getText().toString()) + 1;
			if(n <= LessonTableViewModel.getLessonGroups()[0].length){
				lessonLen.setText(String.valueOf(n));
			}
		});
		
		lessonInfo.findViewById(R.id.layout_lesson_len_remove).setOnClickListener(v -> {
			int n = Integer.parseInt(lessonLen.getText().toString()) - 1;
			if(n > 0){
				lessonLen.setText(String.valueOf(n));
			}
		});
		
		lessonTime = lessonInfo.findViewById(R.id.layout_lesson_time);
		
		RadioGroup lessonType = lessonInfo.findViewById(R.id.layout_lesson_type);
		lessonType.setOnCheckedChangeListener((group, checkedId) -> {
			if(checkedId == R.id.layout_lesson_all){
				lessonTime.setFill();
			}else if(checkedId == R.id.layout_lesson_single){
				lessonTime.setSingle();
			}else if(checkedId == R.id.layout_lesson_double){
				lessonTime.setDouble();
			}
		});
		
		lessonColor = lessonInfo.findViewById(R.id.layout_lesson_color);
		
		animIn = AnimationUtils.loadAnimation(activity, R.anim.anim_bottom_in);
		animOut = AnimationUtils.loadAnimation(activity, R.anim.anim_bottom_out);
		animOut.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){ }
			@Override
			public void onAnimationEnd(Animation animation){
				isLessonInfoShowing = false;
				lessonInfoBack.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation){ }
		});
	}
	
	// 显示课程编辑框
	private void showLessonInfoDialog(int _week, int _count, Lesson lesson){
		
		isLessonInfoShowing = true;
		
		week = _week - 1;
		count = _count - 1;
		
		LessonGroup l = LessonTableViewModel.getLessonGroups()[week][count];
		
		if(l == null){
			l = new LessonGroup(_week, _count);
			LessonTableViewModel.getLessonGroups()[week][count] = l;
		}
		
		if(lesson == null){
			editLesson = new Lesson();
			editLesson.week = new boolean[LessonTableViewModel.getTotalWeek()];
			l.addLesson(editLesson);
		}else{
			editLesson = lesson;
		}
		
		lessonName.setText(editLesson.name);
		lessonPlace.setText(editLesson.place);
		lessonTeacher.setText(editLesson.teacher);
		
		lessonLen.setText(String.valueOf(editLesson.len));
		
		lessonTime.setBooleans(editLesson.week.clone());
		
		lessonColor.setChoose(editLesson.color);
		
		lessonInfoBack.setVisibility(View.VISIBLE);
		lessonInfo.startAnimation(animIn);
	}
	
	/**
	 * TODO: 完善更新操作
	 * 更新并保存总课表
 	 */
	public void updateLesson(){
		LessonTableViewModel.saveLessonData(activity, LessonTableViewModel.getLessonGroups());
		int currentWeek = lessonTable.getCurrentItem();
		weekText.setText("第 " + (currentWeek + 1) + " 周");
		lessonTable.initAdapter();
		lessonTable.setCurrentItem(currentWeek);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		lessonTable.clearMenu();
	}
	
	@Override
	public boolean onBackPressed(){
		lessonTable.clearMenu();
		if(isLessonInfoShowing){
			lessonInfo.startAnimation(animOut);
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
	protected String getName(){
		return "学期课表";
	}
}
