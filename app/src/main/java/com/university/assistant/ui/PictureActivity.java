package com.university.assistant.ui;

import android.os.Bundle;

import com.university.assistant.R;
import com.university.assistant.fragment.pictures.PictureData;
import com.university.assistant.widget.BigImage;
import com.university.assistant.widget.PictureGrid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PictureActivity extends AppCompatActivity{
	
	private BigImage bigImage;
	
	private PictureGrid pictureGrid;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		
		bigImage = findViewById(R.id.activity_picture_image);
		
		pictureGrid = findViewById(R.id.activity_picture_grid);
		
		pictureGrid.setPictures(PictureData.getInstance().getPicture());
		
		pictureGrid.setClickListener((imageView,path) -> {
			bigImage.setImage(imageView,path);
		});
	}
}
