package com.university.assistant.fragment.pictures;

import java.io.Serializable;

public class Picture implements Serializable{
	
	public int id;
	
	public String path;
	
	public String time;
	
	public Picture(){ }
	
	public Picture(int id,String path,String time){
		this.id = id;
		this.path = path;
		this.time = time;
	}
}
