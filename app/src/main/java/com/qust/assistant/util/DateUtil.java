package com.qust.assistant.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil{

    public static final SimpleDateFormat YMD_HM = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
    
    public static final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
    
    public static final SimpleDateFormat MD = new SimpleDateFormat("MM-dd",Locale.CHINA);
    
    public static final SimpleDateFormat HM = new SimpleDateFormat("HH:mm",Locale.CHINA);
    
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
        return YMD_HM.format(c.getTime());
    }
    
    public static Date getDateString(String s){
        try{
            return YMD_HM.parse(s);
        }catch(ParseException e){
            LogUtil.Log(e);
        }
        return new Date();
    }
    
    /**
     * 计算时间差
     */
    public static String timeDifference(String s, String e){
        try{
            Date fromDate = YMD_HM.parse(s);
            Date toDate = YMD_HM.parse(e);
            long from = fromDate.getTime();
            long to = toDate.getTime();
            int hours = (int)((to - from) / (1000 * 60 * 60));
            int minutes = (int) ((to - from) / (1000 * 60)) % 60;
            if(hours != 0){
                if(minutes == 0){
                    return hours + "小时";
                }else{
                    return hours + "小时" + minutes + "分钟";
                }
            }else{
                return minutes + "分钟";
            }
        }catch(ParseException ignored){ }
        return "";
    }
    
    public static int calcDayOffset(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) {  //同一年
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {  //闰年
                    timeDistance += 366;
                } else {  //不是闰年
                    
                    timeDistance += 365;
                }
            }
            return timeDistance + (day2 - day1);
        } else { //不同年
            return day2 - day1;
        }
    }
    
    public static int calcWeekOffset(Date startTime, Date endTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        dayOfWeek = dayOfWeek - 1;
        if (dayOfWeek == 0) dayOfWeek = 7;
        
        int dayOffset = calcDayOffset(startTime, endTime);
        
        int weekOffset = dayOffset / 7;
        int a;
        if (dayOffset > 0) {
            a = (dayOffset % 7 + dayOfWeek > 7) ? 1 : 0;
        } else {
            a = (dayOfWeek + dayOffset % 7 < 1) ? -1 : 0;
        }
        weekOffset = weekOffset + a;
        return weekOffset;
    }

}
