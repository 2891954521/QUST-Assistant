package com.qust.assistant.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;
import com.qust.assistant.sql.PictureData;
import com.qust.assistant.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraActivity extends BaseAnimActivity{

	
	// 如果勾选了不再询问
	private static final int NOT_NOTICE = 2;
	
	private Camera camera;
	
	private SurfaceView surfaceView;
	
	private SurfaceHolder surfaceHolder;
	
	private ImageView shoot,cancel,done;
	
	private byte[] data;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		shoot = findViewById(R.id.activity_camera_shoot);
		shoot.setOnClickListener(v->{
			//拍摄照片
			try{
				camera.takePicture(null,null,(data,camera) -> this.data = data);
				shoot.setVisibility(View.INVISIBLE);
				cancel.setVisibility(View.VISIBLE);
				done.setVisibility(View.VISIBLE);
			}catch(RuntimeException e){
				toast("拍照失败！");
			}
			// camera.startPreview();
		});
		
		cancel = findViewById(R.id.activity_camera_cancel);
		cancel.setOnClickListener(v->{
			camera.startPreview();
			shoot.setVisibility(View.VISIBLE);
			cancel.setVisibility(View.INVISIBLE);
			done.setVisibility(View.INVISIBLE);
		});
		
		done = findViewById(R.id.activity_camera_done);
		done.setOnClickListener(v->{
			try{
				//String time = DateUtil.getDateString(Calendar.getInstance(Locale.CHINA));
				String name =  "Note_" + System.currentTimeMillis() + ".jpg";
				File f = new File(PictureData.picturePath, name);
				FileOutputStream fileOut = new FileOutputStream(f);
				fileOut.write(data,0,data.length);
				fileOut.flush();
				fileOut.close();
				toast("保存成功！");
			}catch(IOException e){
				LogUtil.Log(e);
				toast("保存失败！");
			}
		});
		
		surfaceView = findViewById(R.id.activity_camera_surface);
		surfaceView.setFocusable(true);
		surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
		surfaceHolder = surfaceView.getHolder();
		// 下面设置surfaceView不维护自己的缓冲区,而是等待屏幕的渲染引擎将内容推送到用户面前
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// surfaceView.getHolder().setFixedSize(800, 480);
		surfaceHolder.addCallback(new SurfaceCallback());
		
		// 检查权限，没有就申请
		if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
		}
	}
	
	//相机参数的初始化设置
	private void initCamera(){
		// 得到摄像头的参数
		Camera.Parameters parameters = camera.getParameters();
		// 设置每秒3帧
		//parameters.setPreviewFrameRate(3);
		int PreviewWidth = 0;
		int PreviewHeight = 0;
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);//获取窗口的管理器
		Display display = wm.getDefaultDisplay();//获得窗口里面的屏幕
		// 选择合适的预览尺寸
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		
		// 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
		if (sizeList.size() > 1) {
			Iterator<Camera.Size> itor = sizeList.iterator();
			while (itor.hasNext()) {
				Camera.Size cur = itor.next();
				if (cur.width >= PreviewWidth
						&& cur.height >= PreviewHeight) {
					PreviewWidth = cur.width;
					PreviewHeight = cur.height;
					break;
				}
			}
		}
		parameters.setPreviewSize(PreviewWidth, PreviewHeight); //获得摄像区域的大小
		//parameters.setPreviewSize(1024, 1024);
		// parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight()); // 部分定制手机，无法正常识别该方法。
		parameters.setJpegQuality(100);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		// 1连续对焦
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		
		camera.setParameters(parameters);
		// 2如果要实现连续的自动对焦，这一句必须加上
		camera.cancelAutoFocus();
		// 控制图像的正确显示方向
		// 实现的图像的正确显示
		Method downPolymorphic;
		try{
			downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",int.class);
			downPolymorphic.invoke(camera,90);
		}catch(Exception e){
			LogUtil.Log(e);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
		if(requestCode==1){
			for(int i = 0;i<permissions.length;i++){
				if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
					// 选择了“始终允许”
					Toast.makeText(this,"权限" + permissions[i] + "申请成功",Toast.LENGTH_SHORT).show();
				}else{
					// 用户选择了禁止不再询问
					if(!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){
						new MaterialDialog.Builder(this).title("权限").content("请允许应用访问相机以进行拍照！")
								.positiveText("取消").onPositive((dialog,which) -> {
							Toast.makeText(this,"没有相机权限，无法进行拍照",Toast.LENGTH_SHORT).show();
							finish();
						}).negativeText("确定").onNegative((dialog,which) -> {
							Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							Uri uri = Uri.fromParts("package",getPackageName(),null);//注意就是"package",不用改成自己的包名
							intent.setData(uri);
							startActivityForResult(intent,NOT_NOTICE);
							dialog.dismiss();
						}).show();
					}else{
						// 选择禁止
						new MaterialDialog.Builder(this).title("权限").content("请允许应用访问相机以进行拍照！")
								.positiveText("取消").onPositive((dialog,which) -> {
							Toast.makeText(this,"没有相机权限，无法进行拍照",Toast.LENGTH_SHORT).show();
							finish();
						}).negativeText("确定").onNegative((dialog,which) -> {
							ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
							dialog.dismiss();
						}).show();
					}
				}
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		if(requestCode==NOT_NOTICE){
			// 由于不知道是否选择了允许所以需要再次判断
			if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
				ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
			}
		}
	}
	
	private class SurfaceCallback implements SurfaceHolder.Callback{
		
		@Override
		public void surfaceCreated(@NonNull SurfaceHolder holder){
			if(camera==null){
				camera = Camera.open();
				try{
					camera.setPreviewDisplay(surfaceHolder);
					initCamera();
					camera.startPreview();
				}catch(IOException e){
					LogUtil.Log(e);
				}
			}
		}
		
		@Override
		public void surfaceChanged(@NonNull SurfaceHolder holder,int format,int width,int height){
			//实现自动对焦
			camera.autoFocus((success,camera) -> {
				if(success){
					// 实现相机的参数初始化
					initCamera();
					// 只有加上了这一句，才会自动对焦。
					camera.cancelAutoFocus();
				}
			});
		}
		
		@Override
		public void surfaceDestroyed(@NonNull SurfaceHolder holder){
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		
	}
	
}
