package com.qust.assistant.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;

import com.qust.assistant.R;
import com.qust.assistant.ui.NavigationActivity;
import com.qust.assistant.ui.fragment.picture.PictureFragment;
import com.qust.assistant.ui.fragment.school.AutoEvaluationFragment;
import com.qust.assistant.ui.fragment.school.GetAcademicFragment;
import com.qust.assistant.ui.fragment.school.GetExamFragment;
import com.qust.assistant.ui.fragment.school.GetLessonTableFragment;
import com.qust.assistant.ui.fragment.school.GetMarkFragment;
import com.qust.assistant.ui.fragment.third.CpDailyActivity;
import com.qust.assistant.ui.fragment.third.DrinkFragment;
import com.qust.assistant.ui.fragment.third.fake.LeaveListActivity;
import com.qust.assistant.ui.note.NoteActivity;

public class HomeFragment extends BaseFragment{
    
    @Override
    protected void initLayout(LayoutInflater inflater){
    
        rootView.findViewById(R.id.fragment_home_lesson_table).setOnClickListener(v -> {});
        rootView.findViewById(R.id.fragment_home_note).setOnClickListener(v -> activity.startActivity(new Intent(activity, NoteActivity.class)));
        rootView.findViewById(R.id.fragment_home_pictures).setOnClickListener(v -> activity.startActivity(new Intent(activity, PictureFragment.class)));
    
        // rootView.findViewById(R.id.fragment_home_account).setOnClickListener(v -> activity.startActivity(new Intent(activity,AccountsActivity.class)));
        rootView.findViewById(R.id.fragment_home_navigation).setOnClickListener(v -> activity.startActivity(new Intent(activity,NavigationActivity.class)));
    
        rootView.findViewById(R.id.fragment_home_drink).setOnClickListener(v -> activity.startActivity(new Intent(activity, DrinkFragment.class)));
        rootView.findViewById(R.id.fragment_home_cpdaily).setOnClickListener(v -> activity.startActivity(new Intent(activity,CpDailyActivity.class)));
        rootView.findViewById(R.id.fragment_home_fake_daily).setOnClickListener(v -> activity.startActivity(new Intent(activity,LeaveListActivity.class)));
    
    
        rootView.findViewById(R.id.fragment_home_school_lesson).setOnClickListener(v -> activity.startActivity(new Intent(activity, GetLessonTableFragment.class)));
        rootView.findViewById(R.id.fragment_home_school_mark).setOnClickListener(v -> activity.startActivity(new Intent(activity, GetMarkFragment.class)));
        rootView.findViewById(R.id.fragment_home_school_academic).setOnClickListener(v -> activity.startActivity(new Intent(activity, GetAcademicFragment.class)));
        rootView.findViewById(R.id.fragment_home_school_auto_evaluation).setOnClickListener(v -> activity.startActivity(new Intent(activity, AutoEvaluationFragment.class)));
        // rootView.findViewById(R.id.fragment_home_school_course_selection).setOnClickListener(v -> toast("未完成！"));
        rootView.findViewById(R.id.fragment_home_school_empty_class).setOnClickListener(v -> toast("未完成！"));
        rootView.findViewById(R.id.fragment_home_school_exam).setOnClickListener(v -> activity.startActivity(new Intent(activity, GetExamFragment.class)));
    }
    
    @Override
    protected int getLayout(){
        return R.layout.fragment_home;
    }
    
    @Override
    public String getName(){
        return "主页";
    }
}