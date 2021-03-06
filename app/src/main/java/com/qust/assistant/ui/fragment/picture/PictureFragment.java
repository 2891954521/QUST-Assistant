package com.qust.assistant.ui.fragment.picture;

import android.view.LayoutInflater;

import com.qust.assistant.R;
import com.qust.assistant.sql.PictureData;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.widget.BigImage;
import com.qust.assistant.widget.PictureGrid;

public class PictureFragment extends BaseFragment{
	
	private BigImage bigImage;
	
	private PictureGrid pictureGrid;
	
	public PictureFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		PictureData.init(activity);
		
		bigImage = findViewById(R.id.activity_picture_image);
		
		pictureGrid = findViewById(R.id.activity_picture_grid);
		
		pictureGrid.setPictures(PictureData.getInstance().getPicture());
		
		pictureGrid.setClickListener((imageView,path) -> {
			bigImage.setImage(imageView,path);
		});
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_picture;
	}
	
	@Override
	public String getName(){
		return "相册";
	}
}
