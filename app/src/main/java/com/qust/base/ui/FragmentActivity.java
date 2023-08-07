package com.qust.base.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.R;
import com.qust.assistant.util.LogUtil;
import com.qust.base.fragment.BaseFragment;

public class FragmentActivity extends BaseFragmentAbleActivity{
	
	private BaseFragment fragment;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initFragment(getIntent());
	}
	
	public void initFragment(@NonNull Intent intent){
		Class<?> clazz;
		String className = intent.getStringExtra("fragment");
		
		if(className != null){
			try{
				clazz = Class.forName(className);
			}catch(ClassNotFoundException e){
				LogUtil.Log(e);
				toastError("参数错误");
				return;
			}
		}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
			clazz = intent.getSerializableExtra("fragmentClazz", Class.class);
		}else{
			clazz = (Class<?>)intent.getSerializableExtra("fragmentClazz");
		}
		
		if(clazz == null){
			toastError("参数错误");
			return;
		}
		
		try{
			if(BaseFragment.class.isAssignableFrom(clazz)){
				try{
					fragment = (BaseFragment)clazz.getConstructor(boolean.class, boolean.class).newInstance(true, true);
				}catch(NoSuchMethodException e){
					fragment = (BaseFragment)clazz.newInstance();
				}
				getSupportFragmentManager().beginTransaction().add(R.id.main_frame, fragment, clazz.getName()).commit();
			}else{
				throw new IllegalArgumentException("param error: " + clazz.getName());
			}
		}catch(ReflectiveOperationException e){
			LogUtil.Log(e);
			toastError("参数错误");
		}
	}
	
	
	@Override
	public void startNewFragment(Class<? extends BaseFragment> newFragment){
		startActivity(new Intent(this, FragmentActivity.class).putExtra("fragmentClazz", newFragment));
	}
	
	@Override
	public void closeFragment(){
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		fragment.onResult(requestCode, resultCode, data);
	}
	
	@Override
	public void startActivity(Intent intent){
		super.startActivity(intent);
		overridePendingTransition(R.anim.anim_right_in, 0);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(0, R.anim.anim_rigth_out);
	}
}
