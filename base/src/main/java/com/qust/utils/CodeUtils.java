package com.qust.utils;

import androidx.annotation.NonNull;

public class CodeUtils{
	
	@NonNull
	public static String byteToHexString(@NonNull byte[] byteArray){
		StringBuilder hexString = new StringBuilder();
		for(byte b : byteArray){
			String hex = Integer.toHexString(0xFF & b);
			if(hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	
	public interface Callback{
		void callback();
	}
}
