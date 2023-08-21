package com.qust.model;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.account.NeedLoginException;
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.NotificationUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.vo.Exam;
import com.qust.assistant.vo.Mark;
import com.qust.assistant.vo.Notice;
import com.qust.lesson.LessonTableModel;

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
public class AutoQueryModel{

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
				
				int currentYear = LessonTableModel.getCurrentYear(entranceTime);
				
				String xnm = Integer.toString(currentYear / 2 + entranceTime);
				String xqm = currentYear % 2 == 0 ? "3" : "12";
				
				EAViewModel eaViewModel = EAViewModel.getInstance(activity);
				try{
					if(eaViewModel.isLogin() || eaViewModel.loginSync()){
						String noticeContent = queryNotice(activity, eaViewModel);
						if(noticeContent != null) NotificationUtil.sendNotification(activity, "教务通知", noticeContent);
						
						String mark = queryMark(activity, eaViewModel, currentYear, xnm, xqm);
						if(mark != null) NotificationUtil.sendNotification(activity, "成绩查询", mark);
						
						String exam = queryExam(activity, eaViewModel, currentYear, xnm, xqm);
						if(exam != null) NotificationUtil.sendNotification(activity, "考试查询", mark);
					}
				}catch(NeedLoginException | IOException ignore){ }
				
				SettingUtil.edit().putLong(activity.getString(R.string.last_auto_check_time), current).apply();
			}
		}.start();

	}
	
	/**
	 * 查询最新的教务通知
	 * @param activity
	 * @param eaViewModel
	 * @return
	 */
	@Nullable
	public static String queryNotice(Activity activity, EAViewModel eaViewModel) throws NeedLoginException{
		Notice[] notices = QUSTQueryModel.queryNotice(eaViewModel);
		if(notices.length == 0) return null;
		
		Notice notice = notices[0];
		String id = SettingUtil.getString(activity.getString(R.string.last_notice_id), "");
		if(id.equals(notice.id)) return null;
		
		SettingUtil.edit().putString(activity.getString(R.string.last_notice_id), notice.id).apply();
		
		return notice.xxnr + "\n\n" + notice.cjsj;
	}
	
	
	@Nullable
	public static String queryLessonTable(Activity activity, EAViewModel eaViewModel, int entranceTime, String xnm, String xqm){
		// TODO
		return null;
	}
	
	/**
	 * 查询最新的成绩
	 * @param activity
	 * @param eaViewModel
	 * @param currentYear
	 * @param xnm
	 * @param xqm
	 * @return
	 */
	@Nullable
	public static String queryMark(Activity activity, EAViewModel eaViewModel, int currentYear, String xnm, String xqm) throws NeedLoginException{
		Mark[] newMarks = QUSTQueryModel.queryMark(eaViewModel, xnm, xqm);
		
		Mark[][] marks;
		
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
	 * @param eaViewModel
	 * @param currentYear
	 * @param xnm
	 * @param xqm
	 * @return
	 */
	@Nullable
	public static String queryExam(Activity activity, EAViewModel eaViewModel, int currentYear, String xnm, String xqm) throws NeedLoginException{
		Exam[] newExams = QUSTQueryModel.queryExam(eaViewModel, xnm, xqm);
		
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
