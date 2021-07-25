package com.university.assistant.skin;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import com.university.assistant.util.LogUtil;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SkinActivityLifecycle implements Application.ActivityLifecycleCallbacks{

    private HashMap<Activity,SkinFactory> mLayoutFactoryMap = new HashMap<>();

    @Override
    public void onActivityCreated(@NonNull Activity activity,Bundle savedInstanceState){
        // if(activity instanceof SettingActivity)return;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        // 获得Activity的布局加载器
        try{
            // Android布局加载器使用mFactorySet标记是否设置过Factory
            // 如设置过抛出一次
            // 设置mFactorySet标签为false
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater,false);
        }catch(Exception e){
            LogUtil.Log(e);
        }
        SkinFactory skinLayoutFactory = new SkinFactory();
        LayoutInflaterCompat.setFactory2(layoutInflater,skinLayoutFactory);
        //注册观察者
        SkinManager.getInstance().addObserver(skinLayoutFactory);
        mLayoutFactoryMap.put(activity,skinLayoutFactory);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity){ }

    @Override
    public void onActivityResumed(@NonNull Activity activity){ }

    @Override
    public void onActivityPaused(@NonNull Activity activity){ }

    @Override
    public void onActivityStopped(@NonNull Activity activity){ }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity,@NonNull Bundle outState){ }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity){
        // if(activity instanceof SettingActivity)return;
        //删除观察者
        SkinFactory skinLayoutFactory = mLayoutFactoryMap.remove(activity);
        SkinManager.getInstance().deleteObserver(skinLayoutFactory);
    }
}
