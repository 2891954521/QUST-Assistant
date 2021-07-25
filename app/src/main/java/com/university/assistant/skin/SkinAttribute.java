package com.university.assistant.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.university.assistant.R;

import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SkinAttribute{

    public static final List<String> list = new ArrayList<>();

    static{
        list.add("background");
        list.add("src");
        list.add("textColor");
        list.add("drawableLeft");
        list.add("drawableTop");
        list.add("drawableRight");
        list.add("drawableBottom");
        list.add("tint");
        list.add("backgroundTint");
    }

    private ArrayList<SkinView> skinViews = new ArrayList<>();

    private static int[] getThemeResid(Context context,int[] attrs){
        int[] resIds = new int[]{ attrs.length };
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        for(int i = 0;i<typedArray.length();i++){
            int resourceId = typedArray.getResourceId(i,0);
            resIds[i] = resourceId;
        }
        typedArray.recycle();
        return resIds;
    }

    public void load(View view,AttributeSet attrs){
        ArrayList<SkinAttrParms> skinAttrParms = new ArrayList<>();
        for(int i = 0;i<attrs.getAttributeCount();i++){
            String attributeName = attrs.getAttributeName(i);
            if(list.contains(attributeName)){
                String attributeValue = attrs.getAttributeValue(i);
                if(attributeValue.startsWith("#")) continue;
                int id;
                if(attributeValue.startsWith("?")){
                    int attrid = Integer.parseInt(attributeValue.substring(1));
                    // 屏蔽掉波纹效果的设置
                    if(attrid == R.attr.selectableItemBackground || attrid == R.attr.selectableItemBackgroundBorderless) continue;
                    else id = getThemeResid(view.getContext(),new int[]{attrid})[0];
                }else id = Integer.parseInt(attributeValue.substring(1));
                if(id!=0){
                    SkinAttrParms attrParms = new SkinAttrParms(attributeName,id);
                    skinAttrParms.add(attrParms);
                }
            }
        }
        //将View与之对应的可以动态替换的属性集合放入集合中
        if(!skinAttrParms.isEmpty()){
            SkinView skinView = new SkinView(view,skinAttrParms);
            skinView.applySkin();
            skinViews.add(skinView);
        }
    }

    public void applySkin(){
        for(SkinView skinView : skinViews){
            skinView.applySkin();
        }
    }

    public static class SkinAttrParms{

        private String attrName;

        private int id;

        public SkinAttrParms(String attrName,int id){
            this.attrName = attrName;
            this.id = id;
        }

        public String getAttrName(){
            return attrName;
        }

        public void setAttrName(String attrName){
            this.attrName = attrName;
        }

        public int getId(){
            return id;
        }

        public void setId(int id){
            this.id = id;
        }
    }

    private static class SkinView{

        View view;

        List<SkinAttrParms> parms;

        public SkinView(View view,List<SkinAttrParms> parms){
            this.view = view;
            this.parms = parms;
        }

        public View getView(){
            return view;
        }

        public void setView(View view){
            this.view = view;
        }

        public void applySkin(){
            for(SkinAttrParms parms : parms){
                Drawable left = null, top = null, right = null, bottom = null;
                switch(parms.attrName){
                    case "background":
                        Object background = SkinResourcess.getInstance().getBackground(parms.id);
                        if(background instanceof Integer)view.setBackgroundColor((int)background);
                        else ViewCompat.setBackground(view,(Drawable)background);
                        break;
                    case "src":
                        background = SkinResourcess.getInstance().getBackground(parms.id);
                        if(background instanceof Integer)((ImageView)view).setImageDrawable(new ColorDrawable((int)background));
                        else ((ImageView)view).setImageDrawable((Drawable)background);
                        break;
                    case "textColor":
                        if(view instanceof TextView)((TextView)view).setTextColor(SkinResourcess.getInstance().getColorStateList(parms.id));
                        else ((Toolbar)view).setTitleTextColor(SkinResourcess.getInstance().getColor(parms.id));
                        break;
                    case "drawableLeft":
                        left = SkinResourcess.getInstance().getDrawable(parms.id);
                        break;
                    case "drawableTop":
                        top = SkinResourcess.getInstance().getDrawable(parms.id);
                        break;
                    case "drawableRight":
                        right = SkinResourcess.getInstance().getDrawable(parms.id);
                        break;
                    case "drawableBottom":
                        bottom = SkinResourcess.getInstance().getDrawable(parms.id);
                        break;
                    case "tint":
                        if(Build.VERSION.SDK_INT >= 21)((ImageView)view).setImageTintList(SkinResourcess.getInstance().getColorStateList(parms.id));
                        else ((ImageView)view).setColorFilter(SkinResourcess.getInstance().getColor(parms.id),PorterDuff.Mode.SRC_IN);
                        //Drawable originalDrawable = img.getDrawable();// ContextCompat.getDrawable(this, R.mipmap.ic_home);
                        //if(originalDrawable==null)break;
                        //Drawable tintDrawable = DrawableCompat.wrap(originalDrawable).mutate();
                        //DrawableCompat.setTint(tintDrawable,SkinResourcess.getInstance().getColor(parms.id));
                        //img.setImageDrawable(originalDrawable);
                        break;
                    case "backgroundTint":
                        ((FloatingActionButton)view).setBackgroundTintList(SkinResourcess.getInstance().getColorStateList(parms.id));
                        break;
                    default:
                        break;
                }
                if(null!=left||null!=right||null!=top||null!=bottom)((TextView)view).setCompoundDrawablesWithIntrinsicBounds(left,top,right,bottom);
            }
        }
    }
}
