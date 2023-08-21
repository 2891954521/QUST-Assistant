package com.qust.fragment.ea;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qust.account.NeedLoginException;
import com.qust.assistant.R;
import com.qust.assistant.vo.Mark;
import com.qust.base.HandlerCode;
import com.qust.base.fragment.BaseEAFragment;
import com.qust.model.QUSTQueryModel;
import com.qust.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 成绩查询
 */
public class GetMarkFragment extends BaseEAFragment{
	
	/**
	 * 选中的学年
	 */
	private int selectTerm;
	
	private Mark[][] marks;
	
	private MarkAdapter adapter;
	
	public GetMarkFragment(){
		super();
	}
	
	public GetMarkFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		adapter = new MarkAdapter();
		
		try{
			marks = (Mark[][]) FileUtils.loadData(new File(activity.getFilesDir(), "mark"));
		}catch(Exception e){
			marks = new Mark[TERM_NAME.length][0];
		}

		initYearAndTermPicker();
		
		selectTerm = yearPicker.getValue();
		yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
			selectTerm = newVal;
			adapter.notifyDataSetChanged();
		});
		
		((ExpandableListView)findViewById(R.id.fragment_get_mark_list)).setAdapter(adapter);
	}
	
	@Override
	protected void doQuery() throws NeedLoginException{
		
		sendMessage(HandlerCode.UPDATE_DIALOG, "正在查询成绩");
		
		String[] y = getYearAndTerm();
		
		marks[selectTerm] = QUSTQueryModel.queryMark(eaViewModel, y[0], y[1]);
		
		try{
			FileUtils.saveData(new File(activity.getFilesDir(), "mark"), marks);
		}catch(IOException ignore){ }

		activity.runOnUiThread(() -> {
			dialog.dismiss();
			toast("查询完成");
			adapter.notifyDataSetChanged();
		});
	}
	
	@Override
	public String getName(){
		return "查成绩";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_get_mark;
	}
	
	
	private class MarkAdapter extends BaseExpandableListAdapter{
		
		@Override
		public int getGroupCount(){ return marks[selectTerm].length; }
		
		@Override
		public int getChildrenCount(int groupPosition){
			int len = marks[selectTerm][groupPosition].items.length;
			return len == 0 ? 0 : len + 1;
		}
		
		@Override
		public Object getGroup(int groupPosition){ return null; }
		
		@Override
		public Object getChild(int groupPosition, int childPosition){ return null; }
		
		@Override
		public long getGroupId(int groupPosition){ return 0; }
		
		@Override
		public long getChildId(int groupPosition, int childPosition){ return 0; }
		
		@Override
		public boolean hasStableIds(){ return false; }
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_mark_group, null);
			}
			
			if(isExpanded){
				convertView.findViewById(R.id.item_mark_background).setBackgroundResource(R.drawable.bg_stroke_top);
			}else{
				convertView.findViewById(R.id.item_mark_background).setBackgroundResource(R.drawable.bg_stroke);
			}
			
			Mark mark = marks[selectTerm][groupPosition];
			
			TextView nameText = convertView.findViewById(R.id.item_mark_name);
			nameText.setText(mark.name);
			nameText.setTextColor(activity.getResources().getColor(
					"正常考试".equals(mark.type) ? (mark.mark < 60 ? R.color.colorError : R.color.colorPrimaryText) : R.color.colorAccent
			));
			
			TextView markText = convertView.findViewById(R.id.item_mark_value);
			markText.setText(String.valueOf(mark.mark));
			markText.setTextColor(activity.getResources().getColor(
					mark.mark < 60 ? R.color.colorError : R.color.colorPrimaryText
			));
			
			((TextView)convertView.findViewById(R.id.item_mark_credit)).setText(mark.credit);
			
			((TextView)convertView.findViewById(R.id.item_mark_gpa)).setText(mark.gpa);
			
			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_mark, null);
			}
			
			convertView.findViewById(R.id.item_mark_background).setBackgroundResource(isLastChild ? R.drawable.bg_stroke_bottom : R.drawable.bg_stroke_center);
			
			Mark mark = marks[selectTerm][groupPosition];
			
			if(childPosition == 0){
				((TextView)convertView.findViewById(R.id.item_mark_item)).setText("项目");
				((TextView)convertView.findViewById(R.id.item_mark_value)).setText("成绩");
			}else{
				((TextView)convertView.findViewById(R.id.item_mark_item)).setText(mark.items[childPosition - 1]);
				((TextView)convertView.findViewById(R.id.item_mark_value)).setText(mark.itemMarks[childPosition - 1]);
			}
			
			return convertView;
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition,int childPosition){ return false; }
		
	}
}
