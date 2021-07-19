package com.university.assistant;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.university.assistant.fragment.BaseFragment;
import com.university.assistant.fragment.home.HomeFragment;
import com.university.assistant.fragment.lessontable.LessonTableFragment;
import com.university.assistant.fragment.note.NoteFragment;
import com.university.assistant.ui.BaseActivity;
import com.university.assistant.ui.school.LoginActivity;
import com.university.assistant.ui.UpdateActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends BaseActivity{
	
	private static final int NOT_NOTICE = 2;
	
	public ViewPager viewPager;
	
	private BaseFragment[] fragments = new BaseFragment[3];
	
	private Toolbar toolbar;
	
	private ImageView menuView;
	
	private DrawerLayout drawer;
	
	private BottomNavigationView navigationView;
	
	private int position;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		toolbar = findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(v -> getDrawer().open());
		
		fragments[0] = new HomeFragment().setActivity(this);
		fragments[1] = new LessonTableFragment().setActivity(this);
		fragments[2] = new NoteFragment().setActivity(this);
		
		NavigationView nav = findViewById(R.id.nav_view);
		nav.setNavigationItemSelectedListener(item -> {
			switch(item.getItemId()){
				case R.id.nav_login:
					startActivity(new Intent(this,LoginActivity.class));
					break;
				case R.id.nav_update:
					startActivity(new Intent(this,UpdateActivity.class));
					break;
				case R.id.nav_log:
					startActivity(new Intent(this,LogActivity.class).putExtra("file","debug.log"));
					break;
			}
			return false;
		});
		
		drawer = findViewById(R.id.drawer_layout);
		
		menuView = findViewById(R.id.main_menu);
		
		navigationView = findViewById(R.id.main_navigation);
		navigationView.setOnNavigationItemSelectedListener(item -> {
			viewPager.setCurrentItem(item.getOrder());
			return true;
		});
		
		viewPager = findViewById(R.id.main_view_pager);
		viewPager.setOffscreenPageLimit(2);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
			@Override
			public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){ }
			
			@Override
			public void onPageSelected(int _position){
				position = _position;
				navigationView.getMenu().getItem(position).setChecked(true);
				toolbar.setTitle(fragments[position].getTitle());
				fragments[position].onCreateMenu(menuView);
			}
			
			@Override
			public void onPageScrollStateChanged(int state){ }
		});
		
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@NonNull @Override
			public Fragment getItem(int position) { return fragments[position]; }
			@Override
			public int getCount() { return fragments.length; }
		});
		viewPager.setCurrentItem(1);
		
		if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
			||ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		for(BaseFragment fragment : fragments){
			fragment.onResume();
		}
	}
	
	public void navigationTo(int page){
		viewPager.setCurrentItem(page);
	}
	
	public ImageView getMenu(){
		return menuView;
	}
	
	public ViewPager getViewPager(){ return viewPager; }
	
	public DrawerLayout getDrawer(){ return drawer; }
	
	@Override
	protected void registerReceiver(){
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context context,Intent intent){
				if(intent.getAction()==null) return;
				for(BaseFragment fragment: fragments)fragment.onReceive(intent.getAction());
			}
		},App.APP_UPDATE_LESSON_TABLE);
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
							ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
							dialog.dismiss();
						}).show();
					}
				}
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		if(requestCode==NOT_NOTICE){
			// 由于不知道是否选择了允许所以需要再次判断
			if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED
					||ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
				ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },1);
			}
		}
		if(resultCode==RESULT_OK){
			fragments[position].onResume(data.getStringExtra("class"),data);
		}
		super.onActivityResult(requestCode,resultCode,data);
	}
	
	@Override
	public void onBackPressed(){
		if(fragments[position].onBackPressed())super.onBackPressed();
	}
}