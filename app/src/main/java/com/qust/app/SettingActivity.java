package com.qust.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.qust.QustAPI;
import com.qust.account.ea.EAViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.ui.BaseAnimActivity;
import com.qust.lesson.LessonTableViewModel;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

public class SettingActivity extends BaseAnimActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.activity_setting_contain, new PrefsFragment()).commit();
		
		initToolBar("设置");
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
		switch (currentNightMode) {
			case Configuration.UI_MODE_NIGHT_NO:
				// Night mode is not active, we're using the light theme
				toast("light");
				break;
			case Configuration.UI_MODE_NIGHT_YES:
				// Night mode is active, we're using dark theme
				toast("dark");
				break;
		}
	}
	
	public static class PrefsFragment extends PreferenceFragmentCompat{
		
		private SettingActivity activity;
		
		private EAViewModel eaViewModel;
		
		private LessonTableViewModel lessonTableViewModel;
		
		public PrefsFragment(){}
		
		@Override
		public void onAttach(@NonNull Context context){
			super.onAttach(context);
			activity = (SettingActivity) getActivity();
			
			eaViewModel = EAViewModel.getInstance(activity);
			lessonTableViewModel = LessonTableViewModel.getInstance(activity);
		}
		
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState,String rootKey){
			addPreferencesFromResource(R.xml.preference_setting);
			
			Preference startDay = getSetting(getString(R.string.KEY_START_DAY));
			startDay.setSummary(DateUtil.YMD.format(lessonTableViewModel.getStartDay()));
			startDay.setOnPreferenceClickListener(p -> {
				final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
				calendar.setTime(lessonTableViewModel.getStartDay());
				
				final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.show();
				datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
					DatePicker picker = datePickerDialog.getDatePicker();
					calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
					p.setSummary(DateUtil.YMD.format(calendar.getTime()));
					lessonTableViewModel.setStartDay(calendar.getTime());
					activity.toast("设置完成!");
					datePickerDialog.dismiss();
				});
				return true;
			});
			
			Preference totalWeek = getSetting(getString(R.string.KEY_TOTAL_WEEK));
			totalWeek.setSummary(String.valueOf(lessonTableViewModel.getTotalWeek()));
			totalWeek.setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(getActivity()).title("学期总周数")
						.input("输入周数", String.valueOf(lessonTableViewModel.getTotalWeek()), (dialog, input) -> {})
						.inputType(InputType.TYPE_CLASS_NUMBER)
						.onPositive((dialog, which) -> {
							int week = Integer.parseInt(dialog.getInputEditText().getText().toString());
							if(32 < week || week < 1){
								activity.toastWarning("周数超过限制！");
								return;
							}
							p.setSummary(String.valueOf(week));
							lessonTableViewModel.setTotalWeek(week);
							activity.toast("设置完成!");
				}).show();
				return true;
			});
			
			Preference timeTable = getSetting(getString(R.string.KEY_TIME_TABLE));
			timeTable.setSummary(lessonTableViewModel.getCurrentTime() == 0 ? "冬季" : "夏季");
			timeTable.setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(getActivity()).title("课程时间")
						.items(new String[]{"冬季", "夏季"})
						.itemsCallbackSingleChoice(lessonTableViewModel.getCurrentTime(), (dialog, itemView, position, text) -> {
							p.setSummary(position == 0 ? "冬季" : "夏季");
							lessonTableViewModel.setCurrentTime(position);
							activity.toast("设置完成!");
							dialog.dismiss();
							return true;
						}).show();
				return true;
			});
			
			Preference entranceTime = getSetting(getString(R.string.KEY_ENTRANCE_TIME));
			int entrance = eaViewModel.getEntranceTime();
			entranceTime.setSummary(entrance == -1 ? "未设置" : String.valueOf(entrance));
			entranceTime.setOnPreferenceClickListener(p -> {
				ViewGroup layout = (ViewGroup)LayoutInflater.from(getActivity()).inflate(R.layout.layout_number_picker, null,false);
				
				final NumberPicker numberPicker = layout.findViewById(R.id.numberPicker);
				numberPicker.setWrapSelectorWheel(false);
				numberPicker.setMinValue(2010);
				
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				numberPicker.setMaxValue(currentYear);
				
				int v = eaViewModel.getEntranceTime();
				numberPicker.setValue(v == -1 ? currentYear : v);
				
				DialogUtil.getBaseDialog(getActivity()).title("入学年份")
					.customView(layout, false)
					.onPositive((dialog, what) -> {
						int val = numberPicker.getValue();
						eaViewModel.setEntranceTime(val);
						p.setSummary(String.valueOf(val));
					}).show();
				return true;
			});
			
			
			Preference eaHost = getSetting(getString(R.string.KEY_EA_HOST));
			int index =  SettingUtil.getInt(getString(R.string.KEY_EA_HOST), 0);
			eaHost.setSummary(QustAPI.EA_HOSTS[index]);
			eaHost.setOnPreferenceClickListener(p -> {
				DialogUtil.getBaseDialog(getActivity()).title("选择节点")
						.items(QustAPI.EA_HOSTS)
						.itemsCallbackSingleChoice(index, (dialog, itemView, position, text) -> {
							EAViewModel.getInstance(activity).changeEAHost(position);
							p.setSummary(QustAPI.EA_HOSTS[position]);
							activity.toast("设置完成");
							dialog.dismiss();
							return true;
						}).show();
				return true;
			});
			
			
			SwitchPreference dark = getSetting(getString(R.string.KEY_THEME_DARK));
			dark.setOnPreferenceChangeListener((preference, isDark) -> {
				AppCompatDelegate.setDefaultNightMode((boolean)isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
				return true;
			});
			
			getSetting("key_customize_home").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(getContext(), CustomizeActivity.class));
				return true;
			});
			
			getSetting("key_update").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(getContext(), UpdateActivity.class));
				return true;
			});
			
			getSetting("key_about").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(getContext(), AboutActivity.class));
				return true;
			});
			
			getSetting("key_log").setOnPreferenceClickListener(p -> {
				startActivity(new Intent(getContext(), LogActivity.class).putExtra("file","debug.log"));
				return true;
			});
			
			Preference p = getSetting("key_clear_log");
			int i = new File(LogUtil.LogFile).list().length;
			p.setSummary(i == 0 ? "无Log" : "共计：" + i + "条Log");
			p.setOnPreferenceClickListener(p1 -> {
				DialogUtil.getBaseDialog(getActivity()).title("提示").content("是否清除Log")
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
