package com.qust.assistant.ui.app;

import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qust.assistant.R;
import com.qust.assistant.ui.base.BaseAnimActivity;
import com.qust.assistant.ui.base.BaseFragment;
import com.qust.assistant.ui.fragment.DailyLessonFragment;
import com.qust.assistant.ui.fragment.TermLessonFragment;
import com.qust.assistant.util.ColorUtil;
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
	
	private RecyclerItemAdapter selectedAdapter;
	
	/**
	 * 作为主页的界面
	 */
	private int homePosition;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customize);
		
		initMenuItems();
		initToolBar("自定义主页");
		
		hideAdapter = new MenuAdapter(this, hideItems);
		ListView menuList = findViewById(R.id.menu_list);
		menuList.setAdapter(hideAdapter);
		menuList.setOnItemClickListener((parent, view, position, id) -> {
			selectedItems.add(hideItems.get(position));
			selectedAdapter.notifyItemInserted(selectedItems.size());
			hideItems.remove(position);
			hideAdapter.setItems(hideItems);
		});
		
		selectedAdapter = new RecyclerItemAdapter();
		RecyclerView selectedList = findViewById(R.id.selected_list);
		selectedList.setLayoutManager(new LinearLayoutManager(this));
		selectedList.setAdapter(selectedAdapter);
		
		helper.attachToRecyclerView(selectedList);
		
		addMenuItem(R.drawable.ic_done, v -> {
			JSONArray js = new JSONArray();
			for(MenuItem item : selectedItems) js.put(item.target);
			SettingUtil.edit().putString(getString(R.string.homePages), js.toString()).commit();
			SettingUtil.edit().putInt(getString(R.string.homeOffset), homePosition).commit();
			toast("保存成功，重启应用后生效");
			finish();
		});
	}
	
	/**
	 * 扫描可用的功能界面并初始化数据
	 */
	private void initMenuItems(){
		HashMap<String, MenuItem> allItems = new HashMap<>(16);
		try{
			String androidNM = "http://schemas.android.com/apk/res/android";
			
			for(XmlResourceParser parser = getResources().getXml(R.xml.menu_main); parser.getEventType() != XmlResourceParser.END_DOCUMENT; parser.next()){
				
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
		
		homePosition = SettingUtil.getInt(getString(R.string.homeOffset), 0);
		
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
			if(selectedItems.size() <= homePosition) homePosition = 0;
		}
	}
	
	
	private class RecyclerItemAdapter extends RecyclerView.Adapter<ItemViewHolder>{
		
		@NonNull
		@Override
		public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			View itemView = LayoutInflater.from(CustomizeActivity.this).inflate(R.layout.item_menu_editable, parent, false);
			ItemViewHolder holder = new ItemViewHolder(itemView);
			itemView.setOnClickListener(v -> {
				int tmp = homePosition;
				homePosition = holder.getAdapterPosition();
				selectedAdapter.notifyItemChanged(tmp);
				selectedAdapter.notifyItemChanged(homePosition);
			});
			return holder;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
			holder.onBind(selectedItems.get(position), position);
		}
		
		@Override
		public int getItemCount(){
			return selectedItems.size();
		}
	}
	
	private class ItemViewHolder extends RecyclerView.ViewHolder{
		
		private TextView title;
		private ImageView icon;
		
		private View isHome, remove;
		
		public ItemViewHolder(@NonNull View convertView){
			super(convertView);
			icon = convertView.findViewById(R.id.item_menu_icon);
			title = convertView.findViewById(R.id.item_menu_title);
			isHome = convertView.findViewById(R.id.item_menu_home);
			remove = convertView.findViewById(R.id.item_menu_remove);
			
			remove.setOnClickListener(v -> {
				int position = getAdapterPosition();
				if(position == -1) return;
				hideItems.add(selectedItems.get(position));
				selectedItems.remove(position);
				hideAdapter.setItems(hideItems);
				selectedAdapter.notifyItemRemoved(position);
			});
		}
		
		public void onBind(@NonNull MenuItem menuItem, int position){
			title.setText(menuItem.title);
			icon.setImageResource(menuItem.icon);
			isHome.setVisibility(position == homePosition ? View.VISIBLE : View.INVISIBLE);
			
			if(menuItem.hasMask){
				int color = ColorUtil.TEXT_COLORS[position % (ColorUtil.BACKGROUND_COLORS.length - 1) + 1];
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
					icon.setImageTintList(new ColorStateList(new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}}, new int[]{color, color}));
				}else{
					icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
				}
			}else{
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) icon.setImageTintList(null);
				else icon.clearColorFilter();
			}
		}
		
	}
	
	ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback(){
		
		@Override
		public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder){
			return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
		}
		
		@Override
		public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target){
			recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
			int fromPosition = viewHolder.getAdapterPosition();
			int toPosition = target.getAdapterPosition();
			if(fromPosition < toPosition){
				selectedItems.set(fromPosition, selectedItems.set(toPosition, selectedItems.get(fromPosition)));
			}else{
				selectedItems.set(toPosition, selectedItems.set(fromPosition, selectedItems.get(toPosition)));
			}
			if(fromPosition == homePosition){
				homePosition = toPosition;
			}else if(toPosition == homePosition){
				homePosition = fromPosition;
			}
			selectedAdapter.notifyItemMoved(fromPosition, toPosition);
			return true;
		}
		
		@Override
		public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){ }

	});
	
}
