package com.qust.assistant.ui.third.fake;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class LeaveData{
	
	private static LeaveData leaveData;
	
	public ArrayList<Data> data;
	
	public ArrayList<String> files;
	
	private LeaveData(Context context){
		data = new ArrayList<>();
		files = new ArrayList<>();
		
		File file = context.getExternalFilesDir("Leave");
		
		if(file == null) return;
		
		if(file.exists()){
			File[] leaves = file.listFiles();
			if(leaves == null) return;
			for(File o : leaves){
				try(ObjectInputStream stream = new ObjectInputStream(new FileInputStream(o))){
					data.add((Data)stream.readObject());
					files.add(o.toString());
				}catch(Exception ignored){ }
			}
		}else{
			file.mkdir();
		}

	}
	
	public void delete(int position){
		if(new File(files.get(position)).delete()){
			files.remove(position);
			data.remove(position);
		}
	}
	
	public static void init(Context context){
		if(leaveData == null){
			synchronized(LeaveData.class){
				leaveData = new LeaveData(context);
			}
		}
	}
	
	public static LeaveData getInstance(){
		return leaveData;
	}
	
	
	public static class Data implements Serializable{
		
		public boolean isFinish;
		
		public String type;
		
		public String start;
		
		public String end;
		
		public boolean needLeave;
		
		public String phone;
		
		public String destination;
		
		public String destinationInfo;
		
		public String reason;
		
		public String teacher;
		
		public String teacherOpinion;
		
		public String gps;
		
		public String sTime;
		
		public String pTime;
		
	}
}
