package com.university.assistant.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.university.assistant.MainActivity;
import com.university.assistant.fragment.home.HomeFragment;
import com.university.assistant.ui.BaseActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
