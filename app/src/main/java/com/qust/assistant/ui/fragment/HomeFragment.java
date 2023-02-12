package com.qust.assistant.ui.fragment;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.third.DrinkFragment;
import com.qust.assistant.util.SettingUtil;

public class HomeFragment extends BaseFragment{
	
	private int current;
	
	private ViewPager2 viewPager;
	
	private BaseFragment[] layouts;
	
	private TermLessonFragment termLesson;
	
	private DailyLessonFragment dailyLesson;
	
	public HomeFragment(){
		super();
	}
	
	public HomeFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		termLesson = new TermLessonFragment(true, false);
		dailyLesson = new DailyLessonFragment(true, false);
		
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
		
		if((boolean)SettingUtil.get(SettingUtil.KEY_SHOW_DRINK_CODE, false)){
			DrinkFragment drinkFragment = new DrinkFragment(true, false);
			layouts = new BaseFragment[]{ drinkFragment, dailyLesson, termLesson };
			viewPager.setAdapter(new PagerAdapter(this));
			viewPager.setCurrentItem(1, false);
			current = 1;
		}else{
			layouts = new BaseFragment[]{ dailyLesson, termLesson };
			viewPager.setAdapter(new PagerAdapter(this));
			current = 0;
		}
		
		NewbieGuide.with(activity)
				.setLabel(getClass().getName())
				.addGuidePage(GuidePage.newInstance().setLayoutRes(R.layout.layout_welcome_home))
				.show();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(isCreated() && dailyLesson.isCreated()){
			dailyLesson.updateLesson();
		}
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
	protected String getName(){
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

