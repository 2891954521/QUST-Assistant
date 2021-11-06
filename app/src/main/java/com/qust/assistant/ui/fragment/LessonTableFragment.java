package com.qust.assistant.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.lesson.Lesson;
import com.qust.assistant.lesson.LessonData;
import com.qust.assistant.lesson.LessonGroup;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.NavigationActivity;
import com.qust.assistant.util.ColorUtil;
import com.qust.assistant.widget.ColorPicker;
import com.qust.assistant.widget.DialogRoundTop;
import com.qust.assistant.widget.LessonTable;
import com.qust.assistant.widget.LessonTime;
import com.qust.assistant.widget.VerticalSlidingLayout;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class LessonTableFragment extends BaseFragment{
	
	// 周数显示
	private TextView weekText;
	
	private RelativeLayout layout;
	
	private LessonTable lessonTable;
	
	private VerticalSlidingLayout weekLayout;
	
	private InputMethodManager inputManager;
	
	private boolean isLessonTableShowing, isLessonInfoShowing;
	
	private boolean isInitDayLesson, isInitLessonTable, isInitLessonInfo;
	
	// 课程编辑相关
	private TextView lessonLen;
	
	private LessonTime lessonTime;
	
	private ColorPicker lessonColor;
	
	private FrameLayout lessonInfoBack;
	
	private DialogRoundTop lessonInfo;
	
	private EditText lessonName, lessonPlace, lessonTeacher;
	
	private Animation animIn,animOut;
	
	private int week, count;
	
	private Lesson editLesson;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
		if(activity == null) activity = (MainActivity)getContext();
		
		LessonData.init(getContext());
		
		inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		layout = (RelativeLayout)inflater.inflate(R.layout.fragment_lessontable,container,false);
		
		layout.findViewById(R.id.fragment_timetable_current).setOnClickListener(v -> lessonTable.setCurrentItem(LessonData.getInstance().getCurrentWeek() - 1));
		
		initDayLesson(inflater);
		
		isCreated = true;
		
		onCreateMenu(activity.getMenu());
		return layout;
	}
	
	@Override
	public void onReceive(String action){
		if(App.APP_UPDATE_LESSON_TABLE.equals(action)){
			initDayLesson(LayoutInflater.from(getContext()));
			lessonTable.initAdapter(null);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(isCreated){
			// TODO:resume时更新课表时间
			// if(isInitDayLesson) initDayLesson(LayoutInflater.from(getContext()));
			if(!isInitLessonTable) initLessonTale();
		}
	}
	
	@Override
	public void onCreateMenu(ImageView view){
		if(!isCreated)return;
		view.setVisibility(View.GONE);
		if(!isInitLessonTable){
			initLessonTale();
			isInitLessonTable = true;
		}
	}
	
	@Override
	public boolean onBackPressed(){
		if(isLessonInfoShowing){
			lessonInfo.startAnimation(animOut);
			return false;
		}else if(isLessonTableShowing){
			weekLayout.setBack(true);
			return false;
		}else return super.onBackPressed();
	}
	
	// 加载当日课表
	private void initDayLesson(LayoutInflater inflater){
		
		LinearLayout content = layout.findViewById(R.id.fragment_timetable_today);
		content.removeAllViews();
		
		LessonGroup[] lessonGroups = LessonData.getInstance().getLessonGroups()[LessonData.getInstance().getWeek()];
		
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		int currentWeek = LessonData.getInstance().getCurrentWeek();
		
		String[] time = { "上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
		
		int pass = 0;
		
		boolean needEmpty = false;
		
		View view = null;
		
		Lesson lesson = null;
		
		for(int i=0;i<time.length;i++){
			if(pass == 0){
				if(time[i] != null){
					needEmpty = true;
					TextView t = (TextView)inflater.inflate(R.layout.view_text, null);
					t.setText(time[i]);
					content.addView(t);
				}
				
				if(lessonGroups[i] == null || (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) == null){
					if(needEmpty){
						needEmpty = false;
						content.addView(LessonGroup.getView(getContext(),null, i, 1));
					}
				}else{
					view = LessonGroup.getView(getContext(), lesson, i, lesson.len);
					pass = lesson.len;
				}
			}
			
			h -= LessonData.Lesson_Time[i][0];
			m -= LessonData.Lesson_Time[i][1];
			if(m < 0){ h -= 1; m += 60; }
			
			if(pass > 0){
				pass--;
				if(view == null) continue;
				if(h > 0){
					if(pass == 0){
						((TextView)view.findViewById(R.id.item_lesson_status)).setText("已结束");
						content.addView(view);
					}
				}else if(h == 0){
					if(m > 50){
						if(pass == 0){
							((TextView)view.findViewById(R.id.item_lesson_status)).setText("已结束");
							content.addView(view);
						}
					}else{
						TextView t = view.findViewById(R.id.item_lesson_status);
						t.setText((50 - m) + "min后下课");
						t.setTextColor(ColorUtil.TEXT_COLORS[0]);
						TextView n = view.findViewById(R.id.item_lesson_name);
						n.getPaint().setFakeBoldText(true);
						content.addView(view);
						view = null;
					}
				}else{
					((TextView)view.findViewById(R.id.item_lesson_status)).setText("未开始");
					content.addView(view);
					view = null;
				}
			}
		}
		isInitDayLesson = true;
	}
	
	// 初始化总课表
	private void initLessonTale(){
		// 显示第几周的TextView
		weekText = layout.findViewById(R.id.layout_timetable_week);
		
		lessonTable = layout.findViewById(R.id.fragment_timetable_pager);
		lessonTable.initAdapter(null);
		lessonTable.setViewPager(activity.getViewPager());
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
		
		lessonTable.setCurrentItem(LessonData.getInstance().getCurrentWeek() - 1);
		
		// 总课表关闭回调
		weekLayout = layout.findViewById(R.id.fragment_timetable_week);
		weekLayout.setBackListener(b -> {
			isLessonTableShowing = !b;
			if(b) lessonTable.clearMenu();
		});
		
		isInitLessonTable = true;
	}
	
	// 初始化课程编辑界面
	private void initLessonInfoDialog(){
		lessonInfoBack = layout.findViewById(R.id.layout_lesson_info_back);
		lessonInfo = layout.findViewById(R.id.layout_lesson_info);
		
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)lessonInfo.getLayoutParams();
		WindowManager mWindow = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
		Display display = mWindow.getDefaultDisplay();
		layoutParams.height = display.getHeight()/4*3;
		lessonInfo.setLayoutParams(layoutParams);
		
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
			if(LessonData.getInstance().isConflict(week, count, editLesson, len, lessonTime.getBooleans())){
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
			if(n <= LessonData.getInstance().getLessonGroups()[0].length){
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
		lessonType.setOnCheckedChangeListener((group,checkedId) -> {
			switch(checkedId){
				case R.id.layout_lesson_all:
					lessonTime.setFill();
					break;
				case R.id.layout_lesson_single:
					lessonTime.setSingle();
					break;
				case R.id.layout_lesson_double:
					lessonTime.setDouble();
					break;
			}
		});
		
		lessonColor = lessonInfo.findViewById(R.id.layout_lesson_color);
		
		animIn = AnimationUtils.loadAnimation(getContext(),R.anim.anim_bottom_in);
		animOut = AnimationUtils.loadAnimation(getContext(),R.anim.anim_bottom_out);
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
	private void showLessonInfoDialog(int _week,int _count,Lesson lesson){
		
		isLessonInfoShowing = true;
		
		week = _week - 1;
		count = _count - 1;
		
		LessonGroup l = LessonData.getInstance().getLessonGroups()[week][count];
		
		if(l == null){
			l = new LessonGroup(_week, _count);
			LessonData.getInstance().getLessonGroups()[week][count] = l;
		}
		
		if(lesson == null){
			editLesson = new Lesson();
			editLesson.week = new boolean[LessonData.getInstance().getTotalWeek()];
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
	
	// 更新总课表
	private void updateLesson(){
		LessonData.getInstance().saveLessonData();
		int currentWeek = lessonTable.getCurrentItem();
		lessonTable.setAdapter(lessonTable.getAdapter());
		lessonTable.setCurrentItem(currentWeek);
	}
	
	private void navigation(){

		LessonGroup[] lessonGroups = LessonData.getInstance().getLessonGroups()[LessonData.getInstance().getWeek()];
		
		int currentWeek = LessonData.getInstance().getCurrentWeek();
		
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		int pass = 0;
		
		Lesson lesson = null;
		
		Lesson currentLesson = null;
		
		int current = 0;
		
		for(int i=0;i<lessonGroups.length;i++){
			if(pass == 0){
				if(lessonGroups[i] != null && (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) != null){
					current = i;
					currentLesson = lesson;
					pass = lesson.len;
				}
			}
			
			h -= LessonData.Lesson_Time[i][0];
			m -= LessonData.Lesson_Time[i][1];
			if(m < 0){ h -= 1; m += 60; }
			
			if(pass > 0){
				pass--;
				if(h == 0){
					break;
				}
			}
		}
		
		Lesson nextLesson = null;
		
		if(lessonGroups[current + 1] == null || (nextLesson = lessonGroups[current + 1].getCurrentLesson(currentWeek)) == null){
			startActivity(new Intent(activity,NavigationActivity.class));
		}else if(nextLesson.place.startsWith("明")){
			startActivity(new Intent(activity,NavigationActivity.class)
					.putExtra("start",currentLesson == null ? "学院楼" : currentLesson.place)
					.putExtra("destination",nextLesson.place)
			);
		}else{
			toast("暂不支持导航");
			startActivity(new Intent(activity,NavigationActivity.class));
		}
	}
	
	@Override
	public String getTitle(){
		return "课表";
	}
}
