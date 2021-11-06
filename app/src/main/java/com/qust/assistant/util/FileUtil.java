package com.qust.assistant.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;

public class FileUtil{
    
    private static final char[] HEX_CODE = new char[]{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    
    public static void writeFile(String f,String string){
        writeFile(new File(f),string);
    }

    public static void writeFile(File f,String string){
        try{
            FileWriter w = new FileWriter(f);
            w.write(string);
            w.flush();
            w.close();
        }catch(IOException ignored){ }
    }
    
    @NonNull
    public static String readFile(String str){
        return readFile(new File(str));
    }

    @NonNull
    public static String readFile(File f){
        if(!f.exists())return "";
        StringBuilder sb = new StringBuilder();
        try{
            FileReader b = new FileReader(f);
            char[] c = new char[1024];
            int len = 0;
            while((len = b.read(c))!=-1)sb.append(c,0,len);
        }catch(IOException ignored){ }
        return sb.toString();
    }

    public static void appendFile(File file,String s){
        RandomAccessFile raf = null;
        try{
            if(!file.exists()) file.createNewFile();
            raf = new RandomAccessFile(file,"rw");
            raf.seek(raf.length());
            raf.write(s.getBytes());
        }catch(IOException ignored){

        }finally{
            try{
                if(raf!=null) raf.close();
            }catch(IOException ignored){

            }
        }
    }

    public static void copyFile(InputStream in,String targetPath){
        copyFile(in,new File(targetPath));
    }
    
    public static void copyFile(InputStream in,File targetPath){
        try{
            FileOutputStream fos = new FileOutputStream(targetPath);
            byte[] buffer = new byte[1024];
            int byteCount;
            while((byteCount = in.read(buffer))!=-1){
                fos.write(buffer,0,byteCount);
            }
            fos.flush();
            in.close();
            fos.close();
        }catch(Exception ignore){ }
    }
    
    /**
     * 从assets目录下拷贝文件
     */
    public static void copyFileFromAssets(Context context,String assetsFilePath,String targetFileFullPath){
        InputStream assestsFileInputStream;
        try{
            assestsFileInputStream = context.getAssets().open(assetsFilePath);
            copyFile(assestsFileInputStream,targetFileFullPath);
        }catch(IOException e){
            LogUtil.Log(e);
        }
    }
    
    // 获取不重命文件名
    public static File getOnlyFileName(String file,String suffix){
        File f = new File(file + suffix);
        if(!f.exists()) return f;
        int count = 1;
        while(true){
            f = new File(file + "(" + count + ")" + suffix);
            if(!f.exists()) return f;
            else count++;
        }
    }
    
    // 获取后缀名
    @NonNull
    public static String getFileExt(File file){
        return getFileExt(file.getName());
    }
    //
    @NonNull
    public static String getFileExt(String fileName){
        int pos = fileName.lastIndexOf(".");
        if(pos==-1) return "";
        return fileName.substring(pos + 1).toLowerCase();
    }
    
    // 移除后缀名
    public static String removeExt(String s){
        int index = s.lastIndexOf(".");
        if(index==-1) return s;
        else return s.substring(0,index);
    }
    
    // 计算文件的大小
    public static String getFileSize(long fileS){
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if(fileS<1024){
            fileSizeString = df.format((double)fileS) + "B";
        }else if(fileS<1048576){
            fileSizeString = df.format((double)fileS / 1024) + "K";
        }else if(fileS<1073741824){
            fileSizeString = df.format((double)fileS / 1048576) + "M";
        }else{
            fileSizeString = df.format((double)fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
    
    /**
     * 获取一个文件的md5值(可处理大文件)
     */
    public static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            byte[] data = md5.digest();
            StringBuilder r = new StringBuilder(data.length * 2);
            for (byte b : data) {
                r.append(HEX_CODE[(b >> 4) & 0xF]);
                r.append(HEX_CODE[(b & 0xF)]);
            }
            return r.toString();
        } catch (Exception e) {
            LogUtil.Log(e);
            return "";
        } finally {
            try {
                if (fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
