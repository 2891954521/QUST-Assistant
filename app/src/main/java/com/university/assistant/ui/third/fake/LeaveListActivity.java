package com.university.assistant.ui.third.fake;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.university.assistant.R;
import com.university.assistant.util.DateUtil;
import com.university.assistant.util.DialogUtil;

import java.util.ArrayList;

public class LeaveListActivity extends Activity{
	
	private LinearLayout noLeave;
	
	private BaseAdapter adapter;
	
	private ArrayList<LeaveData.Data> data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_list);
		
		LeaveData.init(this);
		
		data = LeaveData.getInstance().data;
		
		adapter = new LeaveAdapter();
		
		ListView leaveList = findViewById(R.id.activity_leave_list);
		leaveList.setAdapter(adapter);
		
		leaveList.setOnItemClickListener((parent,view,position,id) -> startActivity(new Intent(LeaveListActivity.this,LeaveInfoActivity.class).putExtra("data",position)));
		
		leaveList.setOnItemLongClickListener((parent,view,position,id) -> {
			DialogUtil.getBaseDialog(LeaveListActivity.this)
					.title("删除").content("是否删除这一条请假记录?")
					.onPositive((dialog, witch) -> {
						LeaveData.getInstance().delete(position);
						adapter.notifyDataSetChanged();
						Toast.makeText(LeaveListActivity.this,"删除成功！",Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}).show();
		    return true;
		});
		
		noLeave = findViewById(R.id.activity_no_leave);
		
		findViewById(R.id.activity_back).setOnClickListener(v -> onBackPressed());
		
		findViewById(R.id.activity_new_leave).setOnClickListener(v -> startActivity(new Intent(LeaveListActivity.this,NewLeaveActivity.class)));
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(data.size() == 0){
			noLeave.setVisibility(View.VISIBLE);
		}else{
			noLeave.setVisibility(View.GONE);
		}
		adapter.notifyDataSetChanged();
	}
	
	private class LeaveAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){
			return data.size();
		}
		
		@Override
		public Object getItem(int position){
			return null;
		}
		
		@Override
		public long getItemId(int position){
			return 0;
		}
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(LeaveListActivity.this).inflate(R.layout.item_leave,null);
			}
			((TextView)convertView.findViewById(R.id.leave_type)).setText("我的 " + data.get(position).type + "申请");
			
			String s = data.get(position).sTime;
			((TextView)convertView.findViewById(R.id.leave_sTime)).setText(s.substring(s.indexOf("-") + 1));
			
			s = data.get(position).start;
			String b = data.get(position).end;
			String c = s.substring(s.indexOf("-") + 1) + " 至 " + b + "（共" + DateUtil.timeDifference(s,b) + ")";
			
			((TextView)convertView.findViewById(R.id.leave_time)).setText("请假时间：" + c);
			
			TextView leaveRestTime = convertView.findViewById(R.id.leave_rest_time);
			
			TextView status = convertView.findViewById(R.id.leave_status);
			if(data.get(position).isFinish){
				leaveRestTime.setVisibility(View.VISIBLE);
				leaveRestTime.setText("实际休假时间：" + c);
				status.setTextColor(Color.GRAY);
				status.setText("已完成");
			}else{
				leaveRestTime.setVisibility(View.GONE);
				status.setTextColor(Color.parseColor("#14bc7f"));
				status.setText("正在休假中");
			}
			
			return convertView;
		}
	}

}
