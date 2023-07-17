package com.qust.assistant.ui.fragment;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.qust.assistant.R;
import com.qust.assistant.ui.base.BaseFragment;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HomeFragment extends BaseFragment{
	
	private int current;
	
	private ViewPager2 viewPager;
	
	private BaseFragment[] layouts;
	
	public HomeFragment(){
		super();
	}
	
	public HomeFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){

		viewPager = findViewById(R.id.viewpager2);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
			@Override
			public void onPageSelected(int position){
				layouts[current].onPause();
				layouts[position].onResume();
				toolbar.setTitle(layouts[position].getName());
				current = position;
			}
		});
		
		initLayout();

		NewbieGuide.with(activity)
				.setLabel(getClass().getName())
				.addGuidePage(GuidePage.newInstance().setLayoutRes(R.layout.layout_welcome_home))
				.show();
	}
	
	/**
	 * 加载主页界面
	 */
	private void initLayout(){
		current = SettingUtil.getInt(getString(R.string.homeOffset), 0);
		try{
			JSONArray js = new JSONArray(SettingUtil.getString(getString(R.string.homePages), ""));
			ArrayList<BaseFragment> fragments = new ArrayList<>(js.length());
			for(int i = 0; i < js.length(); i++){
				try{
					Class<?> object = Class.forName(js.getString(i));
					if(BaseFragment.class.isAssignableFrom(object)){
						fragments.add((BaseFragment)object.getConstructor(boolean.class, boolean.class).newInstance(true, false));
					}
				}catch(ReflectiveOperationException e){
					LogUtil.Log(e);
				}
			}
			layouts = fragments.toArray(new BaseFragment[0]);
		}catch(JSONException e){
			DailyLessonFragment dailyLesson = new DailyLessonFragment(true, false);
			TermLessonFragment termLesson = new TermLessonFragment(true, false);
			layouts = new BaseFragment[]{ dailyLesson, termLesson};
		}
		viewPager.setAdapter(new PagerAdapter(this));
		if(current < layouts.length) viewPager.setCurrentItem(current, false);
		else current = 0;
	}
	
	@Override
	public boolean onBackPressed(){
		return layouts[viewPager.getCurrentItem()].onBackPressed();
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_home;
	}
	
	@Override
	public String getName(){
		return null;
	}
	
	public class PagerAdapter extends FragmentStateAdapter{
		
		public PagerAdapter(@NonNull Fragment fragment){
			super(fragment);
		}
		
		@Override
		public int getItemCount(){
			return layouts.length;
		}
		
		@NonNull
		@Override
		public Fragment createFragment(int position){
			return layouts[position];
		}
	}
	
}

