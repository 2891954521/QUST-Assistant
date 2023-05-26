package com.qust.assistant.util.QustUtil;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.qust.assistant.R;
import com.qust.assistant.model.LoginViewModel;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.vo.Notice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 教务通知
 */
public class NoticeUtil{
	
	/**
	 * 查询教务通知
	 *
	 * @param loginViewModel 登录
	 * @param page 			 第几页
	 * @param pageSize  	 每页数量
	 */
	@NonNull
	public static Notice[] queryNotice(@NonNull LoginViewModel loginViewModel, int page, int pageSize){
		try{
			String response = loginViewModel.doPost(QustAPI.SCHOOL_SYSTEM_NOTICE,
					"queryModel.showCount=" + pageSize + "&queryModel.currentPage=" + page + "&queryModel.sortName=cjsj&queryModel.sortOrder=desc"
			);
			if(!"".equals(response)){
				ArrayList<Notice> array = new ArrayList<>();
				JSONArray item = new JSONObject(response).getJSONArray("items");
				for(int i = 0; i < item.length(); i++){
					array.add(new Notice(item.getJSONObject(i)));
				}
				return array.toArray(new Notice[0]);
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return new Notice[0];
	}
	
	@NonNull
	public static Notice[] queryNotice(@NonNull LoginViewModel loginViewModel){
		return queryNotice(loginViewModel, 1, 1);
	}
	
	
	public static void checkNotice(@NonNull Activity activity){
		
		long current = System.currentTimeMillis();
		long frequency = 1000 * 60 * 60 * 24;
		
		if(current - (long)SettingUtil.get(activity.getString(R.string.last_check_notice_time), 0L) < frequency){
			return;
		}
		
		new Thread(){
			@Override
			public void run(){
				LoginViewModel loginViewModel = LoginViewModel.getInstance(activity);
				if(loginViewModel.login() == null){
					return;
				}
				
				Notice[] notices = queryNotice(loginViewModel);
				SettingUtil.edit().putLong(activity.getString(R.string.last_check_notice_time), current).apply();
				if(notices.length == 0) return;
				
				Notice notice = notices[0];
				String id = SettingUtil.getString(activity.getString(R.string.last_notice_id), "");
				if(id.equals(notice.id)) return;
				
				SettingUtil.edit().putString(activity.getString(R.string.last_notice_id), notice.id).apply();
				activity.runOnUiThread(() -> DialogUtil.getBaseDialog(activity)
						.title("通知")
						.content(notice.xxnr + "\n\n" + notice.cjsj)
						.onPositive((dialog, which) -> dialog.dismiss())
						.show()
				);
			}
		}.start();
	}
}
