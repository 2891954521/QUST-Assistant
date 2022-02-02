package com.qust.assistant.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.ui.BaseActivity;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class MenuList extends ListView implements AdapterView.OnItemClickListener{
	
	private final MainActivity activity;
	
	private final ArrayList<Item> items;
	
	public MenuList(Context context){ this(context, null); }
	
	public MenuList(Context context, AttributeSet attributeSet){ this(context, attributeSet, 0); }
	
	public MenuList(Context context, AttributeSet attributeSet, int defStyleAttr){
		super(context, attributeSet, defStyleAttr);
		activity = (MainActivity)context;
		
		items = new ArrayList<>(16);

		try{
			String androidNM = "http://schemas.android.com/apk/res/android";
			XmlResourceParser parser = context.getResources().getXml(R.xml.menu);
			while(parser.getEventType() != XmlResourceParser.END_DOCUMENT){
				if(parser.getEventType() == XmlResourceParser.START_TAG){
					String tagName = parser.getName();
					if("item".equals(tagName)){
						Item item = new Item();
						item.hasMask = Boolean.parseBoolean(parser.getAttributeValue(androidNM, "isDefault"));
						item.title = parser.getAttributeValue(androidNM, "name");
						item.target = parser.getAttributeValue(androidNM, "text");
						item.icon = Xml.asAttributeSet(parser).getAttributeResourceValue(androidNM, "icon", 0);
						items.add(item);
					}
				}
				parser.next();
			}
		}catch(XmlPullParserException | IOException e){
			e.printStackTrace();
		}
		
		setDivider(null);
		
		setAdapter(new MenuAdapter());
		
		setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		Item item = items.get(position);
		try{
			Class<?> clazz = Class.forName(item.target);
			if(BaseFragment.class.isAssignableFrom(clazz)){
				activity.closeMenu();
				activity.addView((Class<? extends BaseFragment>)clazz);
			}else if(BaseActivity.class.isAssignableFrom(clazz)){
				activity.closeMenu();
				activity.startActivity(new Intent(activity, clazz));
			}
		}catch(ClassNotFoundException ignored){ }
	}
	
	private class MenuAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return items.size(); }
		
		@Override
		public Object getItem(int index){ return null; }
		
		@Override
		public long getItemId(int index){ return 0L; }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
//			if(icon[position] == 0){
//				convertView = new View(MenuList.this.getContext());
//				convertView.setLayoutParams((ViewGroup.LayoutParams)new AbsListView.LayoutParams(-1, 2));
//				convertView.setPadding(30, 30, 30, 30);
//				convertView.setBackgroundColor(getResources().getColor(R.color.divider));
//			}else
			
			if(convertView == null){
				convertView = LayoutInflater.from(MenuList.this.getContext()).inflate(R.layout.item_menu, null);
			}
			
			Item item = items.get(position);
			
			((TextView)convertView.findViewById(R.id.item_menu_title)).setText(item.title);
			
			ImageView icon = convertView.findViewById(R.id.item_menu_icon);
			icon.setImageResource(item.icon);
			
			if(item.hasMask){
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) icon.setImageTintList(getContext().getColorStateList(R.color.colorAccent));
				else icon.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
			}else{
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) icon.setImageTintList(null);
				else icon.clearColorFilter();
			}
			
			return convertView;
		}
	}
	
	private static class Item{
		/**
		 * 图标
 		 */
		int icon;
		
		/**
		 * 图标是否有遮罩层
		 */
		boolean hasMask;
		
		/**
		 * 名称
		 */
		String title;
		
		/**
		 * 目标 Fragment 或 Activity 完整类名
		 */
		String target;
	}
}
