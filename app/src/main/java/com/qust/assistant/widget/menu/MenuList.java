package com.qust.assistant.widget.menu;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.qust.assistant.R;
import com.qust.base.ui.FragmentActivity;
import com.qust.base.ui.BaseActivity;
import com.qust.base.fragment.BaseFragment;
import com.qust.base.ui.MenuAble;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 侧滑菜单
 */
public class MenuList extends ListView implements AdapterView.OnItemClickListener{
	
	private final MenuAble activity;
	
	private final ArrayList<MenuItem> items;
	
	public MenuList(Context context){ this(context, null); }
	
	public MenuList(Context context, AttributeSet attributeSet){ this(context, attributeSet, 0); }
	
	public MenuList(Context context, AttributeSet attributeSet, int defStyleAttr){
		super(context, attributeSet, defStyleAttr);
		activity = (MenuAble)context;
		
		items = new ArrayList<>(16);

		try{
			String androidNM = "http://schemas.android.com/apk/res/android";
			XmlResourceParser parser = context.getResources().getXml(R.xml.menu_main);
			while(parser.getEventType() != XmlResourceParser.END_DOCUMENT){
				if(parser.getEventType() == XmlResourceParser.START_TAG){
					String tagName = parser.getName();
					if("item".equals(tagName)){
						MenuItem item = new MenuItem();
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
		
		setAdapter(new MenuAdapter(context, items));
		
		setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		MenuItem item = items.get(position);
		try{
			Class<?> clazz = Class.forName(item.target);
			if(BaseFragment.class.isAssignableFrom(clazz)){
				activity.closeMenu();
				getContext().startActivity(new Intent(getContext(), FragmentActivity.class).putExtra("fragmentClazz", clazz));
			}else if(BaseActivity.class.isAssignableFrom(clazz)){
				activity.closeMenu();
				getContext().startActivity(new Intent(getContext(), clazz));
			}else{
				Toast.makeText(getContext(), "参数错误", Toast.LENGTH_SHORT).show();
			}
		}catch(ClassNotFoundException ignored){ }
	}
}
