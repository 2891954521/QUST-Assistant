package com.university.assistant.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.DatePicker;

import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.lesson.LessonData;
import com.university.assistant.util.DateUtil;
import com.university.assistant.util.DialogUtil;
import com.university.assistant.util.LogUtil;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingActivity extends BaseAnimActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		getSupportFragmentManager().beginTransaction().replace(R.id.activity_setting_contain, new PrefsFragment(this)).commit();
		
		initToolBar(null);
		initSliding(null,null);
	}
	
	public static class PrefsFragment extends PreferenceFragmentCompat{
		
		private SettingActivity activity;
		
		public PrefsFragment(SettingActivity _activity){
			activity = _activity;
		}
		
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState,String rootKey){
			addPreferencesFromResource(R.xml.setting);
			
			findPreference("key_set_start_day").setOnPreferenceClickListener(p -> {
				final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
				
				final DatePicker picker = new DatePicker(getContext());
				picker.init(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH),null);
				
				DialogUtil.getBaseDialog(activity)
						.customView(picker,false)
						.onPositive((dialog, which) -> {
							c.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
							LessonData.getInstance().setStartDay(DateUtil.YMD.format(c.getTime()));
							activity.sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
							activity.toast("设置完成!");
							dialog.dismiss();
				}).show();
				return true;
			});
			
			findPreference("key_set_total_week").setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(activity).title("学期总周数")
						.input("输入周数","",(dialog,input) -> {})
						.inputType(InputType.TYPE_CLASS_NUMBER)
						.onPositive((dialog,which) -> {
							int week = Integer.parseInt(dialog.getInputEditText().getText().toString());
							if(29 < week || week < 1){
								activity.toast("周数超过限制！");
								return;
							}
							LessonData.getInstance().setTotalWeek(week);
							activity.sendBroadcast(new Intent(App.APP_UPDATE_LESSON_TABLE));
							activity.toast("设置完成!");
							dialog.dismiss();
				}).show();
				return true;
			});
			
			findPreference("key_set_lesson_time").setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(activity).title("课程时间")
						.items(new String[]{"冬季", "夏季"})
						.itemsCallback((dialog,itemView,position,text) -> {
							SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(activity);
							setting.edit().putInt("lessonTime",position).apply();
						})
						.onPositive((dialog,which) -> dialog.dismiss()).show();
				return true;
			});
			
			findPreference("key_update").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(activity, UpdateActivity.class));
				return true;
			});
			
			findPreference("key_log").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(activity, LogActivity.class).putExtra("file","debug.log"));
				return true;
			});
			
			Preference p = findPreference("key_clear_log");
			int i = new File(LogUtil.DebugLogFile).list().length;
			p.setSummary(i == 0 ? "无Log" : "共计：" + i + "条Log");
			p.setOnPreferenceClickListener(p1 -> {
				DialogUtil.getBaseDialog(activity).title("提示").content("是否清除Log")
						.onPositive((dialog,which) -> {
							for(File f : new File(LogUtil.DebugLogFile).listFiles()) f.delete();
							p.setSummary("无Log");
							activity.toast("Log已清除！");
						}).show();
				return true;
			});
		}
		
	}

	/*private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            if(intent.getAction()==null)return;
            switch(intent.getAction()){
                case SHOW_FLOAT:
                    //findPreference(SHOW_FLOAT).notifyDependencyChange(intent.getBooleanExtra(intent.getAction(),false));
                    break;
                case LOCK_FLOAT:
					//findPreference(LOCK_FLOAT).notifyDependencyChange(intent.getBooleanExtra(intent.getAction(),false));
                	break;
            }
        }
    };*/
	
}
