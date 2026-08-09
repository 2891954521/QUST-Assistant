package com.qust.helper.next.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.Clock

/**
 * 点击事件
 *
 * @param onClick 点击回调，为 null 时为不可点击
 */
@Composable
fun Modifier.onClick(onClick: (() -> Unit)?): Modifier {
    return this.then(if(onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
}

/**
 * 点击防抖，防止多次点击导致重复执行
 * 
 * @param delay 延迟时间，单位：毫秒
 * @param click 点击事件
 */
@Composable
fun Modifier.clickDebounce(delay: Long = 500, click: () -> Unit): Modifier {
    var time by remember { mutableLongStateOf(0L) }
    return this.clickable(onClick = {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - time >= delay) {
            time = currentTime
            click()
        }
    })
}

/**
 * 长按
 * 
 * @param onClick 点击事件
 * @param longClick 长按事件
 */
@Composable
fun Modifier.longClick(onClick: () -> Unit = { }, longClick: () -> Unit): Modifier {
    return this.combinedClickable(onClick = onClick, onLongClick = longClick)
}