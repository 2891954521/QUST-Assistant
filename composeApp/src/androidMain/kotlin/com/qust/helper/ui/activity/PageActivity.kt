package com.qust.helper.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qust.helper.data.Pages
import com.qust.helper.ui.page.BasePage
import com.qust.helper.ui.page.EmptyPage
import com.qust.helper.ui.theme.AppTheme

class PageActivity: ComponentActivity() {

    companion object {
        fun startActivity(context: Context, page: String){
            context.startActivity(Intent(context, PageActivity::class.java).putExtra("page", page))
        }
    }

    lateinit var page: BasePage<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = Pages[intent.getStringExtra("page")]
        if(page == null) {
            this.page = EmptyPage
        }else{
            this.page = page
        }

        setContent { AppTheme { this.page.ComposePage() } }
    }
}