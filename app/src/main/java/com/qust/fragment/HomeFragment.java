package com.qust.fragment;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.qust.assistant.R;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.fragment.BaseFragment;
import com.qust.fragment.lesson.DailyLessonFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	}
	
	/**
	 * 加载主页界面
	 */
	private void initLayout(){
		current = SettingUtil.getInt(getString(R.string.homeOffset), 0);

		Set<String> set = SettingUtil.getStringSet(getString(R.string.homePages), new HashSet<>(0));
		if(!set.isEmpty()){
			ArrayList<BaseFragment> fragments = new ArrayList<>(set.size());
			for(Iterator<String> it = set.iterator(); it.hasNext();){
				try{
					Class<?> object = Class.forName(it.next());
					if(BaseFragment.class.isAssignableFrom(object)){
						try{
							fragments.add((BaseFragment)object.getConstructor(boolean.class, boolean.class).newInstance(true, false));
						}catch(NoSuchMethodException e){
							fragments.add((BaseFragment)object.newInstance());
						}
					}
				}catch(ReflectiveOperationException e){
					LogUtil.Log(e);
				}
			}
			layouts = fragments.toArray(new BaseFragment[0]);
		}else{
			DailyLessonFragment dailyLesson = new DailyLessonFragment(true, false);
			layouts = new BaseFragment[]{ dailyLesson };
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

