package com.qust.assistant.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.qust.assistant.ui.MainActivity;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment{
    
    protected MainActivity activity;
    
    protected boolean isCreated;
    
    public BaseFragment(){ }
    
    public void onReceive(String action){ }
    
    public void onResume(String className,Intent intent){ }

    public void onCreateMenu(ImageView view){
        view.setVisibility(View.GONE);
    }
    
    public boolean onBackPressed(){ return true; }
    
    public BaseFragment setActivity(MainActivity mainActivity){
        this.activity = mainActivity;
        return this;
    }
    
    protected void toast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }
    
    public String getTitle(){
        return "";
    }
    
}
