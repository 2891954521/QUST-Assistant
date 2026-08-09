package com.qust.helper.next.ui.component.input.numInput

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @param defaultContent 初始内容
 * @param enableDot 是否允许输入小数点
 * @param onUpdate 输入内容后的回调
 * @param onDone 点击确定后的回调
 */
class PopNumInputUIState(
    defaultContent: String = "0",
    enableDot: Boolean = true,
    enableNegative: Boolean = false,
    onUpdate: (String) -> Unit = { },
    onDone: (String) -> Unit
): NumInputUIState(defaultContent, enableDot, enableNegative, onUpdate, onDone) {

    var isShowPop by mutableStateOf(false)

    fun hide(){
        isShowPop = false
        clickDone()
    }

    /**
     * 显示弹窗
     * 
     * @param content 为空则使用旧内容，否则更新为传入的新内容
     */
    fun show(content: String? = null){
        if(content != null) setValue(content)
        isShowPop = true
    }

    override fun clickDone(){
        val result = getResult()
        if(result != null){
            content = result
            isShowPop = false
            onDone(result)
        }
    }

}