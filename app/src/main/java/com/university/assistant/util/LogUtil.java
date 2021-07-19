package com.university.assistant.util;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.university.assistant.App;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil{
    
    // Log路径
    public static String DebugLogFile;

    public static boolean DEBUG;
    
    private static LogUtil logUtil;
    
    private Context context;
    
    public static void init(Context context){
        File f = context.getExternalFilesDir("debug");
        if(f==null)DebugLogFile = new File(context.getExternalFilesDir("log"),"debug.log").toString();
        else{
            if(!f.exists())f.mkdirs();
            DebugLogFile = f.toString();
        }
    }

    // 调试模式下才会进行输出
    public static void Log(String s){
        if(DEBUG)debugLog(s);
    }

    // 输出log
    public static void Log(String s,Throwable e){
        debugLog(s);
        Log(e);
    }

    // 输出到debug文件
    public static void debugLog(String string){
        FileUtil.appendFile(new File(DebugLogFile,"debug.log"),string+"\n");
    }

    // log输出异常
    public static void Log(Throwable e){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String str = stringWriter.toString();
        File f = new File(DebugLogFile,Integer.toString(str.hashCode())+".log");
        if(f.exists())debugLog(SimpleDateFormat.getInstance().format(new Date(System.currentTimeMillis()))+" 发生异常:"+str.hashCode());
        else{
            FileUtil.writeFile(f.toString(),str);
            debugLog(SimpleDateFormat.getInstance().format(new Date(System.currentTimeMillis()))+" 发生异常:"+str.hashCode()+"\n"+e.getClass().getName()+" : "+e.getMessage());
        }
    }
}
