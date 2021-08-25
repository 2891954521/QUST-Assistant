package com.university.assistant.ui.third;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.university.assistant.R;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import androidx.annotation.Nullable;

public class CpDailyActivity extends BaseAnimActivity{

	private TextInputLayout nameText, passwordText;
	
	private MaterialDialog dialog;
	
	private Node[] provinceNode;
	
	private Node[] cityNode;
	
	private Node[] countyNode;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cpdaily);
		
		SharedPreferences data = getSharedPreferences("education",Context.MODE_PRIVATE);
		
		nameText = findViewById(R.id.activity_cpdaily_name);
		passwordText = findViewById(R.id.activity_cpdaily_password);
		
		dialog = new MaterialDialog.Builder(this).progress(true,0).content("处理中...").build();
		
		nameText.getEditText().setText(data.getString("cpdaily_user",""));
		passwordText.getEditText().setText(data.getString("cpdaily_password",""));
		
		findViewById(R.id.activity_cpdaily_submit).setOnClickListener(v -> {
			new Thread(){
				@Override
				public void run(){
					String user = nameText.getEditText().getText().toString();
					String password = passwordText.getEditText().getText().toString();
					
					try{
						String response = WebUtil.doGet("http://139.224.16.208/guide.json", null);
						String url = "http://139.224.16.208/classAffairs/cpdaily/commit.php";
						if(response != null){
							JSONObject js = new JSONObject(response);
							if(js.has("cpdailyCommit")) url = js.getString("cpdailyCommit");
						}
						response = WebUtil.doPost(url, null, "uid=" + user + "&password=" + password);
						
						if(response != null){
							String msg = new JSONObject(response).getString("msg");
							runOnUiThread(() -> {
								dialog.dismiss();
								toast(msg);
							});
						}
					}catch(IOException | JSONException e){
						runOnUiThread(() -> {
							dialog.dismiss();
							LogUtil.Log(e);
							toast("发生错误：" + e.getMessage());
						});
					}
				}
			}.start();
			dialog.show();
		});
		
		loadCity();
		
		NumberPicker province = findViewById(R.id.activity_cpdaily_location_province);
		NumberPicker city = findViewById(R.id.activity_cpdaily_location_city);
		NumberPicker county = findViewById(R.id.activity_cpdaily_location_county);
		
		province.setMinValue(0);
		city.setMinValue(0);
		county.setMinValue(0);
		
		province.setDisplayedValues(getPlaceName(provinceNode));
		province.setMaxValue(provinceNode.length - 1);
		
		province.setOnValueChangedListener((picker,oldVal,newVal) -> {
			cityNode = provinceNode[newVal].child;
			city.setValue(0);
			city.setMaxValue(1);
			city.setDisplayedValues(getPlaceName(cityNode));
			city.setMaxValue(cityNode.length - 1);
		});
		
		city.setOnValueChangedListener((picker,oldVal,newVal) -> {
			countyNode = cityNode[newVal].child;
			county.setValue(0);
			county.setMaxValue(1);
			county.setDisplayedValues(getPlaceName(countyNode));
			county.setMaxValue(countyNode.length - 1);
		});

		findViewById(R.id.activity_cpdaily_update_location).setOnClickListener(v -> {
			toast("未完成！");
			// TODO: 地址上传
//			new Thread(){
//				@Override
//				public void run(){
//					try{
//						String response = WebUtil.doGet("http://139.224.16.208/guide.json", null);
//						String url = "http://139.224.16.208/classAffairs/cpdaily/setLocation.php";
//						if(response != null){
//							JSONObject js = new JSONObject(response);
//							if(js.has("cpdailyLocation")) url = js.getString("cpdailyLocation");
//						}
//						response = WebUtil.doPost(url, null, "");
//
//						if(response != null){
//							String msg = new JSONObject(response).getString("msg");
//							runOnUiThread(() -> {
//								dialog.dismiss();
//								toast(msg);
//							});
//						}
//					}catch(IOException | JSONException e){
//						runOnUiThread(() -> {
//							dialog.dismiss();
//							LogUtil.Log(e);
//							toast("发生错误：" + e.getMessage());
//						});
//					}
//				}
//			}.start();
//			dialog.show();
		});
		
		initToolBar(null);
		initSliding(null,null);
		
	}
	
	private void loadCity(){
		try{
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(getAssets().open("city.json")));
			
			String line;
			StringBuilder sb = new StringBuilder();
			
			while((line = bufReader.readLine()) != null) sb.append(line);
			
			bufReader.close();
			
			JSONArray array = new JSONArray(sb.toString());
			
			provinceNode = new Node[array.length()];
			
			for(int i=0;i<provinceNode.length;i++){
				provinceNode[i] = new Node(array.getJSONObject(i));
			}
		}catch(IOException | JSONException e){
			e.printStackTrace();
		}
	}
	
	
	private String[] getPlaceName(Node[] nodes){
		if(nodes.length == 0) return new String[]{"请选择"};
		String[] result = new String[nodes.length];
		for(int i=0;i<result.length;i++) result[i] = nodes[i].name;
		return result;
	}
	
	private static class Node{
		
		 public String name;
		 
		 public Node[] child;
		 
		 public Node(JSONObject json) throws JSONException{
		 	name = json.getString("name");
		 	if(json.has("children")){
			    JSONArray array = json.getJSONArray("children");
			    child = new Node[array.length()];
			    for(int i=0;i<child.length;i++){
				    child[i] = new Node(array.getJSONObject(i));
			    }
		    }
		 }
	}
}
