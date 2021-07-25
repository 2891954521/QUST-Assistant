package com.university.assistant.ui.third;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.university.assistant.R;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class CpDailyActivity extends BaseAnimActivity{
	
	private static final String[] FORM_TITLE = {
			"今天你的所在地是？",
			"今天你的体温是多少？",
			"今天你的身体状况是？",
			"近14天你或你的共同居住人是否有疫情中、高风险区域人员接触史？",
			"近14天你或你的共同居住人是否和确诊、疑似病人接触过？",
			"近14天你或你的共同居住人是否是确诊、疑似病例？",
			"你或你的共同居住人目前是否被医学隔离？",
			"今天你当地的健康码颜色是？",
			"本人是否承诺以上所填报的全部内容均属实、准确，不存在任何隐瞒与不实的情况，更无遗漏之处"
	};
	
	private static final String[] FORM_CONTENT = {
			"山东省/青岛市/崂山区",
			"37.2℃及以下",
			"健康",
			"否",
			"否",
			"否",
			"否",
			"绿色",
			"是"
	};

	private TextInputLayout nameText, passwordText;
	
	private MaterialDialog dialog;
	
	private String cookie;
	
	private ArrayList<Form> forms;
	
	private FormAdapter adapter;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cpdaily);
		
		forms = new ArrayList<>();
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		nameText = findViewById(R.id.activity_cpdaily_name);
		passwordText = findViewById(R.id.activity_cpdaily_password);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("处理中...").build();
		
		nameText.getEditText().setText(data.getString("cpdaily_user",""));
		passwordText.getEditText().setText(data.getString("cpdaily_password",""));
		
		adapter = new FormAdapter();
		ListView listView = findViewById(R.id.activity_cpdaily_list);
		listView.setAdapter(adapter);
		
		findViewById(R.id.activity_cpdaily_login).setOnClickListener(v -> {
			new Thread(){
				@Override
				public void run(){
					String user = nameText.getEditText().getText().toString();
					String password = passwordText.getEditText().getText().toString();
					if(!getSession(user,password)){
						runOnUiThread(() -> {
							dialog.dismiss();
							toast("登陆失败！用户名或密码错误！");
						});
					}else if(!queryForm()){
						runOnUiThread(() -> {
							dialog.dismiss();
							toast("查询失败！");
						});
					}else{
						SharedPreferences.Editor editor = data.edit();
						editor.putString("cpdaily_user",user);
						editor.putString("cpdaily_password",password);
						editor.apply();
						runOnUiThread(() -> {
							dialog.dismiss();
							adapter.notifyDataSetChanged();
						});
					}
					
				}
			}.start();
			dialog.show();
		});
		
		initToolBar(null);
		initSliding(null, null);
		
	}

	private boolean getSession(String name, String password){
		try{
			String str = WebUtil.doPost(
					"http://139.224.16.208:8080/wisedu-unified-login-api-v1.0/api/login",
					null,
					"login_url=https://qust.campusphere.net/iap/login?service=https%3A%2F%2Fqust.campusphere.net%2Fportal%2Flogin&username="
							+ name + "&password=" + password);
			
			if(str == null){
				return false;
			}
			
			JSONObject js = new JSONObject(str);
			if(js.has("cookies")){
				cookie = js.getString("cookies");
				return true;
			}else toast(js.getString("msg"));
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private boolean queryForm(){
		try{
			String str = WebUtil.doPost(
					"https://qust.campusphere.net/wec-counselor-collector-apps/stu/collector/queryCollectorProcessingList",
					cookie,
					"{\"pageSize\":6,\"pageNumber\":1}",
					"Content-Type", "application/json; charset=utf-8"
			);
			
			if(str == null) return false;
			
			JSONArray array = new JSONObject(str).getJSONObject("datas").getJSONArray("rows");
			if(array.length()==0){
				toast("没有新问卷要填");
				return false;
			}
			for(int i=0;i<array.length();i++){
				JSONObject js = array.getJSONObject(i);
				Form form = new Form();
				form.subject = js.getString("subject");
				form.wid = js.getString("wid");
				form.formWid = js.getString("formWid");
				form.isHandled = js.getInt("isHandled");
				
				forms.add(form);
			}
			return true;
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private boolean getFormInfo(Form form){
		try{
			String str = WebUtil.doPost(
					"https://qust.campusphere.net/wec-counselor-collector-apps/stu/collector/getFormFields",
					cookie,
					String.format("{\"pageSize\":100,\"pageNumber\":1,\"formWid\":%s,\"collectorWid\":%s}",form.formWid, form.wid),
					"Content-Type", "application/json; charset=utf-8"
			);
			
			if(str == null) return false;
			
			form.form =  new JSONObject(str).getJSONObject("datas").getJSONArray("rows");
			return true;
		
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private boolean fillForm(JSONArray form){
		try{
			int index = FORM_TITLE.length - 1;
			
			for(int i=form.length()-1;i>=0;i--){
				
				JSONObject item = form.getJSONObject(i);
				
				if(item.getInt("isRequired") == 1){
					
					if(FORM_TITLE[index].equals(item.getString("title"))){
						int type = item.getInt("fieldType");
						
						if(type == 1 || type == 5){
							item.put("value", FORM_CONTENT[index]);
							index--;
						}else if(type == 2){
							item.put("value", FORM_CONTENT[index]);
							JSONArray array = item.getJSONArray("fieldItems");
							for(int j=array.length()-1;j>=0;j--){
								if(!FORM_CONTENT[index].equals(array.getJSONObject(j).getString("content"))){
									array.remove(j);
								}
							}
							index--;
						}else form.remove(i);
						
					}else return false;
					
				}else form.remove(i);
				
			}
			return true;
		}catch(JSONException e){
			LogUtil.Log(e);
			return false;
		}
	}
	
	private boolean submitForm(Form form){
		try{
			String str = WebUtil.doPost(
					"https://qust.campusphere.net/wec-counselor-collector-apps/stu/collector/submitForm",
					cookie,
					String.format("{\"formWid\":\"%s\",\"collectWid\":\"%s\",\"form\":%s,\"uaIsCpadaily\": true,\"address\": \"中国山东省青岛市崂山区\",\"latitude\": \"36.12438451455235\",\"longitude\":\"120.48051763985441\"}",
							form.formWid, form.wid, form.form.toString()
					),
					"Content-Type", "application/json; charset=utf-8",
					"Cpdaily-Extension", "eZbW2qLZT0G0VbYqnj5mz5UCyZiuS+Mht0ro4VCSTgTancCpi4ru3IpfZibLN2Q4JR3dl7wYTXnTi5dzfAwbYcs5FB4VPqOTrcYNVjoRY9h9J7sxA1MWIWZxiEC7iuzXwAeEjrGmnHnX3P7mprZW66fbhNsIrM938cVo6aK7fgdQx6vGY7OVJBS+kqwk/xE2ipLqV0ro4QNZ9u/6G9MUbyd7QghLIM9PIRJTrd6TzoYPFBHqDHIY57dHHUBUC8RzfvreU/2o5sY="
			);
			
			if(str == null) return false;
			
			String message = new JSONObject(str).getString("message");
			if("SUCCESS".equals(message)){
				form.isHandled = 1;
				runOnUiThread(() -> toast("提交成功！"));
				return true;
			}else if("该收集已填写无需再次填写".equals(message)){
				form.isHandled = 1;
				runOnUiThread(() -> toast("已提交过！"));
				return true;
			}else{
				LogUtil.debugLog(message);
				runOnUiThread(() -> toast("提交失败：" + message));
			}
			
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
		}
		return false;
	}
	
	private void autoSubmitForm(int position){
		dialog.show();
		new Thread(){
			@Override
			public void run(){
				Form form = forms.get(position);
				if(!getFormInfo(form)){
					runOnUiThread(() -> {
						toast("获取表单信息失败！");
						dialog.dismiss();
					});
				}else if(!fillForm(form.form)){
					runOnUiThread(() -> {
						toast("表单自动填写失败！");
						dialog.dismiss();
					});
				}else if(!submitForm(form)){
					runOnUiThread(() -> dialog.dismiss());
				}else runOnUiThread(() -> {
					dialog.dismiss();
					adapter.notifyDataSetChanged();
				});
			}
		}.start();
	}
	
	private static class Form{
		
		public String wid;
		public String formWid;
		
		public String subject;
		public String createTime;
		
		public JSONArray form;
		
		public int isHandled;
	}
	
	private class FormAdapter extends BaseAdapter{
		@Override
		public int getCount(){
			return forms.size();
		}
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(CpDailyActivity.this).inflate(R.layout.item_cpdaily_form,null);
			}
			Form form = forms.get(position);
			((TextView)convertView.findViewById(R.id.item_cpdaily_title)).setText(form.subject);
			((TextView)convertView.findViewById(R.id.item_cpdaily_time)).setText(form.createTime);
			if(form.isHandled==1){
				convertView.findViewById(R.id.item_cpdaily_handled).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.item_cpdaily_submit).setVisibility(View.GONE);
			}else{
				convertView.findViewById(R.id.item_cpdaily_handled).setVisibility(View.GONE);
				Button button = convertView.findViewById(R.id.item_cpdaily_submit);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(v -> autoSubmitForm(position));
			}
			return convertView;
		}
	}
}
