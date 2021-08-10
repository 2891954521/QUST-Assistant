package com.university.assistant.fragment.pictures;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class PictureData{
	
	public static File picturePath;
	
	public static File thumbnailPath;
	
	private static Bitmap emptyImage;
	
	private static PictureData pictureData;
	// 缓存
	private static LruCache<Integer,Bitmap> imageCache;
	
	private PictureDataHelper data;
	
	private PictureData(Context context){
		
		picturePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Note");
		if(!picturePath.exists())picturePath.mkdir();
		
		thumbnailPath = new File(context.getExternalCacheDir(),"Thumbnails");
		if(!thumbnailPath.exists())thumbnailPath.mkdir();
		
		emptyImage = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);
		
		// 获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
		int maxMemory = (int)Runtime.getRuntime().maxMemory();
		int mCacheSize = maxMemory / 8;
		// 给LruCache分配1/8 4M
		imageCache = new LruCache<Integer,Bitmap>(mCacheSize){
			// 必须重写此方法，来测量Bitmap的大小
			@Override
			protected int sizeOf(@NonNull Integer key,@NonNull Bitmap value){
				return value.getRowBytes() * value.getHeight();
			}
		};
		
		data = new PictureDataHelper(context);
	}
	
	public static void init(Context context){
		pictureData = new PictureData(context);
	}
	
	public static PictureData getInstance(){
		return pictureData;
	}
	
	public int addPicture(String path,String time){
		SQLiteDatabase write = data.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("file",path);
		values.put("date",time);
		int id = (int)write.insert("Picture","file",values);
		write.close();
		return id;
	}
	
	public ArrayList<String> getPicture(){
		ArrayList<String> p = new ArrayList<>();
		SQLiteDatabase db = data.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT `file` FROM `Picture` ORDER BY `date`", null);
		if(cursor.getCount() > 0){
			while(cursor.moveToNext()) p.add(cursor.getString(cursor.getColumnIndex("file")));
		}
		cursor.close();
		db.close();
		return p;
	}
	
	public Picture getPicture(int id){
		Picture p = new Picture();
		SQLiteDatabase db = data.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT id FROM `Picture` WHERE id = " + id + ";", null);
		if(cursor.getCount() > 0){
			cursor.moveToNext();
			p.id = cursor.getInt(cursor.getColumnIndex("id"));
			p.path = cursor.getString(cursor.getColumnIndex("file"));
			p.time = cursor.getString(cursor.getColumnIndex("date"));
		}
		cursor.close();
		db.close();
		return p;
	}
	
	
	
	public Bitmap createThumbnail(String path,File thumbnail){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		
		OutputStream outStream = null;
		try{
			outStream = new FileOutputStream(thumbnail);
			bitmap.compress(Bitmap.CompressFormat.PNG,100,outStream);
			outStream.flush();
		}catch(IOException e){
			LogUtil.Log(e);
		}finally{
			if(outStream!=null)try{ outStream.close(); }catch(IOException ignored){}
		}
		return bitmap;
	}
	
	@SuppressLint("StaticFieldLeak")
	public void getPicture(ImageView imageView,String path){
		new AsyncTask<String,Integer,Bitmap>(){
			@Override
			protected Bitmap doInBackground(String... strings){
				Bitmap bitmap;
				if(imageCache.get(path.hashCode())!=null){
					bitmap = imageCache.get(path.hashCode());
				}else{
					File f = new File(path);
					File thumbnail = new File(thumbnailPath,f.getName());
					// 存在缩略图
					if(thumbnail.exists()){
						bitmap =  BitmapFactory.decodeFile(f.toString());
						if(bitmap!=null) imageCache.put(path.hashCode(),bitmap);
					}else if(f.exists()){
						bitmap = createThumbnail(path,thumbnail);
						if(bitmap!=null) imageCache.put(path.hashCode(),bitmap);
					}else{
						return null;
					}
				}
				return bitmap;
				
			}
			
			@Override
			protected void onPostExecute(Bitmap bitmap){
				if(bitmap!=null){
					imageView.setImageBitmap(bitmap);
				}else{
					imageView.setImageBitmap(emptyImage);
				}
			}
		}.execute(path);
	}
	
	private static class PictureDataHelper extends SQLiteOpenHelper{
		
		public PictureDataHelper(Context context){
			super(context,"Database.db",null,1);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL(App.CREATE_NOTE_TABLE);
			db.execSQL(App.CREATE_PICTURE_TABLE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){ }
	}
}
