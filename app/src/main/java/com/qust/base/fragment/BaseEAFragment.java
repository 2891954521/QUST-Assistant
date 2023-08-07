package com.qust.base.fragment;

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
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.HandlerCode;
import com.qust.lesson.LessonTableModel;

/**
 * 教务查询的基类
 */
public abstract class BaseEAFragment extends BaseFragment{
	
	public static final String[] TERM_NAME = {
			"大一 上学期","大一 下学期",
			"大二 上学期","大二 下学期",
			"大三 上学期","大三 下学期",
			"大四 上学期","大四 下学期",
	};
	
	
	protected BaseAdapter adapter;
	
	protected MaterialDialog dialog;
	
	/**
	 * 年份选择器
	 */
	protected NumberPicker yearPicker;
	
	/**
	 * 入学时间
	 */
	protected int entranceTime;
	
	protected EAViewModel eaViewModel;
	
	
	protected Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case HandlerCode.UPDATE_DIALOG:
					dialog.setContent((String)msg.obj);
					break;
				case HandlerCode.NOTIFY_TOAST:
					if(adapter == null){
						toast("刷新数据失败！");
					}else{
						adapter.notifyDataSetChanged();
						toast((String)msg.obj);
					}
					dialog.dismiss();
					break;
				case HandlerCode.DISMISS_TOAST:
					dialog.dismiss();
					toast((String)msg.obj);
					break;
			}
		}
	};
	
	public BaseEAFragment(){
		super();
	}
	
	public BaseEAFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		entranceTime = SettingUtil.getInt(getString(R.string.KEY_ENTRANCE_TIME), 0);
		
		dialog = DialogUtil.getIndeterminateProgressDialog(activity, "查询中").build();
		
		View view = findViewById(R.id.fragment_school_query);
		if(view != null) view.setOnClickListener(v -> beforeQuery());
		
		eaViewModel = EAViewModel.getInstance(getContext());
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
		yearPicker.setValue(LessonTableModel.getCurrentYear(entranceTime));
	}
	
	protected void beforeQuery(){
		dialog.show();
		new Thread(this::doQuery).start();
	}
	
	/**
	 * 执行查询的函数
	 */
	protected abstract void doQuery();
	
	@Override
	protected abstract int getLayoutId();
	
	@Override
	public abstract String getName();
	
	/**
	 * 获取选择的学期参数
	 */
	protected String[] getYearAndTerm(){
		if(entranceTime == 0) sendMessage(HandlerCode.TOAST, "未设置入学年份，可能导致查询结果异常");
		return new String[]{
				Integer.toString(yearPicker.getValue() / 2 + entranceTime),
				yearPicker.getValue() % 2 == 0 ? "3" : "12"
		};
	}
	
	protected final void sendMessage(int code, String msg){
		handler.sendMessage(handler.obtainMessage(code, msg));
	}
}
