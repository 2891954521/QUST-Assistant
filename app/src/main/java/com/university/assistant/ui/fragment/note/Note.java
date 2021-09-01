package com.university.assistant.ui.fragment.note;

import android.database.Cursor;

import com.university.assistant.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable{

    public int id;
    
    public boolean isFinish;
    
    public String title;
    
    public String text;
    
    public String date;
    
    public String deadline;
    
    public ArrayList<String> pictures;
    
    public Item[] items;

    public Note(){
        id = -1;
        title = "";
        text = "";
        date = "";
        deadline = "";
        pictures = new ArrayList<>();
        items = new Item[0];
    }

    public Note(Cursor cursor){
        
        id = cursor.getInt(cursor.getColumnIndex("id"));
        isFinish = cursor.getShort(cursor.getColumnIndex("isFinish")) == 1;
        title = cursor.getString(cursor.getColumnIndex("title"));
        text = cursor.getString(cursor.getColumnIndex("text"));
        date = cursor.getString(cursor.getColumnIndex("date"));
        deadline = cursor.getString(cursor.getColumnIndex("deadline"));
    
        pictures = new ArrayList<>();
    
        try{
            JSONArray json = new JSONArray(cursor.getString(cursor.getColumnIndex("pictures")));
            for(int i=0;i<json.length();i++){
                pictures.add(json.getString(i));
            }
        }catch(JSONException e){
            LogUtil.Log(e);
            pictures = new ArrayList<>();
        }
    
        JSONArray js = null;
        try{
            js = new JSONArray(cursor.getString(cursor.getColumnIndex("items")));
        }catch(JSONException e){
            LogUtil.Log(e);
        }
        if(js!=null){
            items = new Item[js.length() / 2];
            for(int i = 0;i<items.length;i++){
                Item item = new Item();
                try{
                    item.isFinish = js.getBoolean(2 * i);
                    item.text = js.getString(2 * i + 1);
                }catch(JSONException e){
                    LogUtil.Log(e);
                    item.isFinish = false;
                    item.text = "读取数据失败";
                }
                items[i] = item;
            }
        }
        
    }
    
    public JSONArray items2Json(){
        JSONArray data = new JSONArray();
        for(Item item : items){
            data.put(item.isFinish);
            data.put(item.text);
        }
        return data;
    }
    
    public JSONArray picture2Json(){
        JSONArray data = new JSONArray();
        for(String pic : pictures){
            data.put(pic);
        }
        return data;
    }
    
    public JSONObject toJson(){
        JSONObject js = new JSONObject();
        JSONArray data = new JSONArray();
        try{
            js.put("title",title);
            js.put("text",text);
            js.put("date",date);
            js.put("deadline",deadline);
            js.put("pictures",pictures);
            for(Item item : items){
                data.put(item.isFinish);
                data.put(item.text);
            }
        }catch(JSONException e){
            LogUtil.Log(e);
        }finally{
            try{
                js.put("data",data);
            }catch(JSONException e){
                LogUtil.Log(e);
            }
        }
        return js;
    }

    public static class Item implements Serializable{
        
        public boolean isFinish;
        
        public String text;

        public Item(){ }

        public Item(boolean _isFinish,String _text){
            isFinish = _isFinish;
            text = _text;
        }

    }

}


