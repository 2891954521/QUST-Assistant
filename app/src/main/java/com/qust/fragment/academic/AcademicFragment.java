package com.qust.fragment.academic;

import android.view.LayoutInflater;
import android.widget.ExpandableListView;

import com.qust.account.NeedLoginException;
import com.qust.assistant.R;
import com.qust.base.HandlerCode;
import com.qust.base.fragment.BaseEAFragment;
import com.qust.model.AcademicModel;
import com.qust.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class AcademicFragment extends BaseEAFragment{
	
	/**
	 * 展示模式: 0 - 按课程类型，1 - 按修读年份
	 */
	private int showMode;
	
	/**
	 * 不同展示模式对应的Group
	 */
	private LessonInfoGroup[][] showModeGroup;
	
	private AcademicAdapter adapter;
	
	private ExpandableListView listView;
	
	public AcademicFragment(){
		super();
	}
	
	public AcademicFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		adapter = new AcademicAdapter(activity);
		
		listView = findViewById(R.id.activity_get_academic_list);
		listView.setAdapter(adapter);

		loadData();
	}
	
	@Override
	protected void initToolBar(){
		super.initToolBar();
		toolbar.inflateMenu(R.menu.menu_academic);
		toolbar.setOnMenuItemClickListener(item -> {
			int itemId = item.getItemId();
			if(itemId == R.id.menu_academic_show_mode){
				showMode = showMode == 0 ? 1 : 0;
				changeShowMode();
			}else if(itemId == R.id.menu_academic_update){
				startQuery();
			}
			return true;
		});
	}
	
	@Override
	protected void doQuery() throws NeedLoginException{
		sendMessage(HandlerCode.UPDATE_DIALOG, "正在查询");
		
		LessonResult lessonResult = AcademicModel.getLessons(eaViewModel);
		
		showModeGroup[0] = lessonResult.lessonInfoGroup;
		
		try{
			File file = new File(activity.getFilesDir(), "academic");
			if(!file.exists() && !file.mkdirs()) throw new IOException();
			FileUtils.saveData(new File(file, "groups"), lessonResult.lessonInfoGroup);
			FileUtils.saveData(new File(file, "lessons"), lessonResult.lessonInfo);
		}catch(IOException ignored){ }
		
		activity.runOnUiThread(() -> {
			dialog.dismiss();
			toast("查询完成");
			adapter.setLessons(showModeGroup[0], lessonResult.lessonInfo);
		});
	}
	
	/**
	 * 载入序列化后的数据
	 */
	private void loadData(){
		showModeGroup = new LessonInfoGroup[2][];
		LessonInfo[] lessons;
		try{
			File file = new File(activity.getFilesDir(), "academic");
			
			showModeGroup[0] = (LessonInfoGroup[]) FileUtils.loadData(new File(file, "groups"));
			lessons = (LessonInfo[]) FileUtils.loadData(new File(file, "lessons"));
		}catch(Exception e){
			showModeGroup[0] = new LessonInfoGroup[0];
			lessons = new LessonInfo[0];
		}
		adapter.setLessons(showModeGroup[0], lessons);
		listView.invalidateViews();
	}
	
	/**
	 * 切换展示模式
	 */
	private void changeShowMode(){
		if(showMode == 0){
			adapter.setLessons(showModeGroup[0]);
		}else{
			if(showModeGroup[1] == null){
				sortByTerm();
			}
			adapter.setLessons(showModeGroup[1]);
		}
	}
	
	/**
	 * 按修读学期分组
	 */
	private void sortByTerm(){
		LessonInfo[] lessons = adapter.getLessons();
		
		LessonInfoGroup.Builder[] builders = new LessonInfoGroup.Builder[TERM_NAME.length];
		
		for(int i = 0; i < builders.length; i++){
			builders[i] = new LessonInfoGroup.Builder();
			builders[i].group.groupName = TERM_NAME[i];
		}
		
		int entranceTime = eaViewModel.getEntranceTime();
		if(entranceTime == -1){
			toast("未设置入学年份");
			entranceTime = 0;
		}
		
		for(int i = 0; i < lessons.length; i++){
			LessonInfo lesson = lessons[i];
			int index = Math.max(0, (lesson.year - entranceTime) * 2 + lesson.term - 1);
			if(index < builders.length){
				builders[index].addLesson(i);
			}
		}
		
		LessonInfoGroup[] groups = new LessonInfoGroup[builders.length];
		for(int i = 0; i < groups.length; i++) groups[i] = builders[i].build();
		
		showModeGroup[1] = groups;
	}
	
	@Override
	public String getName(){
		return "学业情况查询";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_get_academic;
	}
}
