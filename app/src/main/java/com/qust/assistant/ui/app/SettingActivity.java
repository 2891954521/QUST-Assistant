package com.qust.assistant.ui.app;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.qust.assistant.R;
import com.qust.assistant.model.LessonTableViewModel;
import com.qust.assistant.ui.base.BaseAnimActivity;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingActivity extends BaseAnimActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.activity_setting_contain, new PrefsFragment(this)).commit();
		
		initToolBar("设置");
	}
	
	public static class PrefsFragment extends PreferenceFragmentCompat{
		
		private final SettingActivity activity;
		
		private LessonTableViewModel lessonTableViewModel;
		
		public PrefsFragment(SettingActivity _activity){
			activity = _activity;
		}
		
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		}
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState,String rootKey){
			addPreferencesFromResource(R.xml.preference_setting);
			
			Preference startDay = getSetting(getString(R.string.KEY_START_DAY));
			
			startDay.setSummary(LessonTableViewModel.getStartDay());
			startDay.setOnPreferenceClickListener(p -> {
				final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
				try{
					Date date = DateUtil.YMD.parse(LessonTableViewModel.getStartDay());
					if(date != null) calendar.setTime(date);
				}catch(ParseException ignored){}
				
				final DatePickerDialog datePickerDialog = new DatePickerDialog(activity, null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.show();
				datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
					DatePicker picker = datePickerDialog.getDatePicker();
					calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
					String time = DateUtil.YMD.format(calendar.getTime());
					p.setSummary(time);
					lessonTableViewModel.setStartDay(time);
					activity.toast("设置完成!");
					datePickerDialog.dismiss();
				});
				return true;
			});
			
			Preference totalWeek = getSetting(getString(R.string.KEY_TOTAL_WEEK));
			totalWeek.setSummary(String.valueOf(LessonTableViewModel.getTotalWeek()));
			totalWeek.setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(activity).title("学期总周数")
						.input("输入周数", String.valueOf(LessonTableViewModel.getTotalWeek()), (dialog, input) -> {})
						.inputType(InputType.TYPE_CLASS_NUMBER)
						.onPositive((dialog, which) -> {
							int week = Integer.parseInt(dialog.getInputEditText().getText().toString());
							if(29 < week || week < 1){
								activity.toast("周数超过限制！");
								return;
							}
							p.setSummary(String.valueOf(week));
							lessonTableViewModel.setTotalWeek(week);
							activity.toast("设置完成!");
							dialog.dismiss();
				}).show();
				return true;
			});
			
			Preference timeTable = getSetting(getString(R.string.KEY_TIME_TABLE));
			timeTable.setSummary(SettingUtil.getInt(getString(R.string.KEY_TIME_TABLE), 0) == 0 ? "冬季" : "夏季");
			timeTable.setOnPreferenceClickListener(p -> {
				AtomicInteger time = new AtomicInteger(SettingUtil.getInt(getString(R.string.KEY_TIME_TABLE), 0));
				DialogUtil.getBaseDialog(activity).title("课程时间")
						.items(new String[]{"冬季", "夏季"})
						.itemsCallbackSingleChoice(time.get(), (dialog, itemView, position, text) -> {
							time.set(position);
							p.setSummary(position == 0 ? "冬季" : "夏季");
							lessonTableViewModel.setTimeTable(position);
							SettingUtil.edit().putInt(getString(R.string.KEY_TIME_TABLE), time.intValue()).apply();
							activity.toast("设置完成!");
							dialog.dismiss();
							return true;
						}).show();
				return true;
			});
			
			Preference entranceTime = getSetting(getString(R.string.KEY_ENTRANCE_TIME));
			int entrance = SettingUtil.getInt(getString(R.string.KEY_ENTRANCE_TIME), 0);
			entranceTime.setSummary(entrance == 0 ? "未设置" : String.valueOf(entrance));
			entranceTime.setOnPreferenceClickListener(p -> {
				ViewGroup layout = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.layout_number_picker, null,false);
				
				final NumberPicker numberPicker = layout.findViewById(R.id.numberPicker);
				numberPicker.setWrapSelectorWheel(false);
				numberPicker.setMinValue(2010);
				
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				numberPicker.setMaxValue(currentYear);
				numberPicker.setValue(SettingUtil.getInt(getString(R.string.KEY_ENTRANCE_TIME), currentYear));
				
				DialogUtil.getBaseDialog(activity).title("入学年份")
					.customView(layout, false)
					.onPositive((dialog, what) -> {
						int val = numberPicker.getValue();
						SettingUtil.put(getString(R.string.KEY_ENTRANCE_TIME), val);
						p.setSummary(String.valueOf(val));
					})
					.show();
				return true;
			});
			
			getSetting("key_customize_home").setOnPreferenceClickListener(p -> {
				activity.startActivity(new Intent(activity, CustomizeActivity.class));
				return true;
			});
			
			getSetting("key_update").setOnPreferenceClickListener(p -> {
				activity.startActivity(new Intent(activity, UpdateActivity.class));
				return true;
			});
			
			getSetting("key_about").setOnPreferenceClickListener(p -> {
				activity.startActivity(new Intent(activity, AboutActivity.class));
				return true;
			});
			
			getSetting("key_log").setOnPreferenceClickListener(p -> {
				activity.startActivity(new Intent(activity, LogActivity.class).putExtra("file","debug.log"));
				return true;
			});
			
			Preference p = getSetting("key_clear_log");
			int i = new File(LogUtil.LogFile).list().length;
			p.setSummary(i == 0 ? "无Log" : "共计：" + i + "条Log");
			p.setOnPreferenceClickListener(p1 -> {
				DialogUtil.getBaseDialog(activity).title("提示").content("是否清除Log")
						.onPositive((dialog,which) -> {
							for(File f : new File(LogUtil.LogFile).listFiles()) f.delete();
							p.setSummary("无Log");
							activity.toast("Log已清除！");
						}).show();
				return true;
			});
		}
		
		/**
		 * 没啥用，主要是防止ID不存在
		 */
		@NonNull
		private <T extends Preference> T getSetting(@NonNull String name){
			Preference preference = findPreference(name);
			if(preference == null){
				throw new NullPointerException("Preference is not existence");
			}else{
				return (T) preference;
			}
		}
	}
}
