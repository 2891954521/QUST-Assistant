package com.university.assistant.skin;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Observable;

public class SkinManager extends Observable{
    // 皮肤SharedPreferences文件名
    private static final String SKIN_SHARED = "skins";
    // 皮肤SharedPreferences属性名
    private static final String KEY_SKIN_PATH = "skin-path";

    private static SkinManager skinManager;
	
    private Application application;

    private SharedPreferences mPref;

    private SkinManager(Application application){
        this.application = application;
        mPref = application.getSharedPreferences(SKIN_SHARED,Context.MODE_PRIVATE);
        SkinResourcess.init(application);
        application.registerActivityLifecycleCallbacks(new SkinActivityLifecycle());
        loadSkin(getSkin());
    }

    public static void init(Application application){
        synchronized(SkinManager.class){
            if(skinManager==null)skinManager = new SkinManager(application);
        }
    }

    public static SkinManager getInstance(){
        return skinManager;
    }

    public void loadSkin(String path){
        if(TextUtils.isEmpty(path)){
            setSkin("");
            SkinResourcess.getInstance().reset();
        }else{
            try{
				// 反射获取AssetManager
                AssetManager assetManager = AssetManager.class.newInstance();
                // 反射添加资源路径
				Method method = assetManager.getClass().getMethod("addAssetPath",String.class);
                method.setAccessible(true);
                method.invoke(assetManager,path);
                Resources resources = application.getResources();
                Resources skinRes = new Resources(assetManager,resources.getDisplayMetrics(),resources.getConfiguration());
                //获取包名
                PackageManager mPm = application.getPackageManager();
                PackageInfo info = mPm.getPackageArchiveInfo(path,PackageManager.GET_ACTIVITIES);
                String packageName = info.packageName;
				// 应用皮肤
                SkinResourcess.getInstance().applySkin(skinRes,packageName);
                // 保存皮肤路径
                setSkin(path);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void setSkin(String skinPath){
        mPref.edit().putString(KEY_SKIN_PATH,skinPath).apply();
    }

    public String getSkin(){
        return mPref.getString(KEY_SKIN_PATH,null);
    }

}