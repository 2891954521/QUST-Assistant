package com.qust.assistant.ui.app;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.RelativeGuide;
import com.qust.assistant.R;
import com.qust.assistant.ui.base.BaseAnimActivity;
import com.qust.assistant.ui.base.BaseFragment;
import com.qust.assistant.ui.fragment.DailyLessonFragment;
import com.qust.assistant.ui.fragment.TermLessonFragment;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.widget.menu.MenuAdapter;
import com.qust.assistant.widget.menu.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 主页自定义界面
 */
public class CustomizeActivity extends BaseAnimActivity{
	
	/**
	 * 隐藏的列表
	 */
	private ArrayList<MenuItem> hideItems;
	
	/**
	 * 选中的列表
	 */
	private ArrayList<MenuItem> selectedItems;
	
	private MenuAdapter hideAdapter;
	
	private SelectItemAdapter selectedAdapter;
	
	/**
	 * 展示的界面
	 */
	private int homeOffset;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customize);
		
		initMenuItems();
		initToolBar("自定义主页");
		
		hideAdapter = new MenuAdapter(this, hideItems);
		selectedAdapter = new SelectItemAdapter(this, selectedItems);
		
		ListView menuList = findViewById(R.id.menu_list);
		menuList.setAdapter(hideAdapter);
		menuList.setOnItemClickListener((parent, view, position, id) -> {
			selectedItems.add(hideItems.get(position));
			selectedAdapter.setItems(selectedItems);
			hideItems.remove(position);
			hideAdapter.setItems(hideItems);
		});
		
		ListView selectedList = findViewById(R.id.selected_list);
		selectedList.setAdapter(selectedAdapter);
		selectedList.setOnItemClickListener((parent, view, position, id) -> {
			homeOffset = position;
			selectedAdapter.notifyDataSetInvalidated();
		});
		
		ImageView save = addMenuItem(R.drawable.ic_done, v -> {
			JSONArray js = new JSONArray();
			for(MenuItem item : selectedItems) js.put(item.target);
			SettingUtil.edit().putString(getString(R.string.homePages), js.toString()).commit();
			SettingUtil.edit().putInt(getString(R.string.homeOffset), homeOffset).commit();
			toast("保存成功，重启应用后生效");
			finish();
		});
		
		NewbieGuide.with(this).setLabel(getClass().getName())
				.addGuidePage(GuidePage.newInstance()
					.addHighLight(menuList, new RelativeGuide(R.layout.layout_welcome_customize, Gravity.TOP, 50))
				).addGuidePage(GuidePage.newInstance()
					.addHighLight(selectedList, new RelativeGuide(R.layout.layout_welcome_customize2, Gravity.BOTTOM, 50))
				).addGuidePage(GuidePage.newInstance()
						.addHighLight(save, HighLight.Shape.CIRCLE, 20)
						.setLayoutRes(R.layout.layout_welcome_customize3)
				).show();
	
}
	
	private void initMenuItems(){
		HashMap<String, MenuItem> allItems = new HashMap<>(16);
		try{
			String androidNM = "http://schemas.android.com/apk/res/android";
			
			for(XmlResourceParser parser = getResources().getXml(R.xml.menu); parser.getEventType() != XmlResourceParser.END_DOCUMENT; parser.next()){
				
				if(parser.getEventType() != XmlResourceParser.START_TAG) continue;
				if(!"item".equals(parser.getName())) continue;
				
				String target = parser.getAttributeValue(androidNM, "text");
				Class<?> clazz = Class.forName(target);
				
				if(!BaseFragment.class.isAssignableFrom(clazz)) continue;
				
				MenuItem item = new MenuItem();
				item.title = parser.getAttributeValue(androidNM, "name");
				item.target = target;
				item.icon = Xml.asAttributeSet(parser).getAttributeResourceValue(androidNM, "icon", 0);
				item.hasMask = Boolean.parseBoolean(parser.getAttributeValue(androidNM, "isDefault"));
				
				allItems.put(item.target, item);
			}
		}catch(XmlPullParserException | IOException | ClassNotFoundException e){
			LogUtil.Log(e);
		}
		
		homeOffset = SettingUtil.getInt(getString(R.string.homeOffset), 0);
		
		try{
			JSONArray js = new JSONArray(SettingUtil.getString(getString(R.string.homePages), ""));
			selectedItems = new ArrayList<>(js.length());
			for(int i = 0; i < js.length(); i++){
				String target = js.getString(i);
				MenuItem item = allItems.get(target);
				if(item != null){
					allItems.remove(target);
					selectedItems.add(item);
				}
			}
		}catch(JSONException e){
			selectedItems = new ArrayList<>(16);
			selectedItems.add(allItems.get(DailyLessonFragment.class.getName()));
			selectedItems.add(allItems.get(TermLessonFragment.class.getName()));
			allItems.remove(DailyLessonFragment.class.getName());
			allItems.remove(TermLessonFragment.class.getName());
		}finally{
			hideItems = new ArrayList<>(allItems.values());
		}
	}
	
	private class SelectItemAdapter extends MenuAdapter{
		
		public SelectItemAdapter(Context context, ArrayList<MenuItem> items){
			super(context, items);
		}
		
		@Override
		protected int getLayout(){
			return R.layout.item_menu_editable;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			convertView = super.getView(position, convertView, parent);
			convertView.findViewById(R.id.item_menu_remove).setOnClickListener(v -> {
				hideItems.add(selectedItems.get(position));
				hideAdapter.setItems(hideItems);
				selectedItems.remove(position);
				setItems(selectedItems);
			});
			convertView.findViewById(R.id.item_menu_home).setVisibility(position == homeOffset ? View.VISIBLE : View.INVISIBLE);
			return convertView;
		}
	}
}
