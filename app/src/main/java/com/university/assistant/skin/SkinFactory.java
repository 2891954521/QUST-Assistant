package com.university.assistant.skin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;

public class SkinFactory implements LayoutInflater.Factory2, Observer{
	
	private static final HashMap<String,Constructor<? extends View>> sConstructorMap = new HashMap<>();
	
	private static final Class<?>[] mConstructorSignature = new Class[]{Context.class,AttributeSet.class};
	
	public final String[] a = new String[]{"android.widget.","android.view.",};
	// 属性处理类
	private SkinAttribute skinAttribute;
	
	public SkinFactory(){
		skinAttribute = new SkinAttribute();
	}
	
	@Override
	public View onCreateView(View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs){
		View view = createViewFormTag(name,context,attrs);
		if(view == null) view = createView(name,context,attrs);
		else skinAttribute.load(view,attrs);
		return view;
	}
	
	private View createView(String name,Context context,AttributeSet attrs){
		Constructor<? extends View> constructor = findConstructor(context,name);
		try{
			return constructor.newInstance(context,attrs);
		}catch(Exception ignored){ }
		return null;
	}
	
	private View createViewFormTag(String name,Context context,AttributeSet attrs){
		// 包含自定义控件
		if(name.indexOf('.')!=-1) return createView(name,context,attrs);
		View view = null;
		for(String an : a){
			view = createView(an + name,context,attrs);
			if(view != null) break;
		}
		return view;
	}
	
	private Constructor<? extends View> findConstructor(Context context,String name){
		Constructor<? extends View> constructor = sConstructorMap.get(name);
		if(null==constructor){
			try{
				Class<? extends View> clazz = context.getClassLoader().loadClass(name).asSubclass(View.class);
				constructor = clazz.getConstructor(mConstructorSignature);
				sConstructorMap.put(name,constructor);
			}catch(Exception ignored){
			}
		}
		return constructor;
	}
	
	@Override
	public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs){
		return null;
	}
	
	@Override
	public void update(Observable o,Object arg){
		// 更换皮肤
		skinAttribute.applySkin();
	}
}
