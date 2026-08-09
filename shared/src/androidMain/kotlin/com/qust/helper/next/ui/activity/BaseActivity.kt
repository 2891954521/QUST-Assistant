package com.qust.helper.next.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.qust.helper.next.ui.router.ActivityCollector

open class BaseActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }
}