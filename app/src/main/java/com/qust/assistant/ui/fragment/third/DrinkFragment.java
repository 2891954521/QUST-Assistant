package com.qust.assistant.ui.fragment.third;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;

public class DrinkFragment extends BaseFragment{
	
	private WindowManager.LayoutParams layoutParams;
	
	private float screenBrightness;
	
	private SharedPreferences sp;
	
	private MaterialDialog dialog;
	
	private ImageView image;
	
	private EditText phone;
	
	private EditText password;
	
	private Handler handler;
	
	private String token;
	
	private String code;
	
	private boolean isRunning;
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		sp = activity.getSharedPreferences("drink", Context.MODE_PRIVATE);
		
		code = sp.getString("code", null);
		token = sp.getString("token", null);
		
		image = findViewById(R.id.fragment_drink_code);
		
		phone = findViewById(R.id.fragment_drink_phone);
		password = findViewById(R.id.fragment_drink_password);
		
		phone.setText(sp.getString("account", ""));
		password.setText(sp.getString("password", ""));
		
		dialog = DialogUtil.getIndeterminateProgressDialog(activity, "请稍候...").build();
		
		findViewById(R.id.fragment_drink_get_code).setOnClickListener(v -> {
			if(isRunning){
				toast("正在获取数据");
				return;
			}
			dialog.show();
			new Thread(){
				@Override
				public void run(){
					isRunning = true;
					try{
						if(token != null){
							JSONObject js = new JSONObject(WebUtil.doPost("https://dcxy-customer-app.dcrym.com/app/customer/login",
									null,
									"{\"loginTime\":" + System.currentTimeMillis() + "}",
									"clientsource", "{}", "token", token, "Content-Type", "application/json"));
							if(js.getInt("code") != 1000){
								token = null;
							}
						}
						
						if(token == null){
							String a = phone.getText().toString();
							String b = password.getText().toString();
							JSONObject js = new JSONObject(WebUtil.doPost("https://dcxy-customer-app.dcrym.com/app/customer/login",
									null,
									"{\"loginAccount\":\"" + a + "\",\"password\":\"" + b + "\"}",
									"clientsource", "{}", "Content-Type", "application/json"));
							if(js.getInt("code") == 1000){
								token = js.getJSONObject("data").getString("token");
								sp.edit().putString("token", token).putString("account", a).putString("password", b).apply();
							}else{
								handler.sendMessage(handler.obtainMessage(500, "登陆失败:" + js.getString("msg")));
								return;
							}
						}
						
						JSONObject js = new JSONObject(WebUtil.doGet(
								"https://dcxy-customer-app.dcrym.com/app/customer/flush/idbar",
								null,
								"clientsource", "{}", "token", token));
						
						String data = js.getString("data");
						code = data.substring(0, data.length() - 1) + "3";
						
						sp.edit().putString("code", code).apply();
						
						handler.sendMessage(handler.obtainMessage(200));
						
					}catch(JSONException | IOException e){
						handler.sendMessage(handler.obtainMessage(500, "获取饮水码失败:" + e.getMessage()));
					}
				}
			}.start();
		});
		
		handler = new Handler(activity.getMainLooper()){
			@Override
			public void handleMessage(@NonNull Message msg){
				if(msg.what == 200){
					createCode();
				}else{
					toast((String)msg.obj);
				}
				isRunning = false;
				dialog.dismiss();
			}
		};
		
		SeekBar seekBar = findViewById(R.id.fragment_drink_brightness);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
				setBrightness(progress);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar){ }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar){
				SettingUtil.setting.edit().putInt("drinkBrightness", seekBar.getProgress()).apply();
			}
		});
		
		layoutParams = activity.getWindow().getAttributes();
		screenBrightness = layoutParams.screenBrightness;
		
		int i = SettingUtil.setting.getInt("drinkBrightness", (int)screenBrightness);
		
		seekBar.setProgress(i);
		setBrightness(i);
		
		createCode();
		
	}
	
	private void createCode(){
		image.setImageBitmap(ParamUtil.createBarCode(code));
	}
	
	@Override
	public boolean onBackPressed(){
		layoutParams.screenBrightness = screenBrightness;
		activity.getWindow().setAttributes(layoutParams);
		return true;
	}
	
//	@Override
//	public void onResult(int requestCode, int resultCode, @Nullable Intent data){
//		if(requestCode == App.APP_REQUEST_CODE && resultCode == Activity.RESULT_OK){
//			String filePath = null;
//			Uri uri = data.getData();
//			if(DocumentsContract.isDocumentUri(activity, uri)){
//				// 如果是document类型的 uri, 则通过document id来进行处理
//				String documentId = DocumentsContract.getDocumentId(uri);
//				if("com.android.providers.media.documents".equals(uri.getAuthority())){
//					// MediaProvider
//					// 使用':'分割
//					String id = documentId.split(":")[1];
//					String selection = MediaStore.Images.Media._ID + "=?";
//					String[] selectionArgs = {id};
//					filePath = getDataColumn(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
//				}else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
//					// DownloadsProvider
//					Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId));
//					filePath = getDataColumn(contentUri, null, null);
//				}
//			}else if("content".equalsIgnoreCase(uri.getScheme())){
//				// 如果是 content 类型的 Uri
//				filePath = getDataColumn(uri, null, null);
//			}else if("file".equals(uri.getScheme())){
//				// 如果是 file 类型的 Uri,直接获取图片对应的路径
//				filePath = uri.getPath();
//			}
//
//			try{
//				FileUtil.copyFile(new FileInputStream(filePath), file);
//			}catch(FileNotFoundException ignored){ }
//
//			((ImageView)findViewById(R.id.fragment_drink_code)).setImageBitmap(BitmapFactory.decodeFile(filePath));
//		}
//	}
//
//	private void choosePicture(){
//		Intent intent = new Intent("android.intent.action.GET_CONTENT");
//		Uri imageUri;
//		if(android.os.Build.VERSION.SDK_INT > 24){
//			intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//			imageUri = FileProvider.getUriForFile(activity, "com.qust.assistant", file);
//		}else{
//			imageUri = Uri.fromFile(file);
//		}
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//		intent.putExtra("crop", true);
//		intent.setType("image/*");
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//		activity.startActivityForResult(intent, App.APP_REQUEST_CODE);
//	}
//
//	private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
//		String path = null;
//
//		String[] projection = new String[]{MediaStore.Images.Media.DATA};
//		Cursor cursor = null;
//		try {
//			cursor = activity.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//			if (cursor != null && cursor.moveToFirst()) {
//				int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
//				path = cursor.getString(columnIndex);
//			}
//		} catch (Exception e) {
//			if (cursor != null) {
//				cursor.close();
//			}
//		}
//		return path;
//	}
	
	private void setBrightness(int paramInt){
		layoutParams.screenBrightness = paramInt / 255.0f;
		activity.getWindow().setAttributes(layoutParams);
	}
	
	@Override
	protected int getLayout(){ return R.layout.fragment_drink; }
	
	@Override
	public String getName(){
		return "饮水码";
	}
	
}