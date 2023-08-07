package com.qust.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class FileUtils{
	
	
	/**
	 * 从文件读取序列化后的数据
	 * @param file  文件
	 * @return      反序列化后的数据
	 */
	public static Object loadData(File file) throws IOException, ClassNotFoundException{
		if(file.exists()){
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
				try(ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(file.toPath()))){
					return stream.readObject();
				}
			}else{
				try(ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))){
					return stream.readObject();
				}
			}
		}else throw new FileNotFoundException();
	}
	
	/**
	 * 将数据序列化存储到文件
	 * @param file  文件
	 * @param o     数据对象
	 */
	public static void saveData(File file, Object o) throws IOException{
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
			try(ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(file.toPath()))){
				stream.writeObject(o);
				stream.flush();
			}
		}else{
			try(ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))){
				stream.writeObject(o);
				stream.flush();
			}
		}
	}
	
}
