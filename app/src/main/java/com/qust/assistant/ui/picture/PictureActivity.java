package com.qust.assistant.ui.picture;

import android.os.Bundle;

import com.qust.assistant.R;
import com.qust.assistant.sql.PictureData;
import com.qust.assistant.ui.BaseAnimActivity;
import com.qust.assistant.widget.BigImage;
import com.qust.assistant.widget.PictureGrid;

import androidx.annotation.Nullable;

public class PictureActivity extends BaseAnimActivity{
	
	private BigImage bigImage;
	
	private PictureGrid pictureGrid;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		PictureData.init(this);
		
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
