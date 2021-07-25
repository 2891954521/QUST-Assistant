package com.university.assistant.skin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class SkinResourcess{
	
	private static SkinResourcess skinManager;
	
    private Resources skinResources;

    private Resources appResources;

    private String mSkinPkgName;
	
    private boolean isDefaultSkin = true;

    public static SkinResourcess getInstance(){
        return skinManager;
    }

    public static void init(Context context){
        synchronized(SkinManager.class){
            if(skinManager==null)skinManager = new SkinResourcess(context);
        }
    }

    private SkinResourcess(Context context){
        this.appResources = context.getResources();
    }

    public void applySkin(Resources resources,String pkgName){
        skinResources = resources;
        mSkinPkgName = pkgName;
        //是否使用默认皮肤
        isDefaultSkin = TextUtils.isEmpty(pkgName) || resources == null;
    }
	
	public void reset(){
        skinResources = null;
        mSkinPkgName = "";
        isDefaultSkin = true;
    }

	// 获取皮肤包中对应的id
    private int getIdentifier(int resId){
        if(isDefaultSkin)return resId;
        // 在皮肤包中不一定就是当前程序的 id
        // 获取对应id在当前的名称
        String resName = appResources.getResourceEntryName(resId);
        String resType = appResources.getResourceTypeName(resId);
        return skinResources.getIdentifier(resName,resType,mSkinPkgName);
    }

    public int getColor(int resId){
        if(isDefaultSkin)return appResources.getColor(resId);
        int skinId = getIdentifier(resId);
        if(skinId==0)return appResources.getColor(resId);
        return skinResources.getColor(skinId);
    }

    public ColorStateList getColorStateList(int resId){
        if(isDefaultSkin)return appResources.getColorStateList(resId);
        int skinId = getIdentifier(resId);
        if(skinId==0)return appResources.getColorStateList(resId);
        return skinResources.getColorStateList(skinId);
    }

    public Drawable getDrawable(int resId){
        if(isDefaultSkin)return appResources.getDrawable(resId);
        int skinId = getIdentifier(resId);
        if(skinId==0)return appResources.getDrawable(resId);
        return skinResources.getDrawable(skinId);
    }
    // 可能是Color 也可能是drawable
    public Object getBackground(int resId){
        String resourceTypeName = appResources.getResourceTypeName(resId);
        if(resourceTypeName.equals("color"))return getColor(resId);
        else return getDrawable(resId);
    }
	
    public String getString(int resId){
        try{
            if(isDefaultSkin)return appResources.getString(resId);
            int skinId = getIdentifier(resId);
            if(skinId==0)return appResources.getString(skinId);
            return skinResources.getString(skinId);
        }catch(Resources.NotFoundException ignored){ }
        return null;
    }

    public Typeface getTypeface(int resId){
        String skinTypefacePath = getString(resId);
        if(TextUtils.isEmpty(skinTypefacePath))return Typeface.DEFAULT; 
        try{
            if(isDefaultSkin)return Typeface.createFromAsset(appResources.getAssets(),skinTypefacePath);
            return Typeface.createFromAsset(skinResources.getAssets(),skinTypefacePath);
        }catch(RuntimeException ignored){}
        return Typeface.DEFAULT;
    }
}
