package com.university.assistant.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.university.assistant.R;
import com.university.assistant.fragment.pictures.PictureData;
import com.university.assistant.util.LogUtil;

import java.util.ArrayList;

public class PictureGrid extends GridView{
	
	private int size;
	
	private PictureAdapter adapter;
	
	private ArrayList<String> pictures;
	
	private OnImageClickListener listener;
	
	private OnImageLongClickListener longListener;
	
	public PictureGrid(Context context){
		this(context,null);
	}
	
	public PictureGrid(Context context,AttributeSet attr){
		this(context,attr,0);
	}
	
	public PictureGrid(Context context,AttributeSet attr,int defStyle){
		super(context,attr,defStyle);
		pictures = new ArrayList<>();
		adapter = new PictureAdapter();
		size = (int)(getResources().getDisplayMetrics().widthPixels/3.2f);
		setAdapter(adapter);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev){
		return false;
	}
	
	public void setPictures(ArrayList<String> pictures){
		this.pictures = pictures;
		adapter.notifyDataSetChanged();
	}
	
	private class PictureAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){
			return pictures.size();
		}
		
		@Override
		public Object getItem(int position){
			return null;
		}
		
		@Override
		public long getItemId(int position){
			return 0;
		}
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			ImageView image = (ImageView)convertView;
			if(image==null){
				image = (ImageView)LayoutInflater.from(getContext()).inflate(R.layout.view_image,null);
				image.setLayoutParams(new GridView.LayoutParams(size,size));
			}
			PictureData.getInstance().getPicture(image,pictures.get(position));
			final ImageView  im = image;
			image.setOnClickListener(v -> {
				if(listener!=null)listener.onImageClick(im,pictures.get(position));
			});
			image.setOnLongClickListener(v -> {
				if(longListener!=null)longListener.onImageLongClick(position);
				return true;
			});
			return image;
		}
	}
	
	public void setClickListener(OnImageClickListener _listener){
		listener = _listener;
	}
	
	public void setLongClickListener(OnImageLongClickListener _listener){
		longListener = _listener;
	}
	
	public interface OnImageClickListener{
		void onImageClick(ImageView imageView,String path);
	}
	
	public interface OnImageLongClickListener{
		void onImageLongClick(int index);
	}
}

