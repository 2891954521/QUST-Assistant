package com.qust.assistant.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil{
	
	public static String inputStream2string(InputStream inputStream){
		try{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while((len = inputStream.read(buffer))!=-1) outStream.write(buffer,0,len);
			inputStream.close();
			return outStream.toString();
		}catch(IOException e){
			return "";
		}
	}
	
	public static String doGet(String url, String cookie, String... params) throws IOException {
		HttpURLConnection connection = get(url, cookie, params);
		if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
			return inputStream2string(connection.getInputStream());
		}else return "";
	}
	
	public static HttpURLConnection get(String url, String cookie, String... params) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		
		connection.setInstanceFollowRedirects(false);
		
		connection.setRequestMethod("GET");
		
		if(cookie != null) connection.setRequestProperty("Cookie", cookie);
		
		if(params.length != 0){
			for(int i=0;i<params.length;i+=2){
				connection.setRequestProperty(params[i], params[i+1]);
			}
		}

		connection.connect();
		
		return connection;
	}
	
	public static String doPost(String url, String cookie, String data, String... params) throws IOException {
		HttpURLConnection connection = post(url, cookie, data, params);
		if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
			return inputStream2string(connection.getInputStream());
		}else return "";
	}
	
	public static HttpURLConnection post(String url, String cookie, String data, String... params) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		
		connection.setRequestMethod("POST");
		
		if(cookie != null) connection.setRequestProperty("Cookie", cookie);
		
		if(params.length != 0){
			for(int i=0;i<params.length;i+=2){
				connection.setRequestProperty(params[i], params[i+1]);
			}
		}
		
		connection.getOutputStream().write(data.getBytes());
		
		connection.connect();
		
		return connection;
	}
	
	
}
