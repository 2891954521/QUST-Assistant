package com.qust.assistant.ui.fragment.school;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.util.LoginUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public abstract class BaseSchoolFragment extends BaseFragment{
	
	protected String name, password;
	
	/**
	 * 处理登陆的类
	 */
	protected LoginUtil loginUtil;
	
	protected BaseAdapter adapter;
	
	protected MaterialDialog dialog;
	
	/**
	 * 年份选择器
	 */
	private NumberPicker yearPicker;
	
	protected Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case App.UPDATE_DIALOG:
					dialog.setContent((String)msg.obj);
					break;
				case App.NOTIFY_TOAST:
					if(adapter == null){
						toast("刷新数据失败！");
					}else{
						adapter.notifyDataSetChanged();
						toast((String)msg.obj);
					}
					dialog.dismiss();
					break;
				case App.DISMISS_TOAST:
					dialog.dismiss();
					toast((String)msg.obj);
					break;
			}
		}
	};
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		loginUtil = LoginUtil.getInstance();
		
		SharedPreferences sharedPreferences = activity.getSharedPreferences("education", 0);
		
		name = sharedPreferences.getString("user", null);
		password = sharedPreferences.getString("password", null);
		
		dialog = new MaterialDialog.Builder(activity).progress(true, 0).content("查询中...").build();
		
		findViewById(R.id.fragment_school_query).setOnClickListener(v -> {
			if(name == null || password == null){
				toast("请先登录！");
				activity.addView(LoginFragment.class);
				return;
			}
			new Thread(){
				@Override
				public void run(){
					String errorMsg = loginUtil.login(handler, name, password);
					if(errorMsg == null){
						doQuery(loginUtil.JSESSIONID);
					}else{
						sendMessage(App.DISMISS_TOAST, errorMsg);
					}
				}
			}.start();
			dialog.show();
		});
	}
	
	protected void initList(BaseAdapter adapter){
		this.adapter = adapter;
		((ListView)findViewById(R.id.fragment_school_list)).setAdapter(adapter);
	}
	
	/**
	 * 初始化学期选择器
	 */
	protected void initYearAndTermPicker(){
		yearPicker = findViewById(R.id.fragment_school_year);
		String[] term = new String[50];
		for(int i = 0; i < term.length; i++){
			term[i] = (i / 2 + 2010) + "-" + (i / 2 + 2011) + (i % 2 == 0 ? "第 1 学期" : "第 2 学期");
		}
		yearPicker.setDisplayedValues(term);
		yearPicker.setMinValue(0);
		yearPicker.setMaxValue(term.length - 1);
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		if(y < 2010){
			yearPicker.setValue(0);
		}else{
			int index = (y - 2010) * 2;
			if(calendar.get(Calendar.MONTH) < Calendar.AUGUST) index--;
			yearPicker.setValue(index < term.length ? index : term.length - 1);
		}
	}
	
	/**
	 * 执行查询的函数
	 * @param JSESSIONID cookie
	 */
	protected abstract void doQuery(String JSESSIONID);
	
	@Override
	protected abstract int getLayout();
	
	@Override
	protected abstract String getName();
	
	/**
	 * 从文件读取序列化后的数据
	 * @param file  文件夹名
	 * @param name  文件名
	 * @return      反序列化后的数据
	 */
	protected Object loadData(String file, String name) throws Exception{
		File f = new File(activity.getExternalFilesDir(file), name);
		if(f.exists()){
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
			Object o = stream.readObject();
			stream.close();
			return o;
		}else throw new FileNotFoundException();
	}
	
	/**
	 * 将获取到的数据序列化存储
	 * @param file  文件夹名
	 * @param name  文件名
	 * @param o     数据对象
 	 */
	protected void saveData(String file, String name, Object o) throws IOException{
		File f = new File(activity.getExternalFilesDir(file), name);
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
		stream.writeObject(o);
		stream.flush();
		stream.close();
	}
	
	protected String[] getYearAndTerm(){
		return new String[]{
				Integer.toString(yearPicker.getValue() / 2 + 2010),
				yearPicker.getValue() % 2 == 0 ? "3" : "12"
		};
	}
	
	protected final void sendMessage(int code, String msg){
		Message message = new Message();
		message.what = code;
		message.obj = msg;
		handler.sendMessage(message);
	}
}
