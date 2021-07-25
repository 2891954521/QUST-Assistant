package com.university.assistant.ui.third;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

public class DrinkActivity extends BaseAnimActivity{
	
	private File file;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drink);
		
		findViewById(R.id.activity_drink_set_image).setOnClickListener(v -> choosePicture());
		
		findViewById(R.id.activity_drink_help).setOnClickListener(v -> {
			new MaterialDialog.Builder(this)
				.title("帮助")
				.content("由于多彩校园的饮水码不刷新可以无限重复使用，所以可以截一张图，点击右上角设置按钮设置图片，加快打水速度，并减少看到垃圾多彩校园的广告")
				.positiveText("确定")
				.onPositive((dialog, which) -> dialog.dismiss())
				.show();
		});
		
		file = new File(getExternalFilesDir("Drink"),"code");
		if(file.exists()){
			((ImageView)findViewById(R.id.activity_drink_code)).setImageBitmap(BitmapFactory.decodeFile(file.toString()));
		}
		
		initToolBar(null);
		initSliding(null, null);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		if(requestCode==App.APP_REQUEST_CODE){
			if(resultCode==RESULT_OK){
				String filePath = null;
				Uri uri = data.getData();
				if(DocumentsContract.isDocumentUri(DrinkActivity.this,uri)){
					// 如果是document类型的 uri, 则通过document id来进行处理
					String documentId = DocumentsContract.getDocumentId(uri);
					if("com.android.providers.media.documents".equals(uri.getAuthority())){ // MediaProvider
						// 使用':'分割
						String id = documentId.split(":")[1];
						String selection = MediaStore.Images.Media._ID + "=?";
						String[] selectionArgs = {id};
						filePath = getDataColumn(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection,selectionArgs);
					}else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){ // DownloadsProvider
						Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.parseLong(documentId));
						filePath = getDataColumn(contentUri,null,null);
					}
				}else if("content".equalsIgnoreCase(uri.getScheme())){
					// 如果是 content 类型的 Uri
					filePath = getDataColumn(uri,null,null);
				}else if("file".equals(uri.getScheme())){
					// 如果是 file 类型的 Uri,直接获取图片对应的路径
					filePath = uri.getPath();
				}
				try{
					FileUtil.copyFile(new FileInputStream(filePath),file);
				}catch(FileNotFoundException ignored){ }
				
				((ImageView)findViewById(R.id.activity_drink_code)).setImageBitmap(BitmapFactory.decodeFile(filePath));
			}
		}
	}
	
	private void choosePicture(){
		Intent intent = new Intent("android.intent.action.GET_CONTENT");
		Uri imageUri;
		if(android.os.Build.VERSION.SDK_INT > 24){
			intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			imageUri = FileProvider.getUriForFile(this, "com.university.assistant", file);
		}else{
			imageUri = Uri.fromFile(file);
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra("crop", true);
		intent.setType("image/*");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent,App.APP_REQUEST_CODE);
	}
	
	private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
		String path = null;
		
		String[] projection = new String[]{MediaStore.Images.Media.DATA};
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
				path = cursor.getString(columnIndex);
			}
		} catch (Exception e) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return path;
	}
	
	
}
