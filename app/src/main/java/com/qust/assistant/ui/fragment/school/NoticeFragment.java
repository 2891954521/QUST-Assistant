package com.qust.assistant.ui.fragment.school;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.util.QustUtil.NoticeUtil;
import com.qust.assistant.vo.Notice;

import java.io.IOException;

public class NoticeFragment extends BaseSchoolFragment{
	
	private Notice[] notices;
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		notices = new Notice[0];
		
		try{
			notices = (Notice[])loadData("Notice","notice");
		}catch(Exception e){
			notices = new Notice[0];
		}
		
		initList(new NoticeAdapter());
		
		addMenuItem(inflater, R.drawable.ic_refresh, v -> doLogin());
	}
	
	@Override
	protected void doQuery(){
		sendMessage(App.UPDATE_DIALOG, "正在查询");
		
		notices = NoticeUtil.queryNotice(loginViewModel, 1, 20);
		
		try{
			saveData("Notice","notice", notices);
		}catch(IOException ignored){ }
		
		sendMessage(App.NOTIFY_TOAST, "查询成功！");
	}
	
	
	private class NoticeAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return notices.length; }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			NoticeViewHolder viewHolder;
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_notice, null);
				viewHolder = new NoticeViewHolder(convertView);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (NoticeViewHolder) convertView.getTag();
			}
			viewHolder.setNotice(notices[position]);
			return convertView;
		}
	}
	
	private static class NoticeViewHolder{
		
		private TextView time, content;
		
		public NoticeViewHolder(@NonNull View convertView){
			time = convertView.findViewById(R.id.item_notice_time);
			content = convertView.findViewById(R.id.item_notice_content);
		}
		
		public void setNotice(@NonNull Notice notice){
			time.setText(notice.cjsj);
			content.setText(notice.xxnr);
		}
		
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_notice;
	}
	
	@Override
	public String getName(){
		return "教务通知";
	}
}
