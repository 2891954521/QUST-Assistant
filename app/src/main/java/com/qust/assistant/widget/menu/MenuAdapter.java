package com.qust.assistant.widget.menu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.util.ColorUtil;

import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter{
	
	private final Context context;
	
	private ArrayList<MenuItem> items;
	
	public MenuAdapter(Context context, ArrayList<MenuItem> items){
		this.context = context;
		this.items = items;
	}
	
	public void setItems(ArrayList<MenuItem> items){
		this.items = items;
		notifyDataSetChanged();
	}
	
	protected int getLayout(){
		return R.layout.item_menu;
	}
	
	@Override
	public int getCount(){ return items.size(); }
	
	@Override
	public Object getItem(int index){ return null; }
	
	@Override
	public long getItemId(int index){ return 0L; }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(getLayout(), null);
		}
		
		MenuItem item = items.get(position);
		
		((TextView)convertView.findViewById(R.id.item_menu_title)).setText(item.title);
		
		ImageView icon = convertView.findViewById(R.id.item_menu_icon);
		icon.setImageResource(item.icon);
		
		if(item.hasMask){
			
			int color = ColorUtil.TEXT_COLORS[position % (ColorUtil.BACKGROUND_COLORS.length - 1) + 1];
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				icon.setImageTintList(new ColorStateList(new int[][]{{android.R.attr.state_checked }, {-android.R.attr.state_checked}}, new int[]{color, color}));
			}else{
				icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			}
		}else{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) icon.setImageTintList(null);
			else icon.clearColorFilter();
		}
		
		return convertView;
	}
}