package com.qust.assistant.vo;


import android.os.Handler;

public class QustLoginResult{
	
	public Handler from;
	
	public String message;
	
	public String cookie;
	
	public QustLoginResult(Handler from, String message){
		this.from = from;
		this.message = message;
	}
	
	public QustLoginResult(Handler from, String message, String cookie){
		this.from = from;
		this.message = message;
		this.cookie = cookie;
	}
}
