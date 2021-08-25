package com.university.assistant.ui;

import android.os.Bundle;

import com.university.assistant.R;
import com.university.assistant.sql.PictureData;
import com.university.assistant.widget.BigImage;
import com.university.assistant.widget.PictureGrid;

import androidx.annotation.Nullable;

public class PictureActivity extends BaseAnimActivity{
	
	private BigImage bigImage;
	
	private PictureGrid pictureGrid;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		bigImage = findViewById(R.id.activity_picture_image);
		
		pictureGrid = findViewById(R.id.activity_picture_grid);
		
		pictureGrid.setPictures(PictureData.getInstance().getPicture());
		
		pictureGrid.setClickListener((imageView,path) -> {
			bigImage.setImage(imageView,path);
		});
		
		initToolBar(null);
		initSliding(null, null);
	}
}
