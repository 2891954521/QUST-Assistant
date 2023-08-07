package com.qust.account;

import androidx.annotation.NonNull;

import com.qust.assistant.util.SettingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 自动持久化处理Cookie
 */
public class AccountCookieJar implements CookieJar{
	
	/**
	 * SharedPreferences存储时的字段名
	 */
	private String name;
	
	/**
	 * 储存Cookie
	 */
	private HashMap<String, Cookie> cookieStore;
	
	private ArrayList<Cookie> cookiesList;
	
	/**
	 *
	 * @param host HOST
	 * @param scheme 协议类型：http / https
	 * @param name SharedPreferences存储时的字段名
	 */
	public AccountCookieJar(String host, String scheme, String name){
		this.name = name;
		
		HttpUrl url = new HttpUrl.Builder().host(host).scheme(scheme).build();
		
		cookiesList = new ArrayList<>();
		for(String cookieString : SettingUtil.getStringSet(name, new HashSet<>())){
			Cookie cookie = Cookie.parse(url, cookieString);
			if(cookie != null) cookiesList.add(cookie);
		}

		cookieStore = new HashMap<>(cookiesList.size());
		for(Cookie newCookie : cookiesList){
			cookieStore.put(newCookie.name(), newCookie);
		}
	}
	
	/**
	 * 清空所有Cookie
	 */
	public void clearCookies(){
		cookieStore.clear();
		cookiesList.clear();
	}
	
	@Override
	public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies){
		for(Cookie newCookie : cookies){
			cookieStore.put(newCookie.name(), newCookie);
		}
		cookiesList = new ArrayList<>(cookieStore.values());
		
		Set<String> encodedCookies = new HashSet<>();
		for(Cookie cookie : cookies) encodedCookies.add(cookie.toString());
		SettingUtil.edit().putStringSet(name, encodedCookies).apply();
	}
	
	@NonNull
	@Override
	public List<Cookie> loadForRequest(@NonNull HttpUrl url){
		return cookiesList;
	}
	
}