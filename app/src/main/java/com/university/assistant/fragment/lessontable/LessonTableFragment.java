package com.university.assistant.fragment.lessontable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
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
import com.university.assistant.MainActivity;
import com.university.assistant.R;
import com.university.assistant.fragment.BaseFragment;
import com.university.assistant.ui.school.GetLessonTableActivity;
import com.university.assistant.util.ColorUtil;
import com.university.assistant.util.LogUtil;
import com.university.assistant.widget.ColorPicker;
import com.university.assistant.widget.DialogRoundTop;
import com.university.assistant.widget.LessonTable;
import com.university.assistant.widget.LessonTablePager;
import com.university.assistant.widget.LessonTime;
import com.university.assistant.widget.VerticalSlidingLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class LessonTableFragment extends BaseFragment{
	
	private RelativeLayout layout;
	
	private LessonTablePager viewPager;
	
	private TextView weekText;
	
	private EditText lessonName, lessonPlace, lessonTeacher, startWeek, endWeek;
	
	private ColorPicker lessonColor;
	
	private LessonTime lessonTime;
	
	private RadioGroup lessonType;
	
	private FrameLayout lessonInfoBack;
	
	private DialogRoundTop lessonInfo;
	
	private VerticalSlidingLayout weekLayout;
	
	private InputMethodManager inputManager;
	
	private Animation animIn,animOut;
	
	private boolean isInitDayLesson, isInitLessonTable, isInitLessonInfo;
	
	private boolean isLessonTableShowing, isLessonInfoShowing;
	
	private Lesson.BaseLesson editLesson;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
		if(activity==null) activity = (MainActivity)getContext();
		LessonTableData.init(getContext());
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
			viewPager.getAdapter().notifyDataSetChanged();
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
                                            LessonTableData.getInstance().setStartDay(new SimpleDateFormat("yyyy/MM/dd").format(c.getTime()));
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
			                            	int week = Integer.parseInt(dialog.getInputEditText().getText().toString());
		                                    viewPager.setAdapter(new LessonTableAdapter(week));
		                                    LessonTableData.getInstance().setTotalWeek(week);
			                            	
	                            }).show();
                            	break;
	                        case R.id.menu_timetable_save:
	                        	LessonTableData.getInstance().saveLessonData();
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
		
		Lesson[] lessons = LessonTableData.getInstance().getLessons()[LessonTableData.getInstance().getWeek()];
		
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY) - 8;
		int m = c.get(Calendar.MINUTE);
		
		int currentWeek = LessonTableData.getInstance().getCurrentWeek();
		
		String[] time = { "上午课程", null, null, null, "下午课程", null, null, null, "晚上课程", null};
		
		for(int i=0;i<time.length;i++){
//			h -= LessonTableData.summer[i][0];
//			m -= LessonTableData.summer[i][1];
//			if(m<0){
//				h -= 1;
//				m += 60;
//			}
			if(time[i]!=null){
				TextView t = (TextView)inflater.inflate(R.layout.view_text,null);
				t.setText(time[i]);
				content.addView(t);
			}
			Lesson lesson = Lesson.getLesson(lessons[i], currentWeek);
			content.addView(lesson.getView(getContext(),h,m));
			Lesson.BaseLesson l = lesson.getCurrentLesson(currentWeek);
			if(l != null)i += l.len;
		}
		isInitDayLesson = true;
	}
	
	// 初始化总课表
	private void initLessonTale(){
		// 每周课表界面关闭回调
		weekLayout = layout.findViewById(R.id.fragment_timetable_week);
		weekLayout.setBackListener(b -> isLessonTableShowing = !b);
		
		weekText = layout.findViewById(R.id.layout_timetable_week);
		
		viewPager = layout.findViewById(R.id.fragment_timetable_pager);
		viewPager.setViewPager(activity.getViewPager());
		viewPager.setAdapter(new LessonTableAdapter(LessonTableData.getInstance().getTotalWeek()));
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){ }
			@Override
			public void onPageSelected(int position){
				weekText.setText("第 " + (position + 1) + " 周");
				if(position==0||position==viewPager.getAdapter().getCount()-1)viewPager.setIsEnd(true);
				else viewPager.setIsEnd(false);
			}
			@Override
			public void onPageScrollStateChanged(int state){ }
		});
		
		viewPager.setCurrentItem(LessonTableData.getInstance().getCurrentWeek() - 1);
		
		viewPager.setIsEnd(viewPager.getCurrentItem()==0);
		
		isInitLessonTable = true;
	}
	
	// 初始化课程编辑界面
	private void initLessonInfoLayout(){
		lessonInfoBack = layout.findViewById(R.id.layout_lesson_info_back);
		lessonInfo = layout.findViewById(R.id.layout_lesson_info);
		
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)lessonInfo.getLayoutParams();
		WindowManager mWindow = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = mWindow.getDefaultDisplay();
		layoutParams.height = display.getHeight()/4*3;
		lessonInfo.setLayoutParams(layoutParams);
		
		lessonInfo.findViewById(R.id.layout_lesson_back).setOnClickListener(v -> {
			inputManager.hideSoftInputFromWindow(((Activity)getContext()).getWindow().getDecorView().getWindowToken(), 0);
			lessonInfo.startAnimation(animOut);
		});
		
		lessonInfo.findViewById(R.id.layout_lesson_done).setOnClickListener(v -> {
			
//			editLesson.name = lessonName.getText().toString();
//			editLesson.place = lessonPlace.getText().toString();
//			editLesson.teacher = lessonTeacher.getText().toString();
//
//			String s = startWeek.getText().toString();
//			editLesson.startWeek = TextUtils.isEmpty(s) ? 1 : Integer.parseInt(s);
//			s = endWeek.getText().toString();
//			editLesson.endWeek = TextUtils.isEmpty(s) ? 1 : Integer.parseInt(s);
//
//			int id = lessonType.getCheckedRadioButtonId();
//			editLesson.type = id==R.id.layout_lesson_single ? 1 : id==R.id.layout_lesson_double?0:-1;
//			editLesson.color = lessonColor.getChoose();
//
//			lessonInfo.startAnimation(animOut);
//			LessonTableData.getInstance().saveLessonData();
//			int currentWeek = viewPager.getCurrentItem();
//			viewPager.setAdapter(viewPager.getAdapter());
//			viewPager.setCurrentItem(currentWeek);
//
//			inputManager.hideSoftInputFromWindow(((Activity)getContext()).getWindow().getDecorView().getWindowToken(), 0);
		});
		
		lessonName = lessonInfo.findViewById(R.id.layout_lesson_name);
		lessonPlace = lessonInfo.findViewById(R.id.layout_lesson_place);
		lessonTeacher = lessonInfo.findViewById(R.id.layout_lesson_teacher);
		
		lessonTime = lessonInfo.findViewById(R.id.layout_lesson_time);
		
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
	
	private void lessonInfoDialog(int week, int count,Lesson.BaseLesson lesson){
		
		isLessonInfoShowing = true;
	
		if(lesson == null){
			editLesson = new Lesson.BaseLesson();
			editLesson.week = new boolean[LessonTableData.getInstance().getTotalWeek()];
		}else{
			editLesson = lesson;
		}
		
		lessonName.setText(editLesson.name);
		lessonPlace.setText(editLesson.place);
		lessonTeacher.setText(editLesson.teacher);
		
		lessonTime.setBooleans(editLesson.week);
		
//		lessonType.check(lesson.type==1?R.id.layout_lesson_single:lesson.type==0?R.id.layout_lesson_double:R.id.layout_lesson_every);
		lessonColor.setChoose(editLesson.color==0?1:editLesson.color);

		lessonInfoBack.setVisibility(View.VISIBLE);
		lessonInfo.startAnimation(animIn);
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
	
	private class LessonTableAdapter extends PagerAdapter{
		
		private int total;
		
		private Calendar start;
		
		private SimpleDateFormat sdf;
		
		private String[] week = {"周一","周二","周三","周四","周五","周六","周日"};
		
		public LessonTableAdapter(int _total){
			total = _total;
			sdf = new SimpleDateFormat("MM/dd");
			start = Calendar.getInstance();
			try{
				Date date = new SimpleDateFormat("yyyy/MM/dd").parse(LessonTableData.getInstance().startDay);
				if(date!=null)start.setTime(date);
			}catch(ParseException e){
				LogUtil.Log(e);
			}
		}
		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container,int position){
			ViewGroup layout = (ViewGroup)LayoutInflater.from(getContext()).inflate(R.layout.layout_timetable_week,null);
			
			LinearLayout l = layout.findViewById(R.id.layout_timetable_day);
			
			Calendar current = Calendar.getInstance();
			Calendar c = (Calendar)start.clone();
			
			c.add(Calendar.WEEK_OF_YEAR,position);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 2;
			c.add(Calendar.DATE, - dayOfWeek);

			for(int i=0;i<7;i++){
				TextView t = new TextView(getContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);
				lp.weight = 1;
				t.setLayoutParams(lp);
				t.setPadding(10,20,10,20);
				t.setGravity(Gravity.CENTER);
				t.setText(week[i] + "\n" + sdf.format(c.getTime()));
				t.setTextSize(12);
				if(current.get(Calendar.DATE)==c.get(Calendar.DATE)&&current.get(Calendar.MONTH)==c.get(Calendar.MONTH))
					t.setTextColor(ColorUtil.TEXT_COLORS[1]);
				else t.setTextColor(Color.GRAY);
				l.addView(t);
				c.add(Calendar.DATE,1);
			}
			
			LessonTable lessonTable = layout.findViewById(R.id.layout_timetable_lessons);
			lessonTable.setWeek(position+1);
			lessonTable.setLessonClickListener((week, count, lesson) -> {
				if(!isInitLessonInfo){
					initLessonInfoLayout();
					isInitLessonInfo = true;
				}
				lessonInfoDialog(week, count, lesson);
			});
			container.addView(layout);
			return layout;
		}
		@Override
		public void destroyItem(@NonNull ViewGroup container,int position,@NonNull Object object){ container.removeView((View)object); }
		@Override
		public int getCount(){ return total; }
		@Override
		public boolean isViewFromObject(@NonNull View view,@NonNull Object object){ return view == object; }
		@Override
		public int getItemPosition(@NonNull Object object){
			if ((boolean)((View)object).getTag(R.id.layout_timetable_week)) return POSITION_NONE;
			else return super.getItemPosition(object);
		}
		@Override
		public void notifyDataSetChanged(){
			for(int i=0;i<viewPager.getChildCount();i++){
				View child = viewPager.getChildAt(i);
				child.setTag(R.id.layout_timetable_week,true);
			}
			super.notifyDataSetChanged();
		}
	}
	
	@Override
	public String getTitle(){
		return "课表";
	}
}
