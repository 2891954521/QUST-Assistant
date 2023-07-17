package com.qust.assistant.util.QustUtil;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.R;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.util.NotificationUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.vo.Exam;
import com.qust.assistant.vo.Mark;
import com.qust.assistant.vo.Notice;
import com.qust.assistant.vo.QueryLessonResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 自动查询工具类
 */
public class AutoQueryUtil{

	/**
	 * 开始自动查询
	 */
	public static void startAutoQuery(@NonNull Activity activity){
		
		long current = System.currentTimeMillis();
		long frequency = 1000 * 60 * 60 * 24;
		
		if(current - (long)SettingUtil.get(activity.getString(R.string.last_auto_check_time), 0L) < frequency){
			return;
		}
		
		new Thread(){
			@Override
			public void run(){
				int entranceTime = SettingUtil.getInt(activity.getString(R.string.KEY_ENTRANCE_TIME), 0);
				if(entranceTime == 0) return;
				
				int currentYear = LessonUtil.getCurrentYear(entranceTime);
				
				String xnm = Integer.toString(currentYear / 2 + entranceTime);
				String xqm = currentYear % 2 == 0 ? "3" : "12";
				
				LoginViewModel loginViewModel = LoginViewModel.getInstance(activity);
				if(loginViewModel.login() == null){
					return;
				}
				
				String noticeContent = queryNotice(activity, loginViewModel);
				if(noticeContent != null){
					NotificationUtil.sendNotification(activity, "教务通知", noticeContent);
				}
				
				String mark = queryMark(activity, loginViewModel, currentYear, xnm, xqm);
				if(mark != null){
					NotificationUtil.sendNotification(activity, "成绩查询", mark);
				}
				
				String exam = queryExam(activity, loginViewModel, currentYear, xnm, xqm);
				if(exam != null){
					NotificationUtil.sendNotification(activity, "考试查询", mark);
				}
				
				SettingUtil.edit().putLong(activity.getString(R.string.last_auto_check_time), current).apply();
			}
		}.start();

	}
	
	/**
	 * 查询最新的教务通知
	 * @param activity
	 * @param loginViewModel
	 * @return
	 */
	@Nullable
	public static String queryNotice(Activity activity, LoginViewModel loginViewModel){
		Notice[] notices = QUSTQueryUtil.queryNotice(loginViewModel);
		if(notices.length == 0) return null;
		
		Notice notice = notices[0];
		String id = SettingUtil.getString(activity.getString(R.string.last_notice_id), "");
		if(id.equals(notice.id)) return null;
		
		SettingUtil.edit().putString(activity.getString(R.string.last_notice_id), notice.id).apply();
		
		return notice.xxnr + "\n\n" + notice.cjsj;
	}
	
	
	@Nullable
	public static String queryLessonTable(Activity activity, LoginViewModel loginViewModel, int entranceTime, String xnm, String xqm){
		
		QueryLessonResult result = LessonUtil.queryLessonTable(loginViewModel, xnm, xqm);
		
		try{
			return null;
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 查询最新的成绩
	 * @param activity
	 * @param loginViewModel
	 * @param currentYear
	 * @param xnm
	 * @param xqm
	 * @return
	 */
	@Nullable
	public static String queryMark(Activity activity, LoginViewModel loginViewModel, int currentYear, String xnm, String xqm){
		Mark[] newMarks = QUSTQueryUtil.queryMark(loginViewModel, xnm, xqm);
		
		Mark[][] marks = null;
		
		try{
			marks = (Mark[][])loadData(activity, "Mark", "mark");
		}catch(Exception ignored){
			marks = new Mark[0][];
		}
		
		if(marks.length > currentYear && newMarks.length > marks[currentYear].length){
			marks[currentYear] = newMarks;
			saveData(activity, "Mark","mark", marks);
			return "已查询到新的成绩！";
		}else{
			return null;
		}
	}
	
	/**
	 * 查询最新的考试
	 * @param activity
	 * @param loginViewModel
	 * @param currentYear
	 * @param xnm
	 * @param xqm
	 * @return
	 */
	@Nullable
	public static String queryExam(Activity activity, LoginViewModel loginViewModel, int currentYear, String xnm, String xqm){
		Exam[] newExams = QUSTQueryUtil.queryExam(loginViewModel, xnm, xqm);
		
		try{
			Exam[][] exams = (Exam[][])loadData(activity, "Exam","exam");
			if(newExams.length > exams[currentYear].length){
				exams[currentYear] = newExams;
				saveData(activity, "Exam","exam", exams);
				return "已查询到新的成绩！";
			}else{
				return null;
			}
		}catch(Exception e){
			return null;
		}
	}
	
	
	public static Object loadData(@NonNull Context context, String file, String name) throws Exception{
		File f = new File(context.getExternalFilesDir(file), name);
		if(f.exists()){
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
			Object o = stream.readObject();
			stream.close();
			return o;
		}else throw new FileNotFoundException();
	}
	
	protected static void saveData(@NonNull Context context, String file, String name, Object o){
		try{
			File f = new File(context.getExternalFilesDir(file), name);
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
			stream.writeObject(o);
			stream.flush();
			stream.close();
		}catch(IOException ignored){ }
	}
	
}
