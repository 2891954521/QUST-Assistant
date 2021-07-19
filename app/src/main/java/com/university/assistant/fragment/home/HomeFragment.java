package com.university.assistant.fragment.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.university.assistant.MainActivity;
import com.university.assistant.R;
import com.university.assistant.fragment.BaseFragment;
import com.university.assistant.ui.school.GetAcademicActivity;
import com.university.assistant.ui.school.GetExamActivity;
import com.university.assistant.ui.third.CpDailyActivity;
import com.university.assistant.ui.third.DrinkActivity;
import com.university.assistant.ui.school.GetLessonTableActivity;
import com.university.assistant.ui.school.GetMarkActivity;
import com.university.assistant.ui.PictureActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeFragment extends BaseFragment{
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        if(activity==null) activity = (MainActivity)getContext();
        isCreated = true;
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_home,container,false);
        layout.findViewById(R.id.fragment_home_lesson_table).setOnClickListener(v -> activity.navigationTo(1));
        layout.findViewById(R.id.fragment_home_note).setOnClickListener(v -> activity.navigationTo(2));
        layout.findViewById(R.id.fragment_home_pictures).setOnClickListener(v -> startActivity(new Intent(getContext(),PictureActivity.class)));
        layout.findViewById(R.id.fragment_home_drink).setOnClickListener(v -> startActivity(new Intent(getContext(),DrinkActivity.class)));
        layout.findViewById(R.id.fragment_home_cpdaily).setOnClickListener(v -> startActivity(new Intent(getContext(),CpDailyActivity.class)));
        layout.findViewById(R.id.fragment_home_school_lesson).setOnClickListener(v -> startActivity(new Intent(getContext(),GetLessonTableActivity.class)));
        layout.findViewById(R.id.fragment_home_school_mark).setOnClickListener(v -> startActivity(new Intent(getContext(),GetMarkActivity.class)));
        layout.findViewById(R.id.fragment_home_school_academic).setOnClickListener(v -> startActivity(new Intent(getContext(),GetAcademicActivity.class)));
        layout.findViewById(R.id.fragment_home_school_course_selection).setOnClickListener(v -> toast("未完成！"));
        layout.findViewById(R.id.fragment_home_school_empty_class).setOnClickListener(v -> toast("未完成！"));
        layout.findViewById(R.id.fragment_home_school_exam).setOnClickListener(v -> startActivity(new Intent(getContext(),GetExamActivity.class)));
    
    
        return layout;
    }
    
    @Override
    public void onCreateMenu(ImageView view){
        if(!isCreated)return;
        view.setVisibility(View.GONE);
    }
    
    @Override
    public String getTitle(){
        return "主页";
    }
}