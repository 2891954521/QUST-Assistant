package com.qust.fragment.ea;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qust.assistant.R;
import com.qust.model.QUSTQueryModel;
import com.qust.assistant.vo.Notice;
import com.qust.base.HandlerCode;
import com.qust.base.fragment.BaseEAFragment;
import com.qust.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class NoticeFragment extends BaseEAFragment{
	
	private Notice[] notices;
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		notices = new Notice[0];
		
		try{
			notices = (Notice[]) FileUtils.loadData(new File(activity.getFilesDir(), "notice"));
		}catch(Exception e){
			notices = new Notice[0];
		}
		
		initList(new NoticeAdapter());
		
		addMenuItem(inflater, R.drawable.ic_refresh, v -> beforeQuery());
	}
	
	@Override
	protected void doQuery(){
		sendMessage(HandlerCode.UPDATE_DIALOG, "正在查询");
		
		notices = QUSTQueryModel.queryNotice(eaViewModel, 1, 20);
		
		try{
			FileUtils.saveData(new File(activity.getFilesDir(), "notice"), notices);
		}catch(IOException ignored){ }
		
		sendMessage(HandlerCode.NOTIFY_TOAST, "查询成功！");
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
