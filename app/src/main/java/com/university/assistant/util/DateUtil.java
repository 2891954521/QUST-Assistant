package com.university.assistant.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil{

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
    
    
//    public static String getDateString(Calendar c){
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(c.get(Calendar.YEAR));
//        sb.append("-");
//        sb.append(c.get(Calendar.MONTH)+1);
//        sb.append("-");
//        sb.append(c.get(Calendar.DATE));
//
////        sb.append(c.get(Calendar.DAY_OF_MONTH));
////        sb.append("日 星期");
////        int week = c.get(Calendar.DAY_OF_WEEK);
////        switch(week){
////            case 1:sb.append("日");break;
////            case 2:sb.append("一");break;
////            case 3:sb.append("二");break;
////            case 4:sb.append("三");break;
////            case 5:sb.append("四");break;
////            case 6:sb.append("五");break;
////            case 7:sb.append("六");break;
////            default:sb.append("?");
////        }
//
//        return  sb.toString();
//    }
    
    public static synchronized String getDateString(Calendar c){
        return FORMATTER.format(c.getTime());
    }
    
    public static Date getDateString(String s){
        try{
            return FORMATTER.parse(s);
        }catch(ParseException e){
            LogUtil.Log(e);
        }
        return new Date();
    }
}
