package com.qust.assistant.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.NavigationActivity;
import com.qust.assistant.ui.note.NoteActivity;
import com.qust.assistant.ui.picture.PictureActivity;
import com.qust.assistant.ui.school.AutoEvaluationActivity;
import com.qust.assistant.ui.school.GetAcademicActivity;
import com.qust.assistant.ui.school.GetExamActivity;
import com.qust.assistant.ui.school.GetLessonTableActivity;
import com.qust.assistant.ui.school.GetMarkActivity;
import com.qust.assistant.ui.third.CpDailyActivity;
import com.qust.assistant.ui.third.DrinkActivity;
import com.qust.assistant.ui.third.fake.LeaveListActivity;

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
        layout.findViewById(R.id.fragment_home_note).setOnClickListener(v -> activity.startActivity(new Intent(activity, NoteActivity.class)));
        layout.findViewById(R.id.fragment_home_pictures).setOnClickListener(v -> activity.startActivity(new Intent(activity, PictureActivity.class)));

        // layout.findViewById(R.id.fragment_home_account).setOnClickListener(v -> activity.startActivity(new Intent(activity,AccountsActivity.class)));
        layout.findViewById(R.id.fragment_home_navigation).setOnClickListener(v -> activity.startActivity(new Intent(activity,NavigationActivity.class)));

        layout.findViewById(R.id.fragment_home_drink).setOnClickListener(v -> activity.startActivity(new Intent(activity,DrinkActivity.class)));
        layout.findViewById(R.id.fragment_home_cpdaily).setOnClickListener(v -> activity.startActivity(new Intent(activity,CpDailyActivity.class)));
        layout.findViewById(R.id.fragment_home_fake_daily).setOnClickListener(v -> activity.startActivity(new Intent(activity,LeaveListActivity.class)));


        layout.findViewById(R.id.fragment_home_school_lesson).setOnClickListener(v -> activity.startActivity(new Intent(activity,GetLessonTableActivity.class)));
        layout.findViewById(R.id.fragment_home_school_mark).setOnClickListener(v -> activity.startActivity(new Intent(activity,GetMarkActivity.class)));
        layout.findViewById(R.id.fragment_home_school_academic).setOnClickListener(v -> activity.startActivity(new Intent(activity,GetAcademicActivity.class)));
        layout.findViewById(R.id.fragment_home_school_auto_evaluation).setOnClickListener(v -> activity.startActivity(new Intent(activity, AutoEvaluationActivity.class)));
        // layout.findViewById(R.id.fragment_home_school_course_selection).setOnClickListener(v -> toast("未完成！"));
        layout.findViewById(R.id.fragment_home_school_empty_class).setOnClickListener(v -> toast("未完成！"));
        layout.findViewById(R.id.fragment_home_school_exam).setOnClickListener(v -> activity.startActivity(new Intent(activity,GetExamActivity.class)));
    
    
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