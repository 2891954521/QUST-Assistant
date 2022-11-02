package com.qust.assistant.ui.fragment.school;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.billy.android.swipe.SmartSwipe;
import com.billy.android.swipe.consumer.SpaceConsumer;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.QustUtil.LessonUtil;
import com.qust.assistant.util.SettingUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class BaseSchoolFragment extends BaseFragment{
	
	public static final String[] TERM_NAME = {
			"大一 上学期","大一 下学期",
			"大二 上学期","大二 下学期",
			"大三 上学期","大三 下学期",
			"大四 上学期","大四 下学期",
	};
	
	/**
	 * 处理登陆的类
	 */
	protected LoginViewModel loginViewModel;
	
	protected BaseAdapter adapter;
	
	protected MaterialDialog dialog;
	
	/**
	 * 年份选择器
	 */
	private NumberPicker yearPicker;
	
	protected int entranceTime;
	
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
	
	public BaseSchoolFragment(MainActivity activity){
		this(activity, false, true);
	}
	
	public BaseSchoolFragment(MainActivity activity, boolean isRoot, boolean hasToolBar){
		super(activity, isRoot, hasToolBar);
		entranceTime = SettingUtil.getInt(SettingUtil.KEY_ENTRANCE_TIME, 2010);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		dialog = DialogUtil.getIndeterminateProgressDialog(activity, "查询中").build();
		
		View view = findViewById(R.id.fragment_school_query);
		if(view != null) view.setOnClickListener(v -> doLogin());
		
		initViewModel();
	}
	
	protected void initViewModel(){
		loginViewModel = LoginViewModel.getInstance(activity);
		loginViewModel.getLoginResult().observe(this, result -> {
			if(result.from == handler){
				if(result.cookie != null){
					new Thread(this::doQuery).start();
				}else{
					handler.sendMessage(handler.obtainMessage(App.DISMISS_TOAST, result.message));
				}
			}
		});
	}
	
	protected void initList(BaseAdapter adapter){
		this.adapter = adapter;
		ListView listView = findViewById(R.id.fragment_school_list);
		listView.setAdapter(adapter);
		SmartSwipe.wrap(listView).addConsumer(new SpaceConsumer()).enableVertical();
	}
	
	/**
	 * 初始化学期选择器
	 */
	protected void initYearAndTermPicker(){
		yearPicker = findViewById(R.id.fragment_school_year);
		yearPicker.setWrapSelectorWheel(false);
		yearPicker.setDisplayedValues(TERM_NAME);
		yearPicker.setMinValue(0);
		yearPicker.setMaxValue(TERM_NAME.length - 1);
		yearPicker.setValue(LessonUtil.getCurrentYear(entranceTime));
	}
	
	protected void doLogin(){
		new Thread(() -> loginViewModel.login(activity, handler)).start();
		dialog.show();
	}
	
	/**
	 * 执行查询的函数
	 */
	protected abstract void doQuery();
	
	@Override
	protected abstract int getLayoutId();
	
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
	
	/**
	 * 获取选择的学期参数
	 */
	protected String[] getYearAndTerm(){
		return new String[]{
				Integer.toString(yearPicker.getValue() / 2 + entranceTime),
				yearPicker.getValue() % 2 == 0 ? "3" : "12"
		};
	}
	
	protected final void sendMessage(int code, String msg){
		handler.sendMessage(handler.obtainMessage(code, msg));
	}
}
