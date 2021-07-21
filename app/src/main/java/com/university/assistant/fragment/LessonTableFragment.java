package com.university.assistant.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.App;
import com.university.assistant.Lesson.Lesson;
import com.university.assistant.Lesson.LessonData;
import com.university.assistant.Lesson.LessonGroup;
import com.university.assistant.MainActivity;
import com.university.assistant.R;
import com.university.assistant.ui.school.GetLessonTableActivity;
import com.university.assistant.util.DateUtil;
import com.university.assistant.widget.ColorPicker;
import com.university.assistant.widget.DialogRoundTop;
import com.university.assistant.widget.LessonTable;
import com.university.assistant.widget.LessonTime;
import com.university.assistant.widget.VerticalSlidingLayout;

import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
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
	
	private RadioGroup lessonType;
	
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
		if(activity==null) activity = (MainActivity)getContext();
		LessonData.init(getContext());
		layout = (RelativeLayout)inflater.inflate(R.layout.fragment_lessontable,container,false);
		inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		initDayLesson(inflater);
		layout.findViewById(R.id.fragment_timetable_navigation).setOnClickListener(v -> navigation());
		isCreated = true;
		onCreateMenu(activity.getMenu());
		return layout;
	}
	
	@Override
	public void onReceive(String action){
		if(App.APP_UPDATE_LESSON_TABLE.equals(action)){
			initDayLesson(LayoutInflater.from(getContext()));
			lessonTable.getAdapter().notifyDataSetChanged();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(isInitDayLesson) initDayLesson(LayoutInflater.from(getContext()));
		if(isCreated&&!isInitLessonTable) initLessonTale();
	}
	
	@Override
	public void onCreateMenu(ImageView view){
		if(!isCreated)return;
		view.setVisibility(View.VISIBLE);
		view.setImageResource(R.drawable.ic_more);
		view.setOnClickListener(v -> {
			PopupMenu popup = new PopupMenu(activity,v);
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.fragment_lessontable,popup.getMenu());
			popup.setOnMenuItemClickListener(item -> {
                        switch(item.getItemId()){
                            case R.id.menu_timetable_set_start_day:
                                final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
                                final DatePicker picker = new DatePicker(getContext());
                                picker.init(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH),null);
                                new MaterialDialog.Builder(activity).customView(picker,false)
                                        .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
                                        .positiveText("确定").onPositive((dialog,which) -> {
                                            c.set(picker.getYear(),picker.getMonth(),picker.getDayOfMonth());
                                            LessonData.getInstance().setStartDay(DateUtil.YMD.format(c.getTime()));
                                            toast("设置完成!");
                                            dialog.dismiss();
                                }).show();
                                break;
                            case R.id.menu_timetable_get_lesson_table:
                            	activity.startActivity(new Intent(getContext(),GetLessonTableActivity.class));
                                break;
                            case R.id.menu_timetable_set_total_week:
                            	new MaterialDialog.Builder(activity).title("学期总周数")
			                            .input("输入周数","",(dialog,input) -> {})
			                            .inputType(InputType.TYPE_CLASS_NUMBER)
			                            .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
			                            .positiveText("确定").onPositive((dialog,which) -> {
				                            LessonData.getInstance().setTotalWeek(week);
		                                    lessonTable.initAdapter(null);
			                            	
	                            }).show();
                            	break;
	                        case R.id.menu_timetable_save:
	                        	LessonData.getInstance().saveLessonData();
	                        	break;
                        }
                        return false;
                    }
            );
			popup.show();
		});
		
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
		
		boolean needEmpty = false;
		
		for(int i=0;i<time.length;){
//			h -= LessonTableData.summer[i][0];
//			m -= LessonTableData.summer[i][1];
//			if(m<0){
//				h -= 1;
//				m += 60;
//			}
			if(time[i]!=null){
				needEmpty = true;
				TextView t = (TextView)inflater.inflate(R.layout.view_text,null);
				t.setText(time[i]);
				content.addView(t);
			}
			Lesson lesson;
			if(lessonGroups[i] == null || (lesson = lessonGroups[i].getCurrentLesson(currentWeek)) == null){
				if(needEmpty){
					needEmpty = false;
					content.addView(LessonGroup.getView(getContext(),null, i, 1, h, m));
				}
				i++;
			}else{
				content.addView(LessonGroup.getView(getContext(),lesson, i , lesson.len, h, m));
				i += lesson.len;
			}
		}
		isInitDayLesson = true;
	}
	
	// 初始化总课表
	private void initLessonTale(){
		// 每周课表界面关闭回调
		weekLayout = layout.findViewById(R.id.fragment_timetable_week);
		weekLayout.setBackListener(b -> isLessonTableShowing = !b);
		
		weekText = layout.findViewById(R.id.layout_timetable_week);
		
		lessonTable = layout.findViewById(R.id.fragment_timetable_pager);
		lessonTable.initAdapter(null);
		lessonTable.setViewPager(activity.getViewPager());
		lessonTable.setLessonClickListener((week, count, lesson) -> {
			if(!isInitLessonInfo){
				initLessonInfoLayout();
				isInitLessonInfo = true;
			}
			lessonInfoDialog(week, count, lesson);
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
		
		isInitLessonTable = true;
	}
	
	// 初始化课程编辑界面
	private void initLessonInfoLayout(){
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
			
			int len = Integer.parseInt(lessonLen.getText().toString());
			
			if(LessonData.getInstance().isConflict(week, count, editLesson, len, lessonTime.getBooleans())){
				toast("课程时间冲突！");
				return;
			}
			
			editLesson.name = lessonName.getText().toString();
			editLesson.place = lessonPlace.getText().toString();
			editLesson.teacher = lessonTeacher.getText().toString();
			
			editLesson.len = len;
			
			editLesson.week = lessonTime.getBooleans();
			
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
		
		lessonType = lessonInfo.findViewById(R.id.layout_lesson_type);
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
	private void lessonInfoDialog(int _week,int _count,Lesson lesson){
		
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
		
		lessonTime.setBooleans(editLesson.week);
		
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
	
	// TODO:暂时屏蔽导航功能
	private void navigation(){
//
//		Lesson[] lessons = LessonTableData.getInstance().getLessons()[LessonTableData.getInstance().getWeek()];
//
//		Calendar c = Calendar.getInstance();
//		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
//		int m = c.get(Calendar.MINUTE);
//
//		int currentWeek = LessonTableData.getInstance().getCurrentWeek();
//
//		int nextLesson = 0;
//
//		for(int i=0;i<5;i++){
//			h -= LessonTableData.summer[i][0];
//			m -= LessonTableData.summer[i][1];
//			if(m<0){
//				h -= 1;
//				m += 60;
//			}
//			if(h<0){
//				nextLesson = i;
//				break;
//			}
//		}
//
//		String s = "";
//
//		Lesson next = Lesson.getLesson(lessons[nextLesson],currentWeek,nextLesson+1);
//
//		if(nextLesson==0){
//			if(next.place.startsWith("明")){
//				Intent i = new Intent(activity,NavigationActivity.class);
//				i.putExtra("start","学院楼");
//				i.putExtra("destination",next.place);
//				startActivity(i);
//			}else if("".equals(next.place)){
//				toast("当前没有课");
//			}else{
//				toast("暂不支持导航");
//			}
//		}else{
//			Lesson current = Lesson.getLesson(lessons[nextLesson-1],currentWeek,nextLesson);
//			if(next.place.startsWith("明")){
//				Intent i = new Intent(activity,NavigationActivity.class);
//				i.putExtra("start",current.place);
//				i.putExtra("destination",next.place);
//				startActivity(i);
//			}else if("".equals(next.place)){
//				toast("当前没有课");
//			}else{
//				toast("暂不支持导航");
//			}
//		}
	}
	
	@Override
	public String getTitle(){
		return "课表";
	}
}
