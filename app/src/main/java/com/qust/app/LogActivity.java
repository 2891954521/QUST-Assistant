package com.qust.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.R;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.base.ui.BaseAnimActivity;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogActivity extends BaseAnimActivity{
    
    private final Pattern pattern = Pattern.compile(":([\\-0-9]+)\n");
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        
        String s = getIntent().getStringExtra("file");
        if(s != null){
            
            File f = new File(LogUtil.LogFile, s);
            
            SpannableStringBuilder str = new SpannableStringBuilder(FileUtil.readFile(f));
            Matcher matcher = pattern.matcher(str);
            while(matcher.find()) setClickableSpan(str, matcher);
            
            TextView text = findViewById(R.id.activity_log_message);
            text.setText(str);
            text.setMovementMethod(LinkMovementMethod.getInstance());
            
            initToolBar(f.getName());
        }
        
    }
    
    private void setClickableSpan(SpannableStringBuilder clickableHtmlBuilder, Matcher matcher){
        final String s = matcher.group(1);
        ClickableSpan clickableSpan = new ClickableSpan(){
            @Override
            public void onClick(@NonNull View view){
                startActivity(new Intent(LogActivity.this, LogActivity.class).putExtra("file", s + ".log"));
            }
            
            @Override
            public void updateDrawState(TextPaint ds){
                ds.setUnderlineText(true);
            }
        };
        // 设置超链接文本的颜色
        // clickableHtmlBuilder.setSpan(new ForegroundColorSpan(SkinResourcess.getInstance().getColor(R.color.Accent)),start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableHtmlBuilder.setSpan(clickableSpan, matcher.start() + 1, matcher.end() - 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }
}
