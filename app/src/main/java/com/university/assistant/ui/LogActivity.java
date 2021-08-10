package com.university.assistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.FileUtil;
import com.university.assistant.util.LogUtil;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LogActivity extends BaseAnimActivity{
    
    private Pattern pattern = Pattern.compile(":[\\-0-9]+\n");
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        
        String s = getIntent().getStringExtra("file");
        File f = null;
        if(s != null){
            f = new File(LogUtil.DebugLogFile,s);
            TextView text = findViewById(R.id.activity_log_message);
            SpannableStringBuilder str = new SpannableStringBuilder(FileUtil.readFile(f));
            // 根据正则匹配出带有超链接的文字
            Matcher matcher = pattern.matcher(str);
            while(matcher.find())setClickableSpan(str,matcher);
            text.setText(str);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }
        
        initToolBar(f == null ? null : f.getName());
        initSliding(null, null);
    }

    private void setClickableSpan(SpannableStringBuilder clickableHtmlBuilder,Matcher matcher){
        final String s = matcher.group();
        ClickableSpan clickableSpan = new ClickableSpan(){
            public void onClick(@NonNull View view){
                startActivity(new Intent(LogActivity.this,LogActivity.class).putExtra("file",s.substring(1,s.length()-1)+".log"));
            }
            @Override
            public void updateDrawState(TextPaint ds){
                ds.setUnderlineText(true);//当传入true时超链接下会有一条下划线
            }
        };
        // 设置超链接文本的颜色
        // clickableHtmlBuilder.setSpan(new ForegroundColorSpan(SkinResourcess.getInstance().getColor(R.color.Accent)),start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        clickableHtmlBuilder.setSpan(clickableSpan,matcher.start(),matcher.end(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }
}
