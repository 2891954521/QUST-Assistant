package com.qust.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.widget.ColorPicker;
import com.qust.assistant.widget.lesson.LessonTime;
import com.qust.base.ui.BaseActivity;
import com.qust.lesson.Lesson;
import com.qust.lesson.LessonGroup;
import com.qust.lesson.LessonTableModel;
import com.qust.lesson.LessonTableViewModel;
import com.qust.utils.CodeUtils;

/**
 * 课程编辑器
 */
public class LessonEditor{
	
	private BaseActivity activity;
	
	private TextView lessonLen;
	
	private LessonTime lessonTime;
	
	private ColorPicker lessonColor;
	
	private EditText lessonName, lessonPlace, lessonTeacher;
	
	private ViewGroup rootView;
	
	private int week, count;
	
	private Lesson editLesson;
	
	private BottomDialog lessonEdit;
	
	private InputMethodManager inputManager;
	
	private LessonTableViewModel lessonTableViewModel;
	
	private CodeUtils.Callback callback;
	
	public LessonEditor(BaseActivity activity, ViewGroup rootView, CodeUtils.Callback callback){
		this.activity = activity;
		this.rootView = rootView;
		this.callback = callback;
		
		lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		
		inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	
	/**
	 * 初始化课程编辑界面
	 */
	private void initLessonInfoDialog(){
		View back = LayoutInflater.from(activity).inflate(R.layout.layout_lesson_edit, null);
		
		rootView.addView(back);
		
		lessonEdit = new BottomDialog(activity, back, back.findViewById(R.id.layout_lesson_info), 0.75f);
		
		lessonEdit.findViewById(R.id.layout_lesson_back).setOnClickListener(v -> {
			inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
			lessonEdit.hide();
		});
		
		lessonEdit.findViewById(R.id.layout_lesson_done).setOnClickListener(v -> {
			
			long time = lessonTime.getLong();
			if(time == 0){
				activity.toastWarning("请选择上课时间！");
				return;
			}
			
			int len = editLesson.len;
			long weeks = editLesson.week;
			
			editLesson.len = Integer.parseInt(lessonLen.getText().toString());
			editLesson.week = time;
			
			if(LessonTableModel.isConflict(lessonTableViewModel.getLessonTable(), week, count, editLesson)){
				editLesson.len = len;
				editLesson.week = weeks;
				activity.toastWarning("课程时间冲突！");
				return;
			}
			
			String name = lessonName.getText().toString();
			if("".equals(name)){
				activity.toastWarning("请输入课程名称！");
				return;
			}
			
			editLesson.name = name;
			editLesson.color = lessonColor.getChoose();
			editLesson.place = lessonPlace.getText().toString();
			editLesson.teacher = lessonTeacher.getText().toString();
			
			// 通知课表更新
			callback.callback();
			
			lessonEdit.hide();
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
	public void showLessonInfoDialog(int _week, int _count, Lesson lesson){
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
	
	public boolean isShowing(){
		return lessonEdit != null && lessonEdit.isShowing();
	}
	
	public void hide(){
		if(lessonEdit != null) lessonEdit.hide();
	}
	
	
}
