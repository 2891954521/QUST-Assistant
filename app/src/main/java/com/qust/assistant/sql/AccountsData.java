package com.qust.assistant.sql;

import android.content.Context;

import com.qust.assistant.accounts.Account;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class AccountsData{
	
	private static AccountsData accountsData;
	
	public ArrayList<Account> accounts;
	
	public ArrayList<Integer> day;
	
	public ArrayList<String> name;
	
	private AccountsData(Context context){
		accounts = new ArrayList<>();
		day = new ArrayList<>();
		
		File file = new File(context.getFilesDir(),"Account/type.json");
		
		if(file.exists()){
			try{
				JSONArray array = new JSONArray(FileUtil.readFile(file));
				name = new ArrayList<>(array.length());
				for(int i=0;i<array.length();i++){
					name.add(array.getString(i));
				}
			}catch(JSONException e){
				LogUtil.Log(e);
			}
		}else{
			name = new ArrayList<>(4);
			//name.add();
		}
	}
	
	public static void init(Context context){
		synchronized(AccountsData.class){
			if(accountsData != null){
				accountsData = new AccountsData(context);
			}
		}
	}
	
	public static AccountsData getInstance(){
		return accountsData;
	}
	
	public void saveType(Context context){
		JSONArray array = new JSONArray();
		try{
			for(int i=0;i<name.size();i++){
				array.put(new JSONObject().put("name",name.get(i)));
			}
		}catch(JSONException e){
			LogUtil.Log(e);
		}
		FileUtil.writeFile(new File(context.getFilesDir(),"Account/type.json"), array.toString());
	}
}
