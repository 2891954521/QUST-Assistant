package com.qust.assistant.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.third.DrinkFragment;
import com.qust.assistant.util.SettingUtil;

public class HomeFragment extends BaseFragment{
	
	private int current;
	
	private ViewPager2 viewPager;
	
	private BaseFragment[] layouts;
	
	private TermLessonFragment termLesson;
	
	private DailyLessonFragment dailyLesson;
	
	public HomeFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		termLesson = new TermLessonFragment(activity);
		
		dailyLesson = new DailyLessonFragment(activity);
		
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
			layouts = new BaseFragment[]{ new DrinkFragment(activity), dailyLesson, termLesson };
			viewPager.setAdapter(new PagerAdapter());
			viewPager.setCurrentItem(1, false);
			current = 1;
		}else{
			layouts = new BaseFragment[]{ dailyLesson, termLesson };
			viewPager.setAdapter(new PagerAdapter());
			current = 0;
		}
		
	}
	
	@Override
	public void onPause(){
		layouts[viewPager.getCurrentItem()].onPause();
	}
	
	@Override
	public void onResume(){
		if(dailyLesson.isCreated()){
			dailyLesson.updateLesson();
		}
		layouts[viewPager.getCurrentItem()].onResume();
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
	
	public class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.LayoutHolder>{
		
		@NonNull
		@Override
		public PagerAdapter.LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new PagerAdapter.LayoutHolder();
		}
		
		@Override
		public void onBindViewHolder(@NonNull PagerAdapter.LayoutHolder holder, int position){
			holder.setView(layouts[position].getLayout());
		}
		
		@Override
		public int getItemCount(){
			return layouts.length;
		}
		
		public class LayoutHolder extends RecyclerView.ViewHolder{
			
			public LayoutHolder(){
				super(new RelativeLayout(activity));
				itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			}
			
			public void setView(View view){
				view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
				((RelativeLayout)itemView).removeAllViews();
				((RelativeLayout)itemView).addView(view);
			}
		}
	}
}

