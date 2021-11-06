package com.qust.assistant.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper{
	
	public static final String CREATE_NOTE_TABLE = "CREATE TABLE `Note` (" +
					"`id` INTEGER primary key autoincrement," +
					"`isFinish` BOOLEAN,"       + // 是否超过截止日期
					"`title` TINYTEXT,"         + // 最长255
					"`text` TEXT,"              + // 最长65535
					"`items` TEXT,"             +
					"`date` DATETIME,"          +
					"`deadline` DATETIME,"      +
					"`pictures` TEXT"           +
					");";
	
	public static final String CREATE_PICTURE_TABLE = "CREATE TABLE `Picture` (" +
					"`id` INTEGER primary key autoincrement," +
					"`file` TEXT,"              +
					"`date` DATETIME"           +
					");";
	
	public static final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE `Accounts` (" +
					"`id` INTEGER primary key autoincrement," +
					"`type` INTEGER,"           +
					"`value` INTEGER,"          +
					"`description` TINYTEXT,"   +
					"`date` DATETIME"           +
					");";
	
	private static DataBase dataBase;
	
	private DataBase(Context context){
		super(context,"Database.db",null,2);
	}
	
	public static void init(Context context){
		synchronized(DataBase.class){
			if(dataBase == null){
				dataBase = new DataBase(context);
			}
		}
	}
	
	public static DataBase getInstance(){
		return dataBase;
	}
	
	// 第一次创建数据库时调用
	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_NOTE_TABLE);
		db.execSQL(CREATE_PICTURE_TABLE);
		db.execSQL(CREATE_ACCOUNTS_TABLE);
	}
	
	// 版本更新的时候调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		switch(oldVersion){
			case 1:
				db.execSQL(CREATE_ACCOUNTS_TABLE);
				break;
		}
	}
	
}