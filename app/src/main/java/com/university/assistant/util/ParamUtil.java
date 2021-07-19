package com.university.assistant.util;

import java.util.regex.Pattern;

public class ParamUtil{
	
	private static final Pattern FLOAT_PATTERN = Pattern.compile("[0-9\\\\.]*");
	
	public static boolean isFloat(String str){
		return FLOAT_PATTERN.matcher(str).matches();
	}
}
