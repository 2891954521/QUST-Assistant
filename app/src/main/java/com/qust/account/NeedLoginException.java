package com.qust.account;

/**
 * 需要登录
 */
public class NeedLoginException extends Exception{
	
	private static final long serialVersionUID = 0L;
	
	
	public NeedLoginException(){
		super("需要登录");
	}
}
